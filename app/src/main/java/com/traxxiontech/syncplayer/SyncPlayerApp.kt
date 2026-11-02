package com.traxxiontech.syncplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SyncPlayerApp : Application() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "sync_player_playback"
        const val NOTIFICATION_CHANNEL_ID = "sync_player_alerts"
        lateinit var instance: SyncPlayerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val playbackChannel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows now playing music notification with controls"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sync Player Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Media scanning, storage and vault alerts"
            }

            notificationManager.createNotificationChannel(playbackChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }
}
