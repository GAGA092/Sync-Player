package com.traxxiontech.syncplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.traxxiontech.syncplayer.MainActivity
import com.traxxiontech.syncplayer.R
import com.traxxiontech.syncplayer.SyncPlayerApp
import com.traxxiontech.syncplayer.model.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentSong: SongItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffle: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutesLeft: Int = 0,
    val bassBoostStrength: Short = 0,
    val eqEnabled: Boolean = false
)

class MusicPlaybackService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private var playlist: List<SongItem> = emptyList()
    private var currentIndex: Int = -1

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var sleepTimer: CountDownTimer? = null

    companion object {
        const val ACTION_PLAY = "com.traxxiontech.syncplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.traxxiontech.syncplayer.ACTION_PAUSE"
        const val ACTION_PLAY_PAUSE = "com.traxxiontech.syncplayer.ACTION_PLAY_PAUSE"
        const val ACTION_PREV = "com.traxxiontech.syncplayer.ACTION_PREV"
        const val ACTION_NEXT = "com.traxxiontech.syncplayer.ACTION_NEXT"
        const val ACTION_STOP = "com.traxxiontech.syncplayer.ACTION_STOP"
        const val NOTIFICATION_ID = 1001

        private val _playbackState = MutableStateFlow(PlaybackState())
        val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

        var activeService: MusicPlaybackService? = null
            private set
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        activeService = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initMediaPlayer()
        registerReceivers()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                handleTrackCompletion()
            }
            setOnErrorListener { _, _, _ ->
                true
            }
        }
        initAudioFx()
    }

    private fun initAudioFx() {
        try {
            val audioSessionId = mediaPlayer?.audioSessionId ?: return
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_PLAY -> resumePlayback()
            ACTION_PAUSE -> pausePlayback()
            ACTION_PREV -> playPrevious()
            ACTION_NEXT -> playNext()
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    fun setQueue(songs: List<SongItem>, startIndex: Int = 0) {
        playlist = songs
        currentIndex = startIndex
        if (playlist.isNotEmpty() && currentIndex in playlist.indices) {
            playSong(playlist[currentIndex])
        }
    }

    fun playSong(song: SongItem) {
        try {
            requestAudioFocus()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(applicationContext, song.uri)
            mediaPlayer?.prepare()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(_playbackState.value.playbackSpeed) ?: return
            }

            mediaPlayer?.start()

            _playbackState.value = _playbackState.value.copy(
                currentSong = song,
                isPlaying = true,
                durationMs = mediaPlayer?.duration?.toLong() ?: song.durationMs,
                currentPositionMs = 0L
            )

            startProgressTracker()
            startForeground(NOTIFICATION_ID, buildNotification(song, true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (mediaPlayer?.isPlaying == true) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    fun resumePlayback() {
        if (requestAudioFocus()) {
            mediaPlayer?.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracker()
            _playbackState.value.currentSong?.let {
                startForeground(NOTIFICATION_ID, buildNotification(it, true))
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        _playbackState.value.currentSong?.let {
            val notification = buildNotification(it, false)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun playNext() {
        if (playlist.isEmpty()) return
        if (_playbackState.value.isShuffle) {
            currentIndex = playlist.indices.random()
        } else {
            currentIndex = (currentIndex + 1) % playlist.size
        }
        playSong(playlist[currentIndex])
    }

    fun playPrevious() {
        if (playlist.isEmpty()) return
        val pos = mediaPlayer?.currentPosition ?: 0
        if (pos > 3000) {
            seekTo(0)
            return
        }
        currentIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
        playSong(playlist[currentIndex])
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
        }
    }

    fun toggleRepeat() {
        val next = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = next)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun setBassBoost(level: Short) {
        try {
            bassBoost?.setStrength(level)
            _playbackState.value = _playbackState.value.copy(bassBoostStrength = level)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerBandGain(band: Short, gainMilliBels: Short) {
        try {
            equalizer?.setBandLevel(band, gainMilliBels)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            _playbackState.value = _playbackState.value.copy(eqEnabled = enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        if (minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = 0)
            return
        }

        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = minutes)
        sleepTimer = object : CountDownTimer((minutes * 60 * 1000).toLong(), 60000L) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 60000).toInt() + 1
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = mins)
            }

            override fun onFinish() {
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = 0)
                pausePlayback()
            }
        }.start()
    }

    private fun handleTrackCompletion() {
        when (_playbackState.value.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
            }
            RepeatMode.ALL -> playNext()
            RepeatMode.OFF -> {
                if (currentIndex < playlist.size - 1) {
                    playNext()
                } else {
                    pausePlayback()
                    seekTo(0)
                }
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _playbackState.value = _playbackState.value.copy(currentPositionMs = current)
                delay(300)
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> pausePlayback()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlayback()
                        AudioManager.AUDIOFOCUS_GAIN -> resumePlayback()
                    }
                }
                .build()
            return audioManager?.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            return audioManager?.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) pausePlayback()
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun buildNotification(song: SongItem, isPlaying: Boolean): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 1, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getService(
            this, 2, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 3, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 4, Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, SyncPlayerApp.PLAYBACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(song.title)
            .setContentText("${song.artist} • ${song.album}")
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Prev", prevIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${song.artist} • ${song.album}\nTap to open Sync Player")
            )
            .build()
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pausePlayback()
            }
        }
    }

    private fun registerReceivers() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeService = null
        progressJob?.cancel()
        sleepTimer?.cancel()
        try {
            unregisterReceiver(noisyReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer?.release()
        bassBoost?.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
