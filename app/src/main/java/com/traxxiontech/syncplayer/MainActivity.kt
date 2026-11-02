package com.traxxiontech.syncplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.data.MediaRepository
import com.traxxiontech.syncplayer.model.MediaType
import com.traxxiontech.syncplayer.model.PhotoItem
import com.traxxiontech.syncplayer.model.SongItem
import com.traxxiontech.syncplayer.model.SyncedLyrics
import com.traxxiontech.syncplayer.model.ThemeType
import com.traxxiontech.syncplayer.model.UserProfile
import com.traxxiontech.syncplayer.model.VaultMediaItem
import com.traxxiontech.syncplayer.model.VideoItem
import com.traxxiontech.syncplayer.service.MusicPlaybackService
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.MiniPlayerBar
import com.traxxiontech.syncplayer.ui.screens.AboutScreen
import com.traxxiontech.syncplayer.ui.screens.FullMusicPlayerScreen
import com.traxxiontech.syncplayer.ui.screens.FullscreenPhotoViewer
import com.traxxiontech.syncplayer.ui.screens.FullscreenVideoPlayer
import com.traxxiontech.syncplayer.ui.screens.HomeScreen
import com.traxxiontech.syncplayer.ui.screens.MediaManagerScreen
import com.traxxiontech.syncplayer.ui.screens.MediaShareScreen
import com.traxxiontech.syncplayer.ui.screens.PhotoGalleryScreen
import com.traxxiontech.syncplayer.ui.screens.SettingsScreen
import com.traxxiontech.syncplayer.ui.screens.SongRowItem
import com.traxxiontech.syncplayer.ui.screens.SplashScreen
import com.traxxiontech.syncplayer.ui.screens.VaultScreen
import com.traxxiontech.syncplayer.ui.screens.VideoScreen
import com.traxxiontech.syncplayer.ui.screens.WelcomeProfileScreen
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors
import com.traxxiontech.syncplayer.ui.theme.SyncPlayerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: MediaRepository
    private var playbackService: MusicPlaybackService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicPlaybackService.LocalBinder
            playbackService = binder?.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = MediaRepository(applicationContext)

        // Bind playback service
        val intent = Intent(this, MusicPlaybackService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            val userProfile by repository.userProfile.collectAsStateWithLifecycle()
            var selectedTheme by remember { mutableStateOf(userProfile.favoriteTheme) }

            // Sync with profile theme
            LaunchedEffect(userProfile.favoriteTheme) {
                selectedTheme = userProfile.favoriteTheme
            }

            SyncPlayerTheme(selectedTheme = selectedTheme) {
                SyncPlayerMainContent(
                    repository = repository,
                    getService = { playbackService },
                    onThemeChanged = { newTheme ->
                        selectedTheme = newTheme
                        repository.updateUserProfile(userProfile.copy(favoriteTheme = newTheme))
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}

enum class MainScreenState {
    SPLASH, WELCOME_PROFILE, MAIN
}

@Composable
fun SyncPlayerMainContent(
    repository: MediaRepository,
    getService: () -> MusicPlaybackService?,
    onThemeChanged: (ThemeType) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val glassColors = LocalGlassmorphicColors.current

    var screenState by remember { mutableStateOf(MainScreenState.SPLASH) }
    var currentNavRoute by remember { mutableStateOf("home") }

    // Media flows
    val songs by repository.songs.collectAsStateWithLifecycle()
    val videos by repository.videos.collectAsStateWithLifecycle()
    val photos by repository.photos.collectAsStateWithLifecycle()
    val playlists by repository.playlists.collectAsStateWithLifecycle()
    val vaultItems by repository.vaultItems.collectAsStateWithLifecycle()
    val userProfile by repository.userProfile.collectAsStateWithLifecycle()
    val isScanning by repository.isScanning.collectAsStateWithLifecycle()

    // Playback state
    val playbackState by MusicPlaybackService.playbackState.collectAsStateWithLifecycle()

    // Full screen overlays
    var isFullPlayerVisible by remember { mutableStateOf(false) }
    var currentPlayingLyrics by remember { mutableStateOf<SyncedLyrics?>(null) }
    var viewingVideo by remember { mutableStateOf<VideoItem?>(null) }
    var viewingPhoto by remember { mutableStateOf<PhotoItem?>(null) }

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Request permissions on start
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        coroutineScope.launch {
            repository.scanDeviceMedia()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Load lyrics when current song changes
    LaunchedEffect(playbackState.currentSong?.id) {
        playbackState.currentSong?.let { song ->
            currentPlayingLyrics = repository.getLyricsForSong(song)
        }
    }

    // Back button handling
    BackHandler(enabled = isFullPlayerVisible || viewingVideo != null || viewingPhoto != null || currentNavRoute != "home" || drawerState.isOpen) {
        when {
            drawerState.isOpen -> coroutineScope.launch { drawerState.close() }
            viewingVideo != null -> viewingVideo = null
            viewingPhoto != null -> viewingPhoto = null
            isFullPlayerVisible -> isFullPlayerVisible = false
            currentNavRoute != "home" -> currentNavRoute = "home"
        }
    }

    when (screenState) {
        MainScreenState.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    screenState = if (userProfile.fullName.isBlank()) {
                        MainScreenState.WELCOME_PROFILE
                    } else {
                        MainScreenState.MAIN
                    }
                }
            )
        }

        MainScreenState.WELCOME_PROFILE -> {
            WelcomeProfileScreen(
                currentProfile = userProfile,
                onCompleteProfile = { updated ->
                    repository.updateUserProfile(updated)
                    onThemeChanged(updated.favoriteTheme)
                    screenState = MainScreenState.MAIN
                }
            )
        }

        MainScreenState.MAIN -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFF0F141C),
                        drawerContentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .statusBarsPadding()
                        ) {
                            // User Profile in Drawer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentNavRoute = "settings"
                                        coroutineScope.launch { drawerState.close() }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(glassColors.glowColor.copy(alpha = 0.2f))
                                        .border(2.dp, glassColors.glowColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!userProfile.avatarUriString.isNullOrBlank()) {
                                        AsyncImage(
                                            model = userProfile.avatarUriString,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Avatar",
                                            tint = glassColors.glowColor,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = userProfile.fullName.ifBlank { "User Profile" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "@${userProfile.username.ifBlank { "syncuser" }}",
                                        fontSize = 12.sp,
                                        color = glassColors.glowColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(glassColors.borderGlass.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Drawer items
                            DrawerNavItem("Home", Icons.Filled.Home, currentNavRoute == "home") {
                                currentNavRoute = "home"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Music Library", Icons.Filled.MusicNote, currentNavRoute == "music") {
                                currentNavRoute = "music"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Video Cinema", Icons.Filled.Videocam, currentNavRoute == "video") {
                                currentNavRoute = "video"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Photo Gallery", Icons.Filled.Album, currentNavRoute == "photos") {
                                currentNavRoute = "photos"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Offline Share", Icons.Filled.Share, currentNavRoute == "share") {
                                currentNavRoute = "share"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Media Manager & Storage", Icons.Filled.Folder, currentNavRoute == "manager") {
                                currentNavRoute = "manager"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Secure Media Vault 🔒", Icons.Filled.Lock, currentNavRoute == "vault") {
                                currentNavRoute = "vault"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("Settings & Profile", Icons.Filled.Settings, currentNavRoute == "settings") {
                                currentNavRoute = "settings"
                                coroutineScope.launch { drawerState.close() }
                            }
                            DrawerNavItem("About App & Developer", Icons.Filled.Info, currentNavRoute == "about") {
                                currentNavRoute = "about"
                                coroutineScope.launch { drawerState.close() }
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        // Top App Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("menu_drawer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Menu",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Sync Player",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Traxxion Tech 🇿🇼",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = glassColors.glowColor
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { currentNavRoute = "vault" },
                                    modifier = Modifier.testTag("vault_shortcut_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Secure Vault",
                                        tint = if (vaultItems.isNotEmpty()) glassColors.glowColor else Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.scanDeviceMedia()
                                        }
                                    },
                                    modifier = Modifier.testTag("rescan_media_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Rescan Media",
                                        tint = glassColors.glowColor
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            // Mini Player Bar
                            if (playbackState.currentSong != null && !isFullPlayerVisible && viewingVideo == null && viewingPhoto == null) {
                                MiniPlayerBar(
                                    currentSong = playbackState.currentSong,
                                    isPlaying = playbackState.isPlaying,
                                    onPlayPause = {
                                        getService()?.togglePlayPause()
                                    },
                                    onNext = {
                                        getService()?.playNext()
                                    },
                                    onBarClick = {
                                        isFullPlayerVisible = true
                                    }
                                )
                            }

                            // Navigation Bar
                            NavigationBar(
                                containerColor = Color(0xDD0D1117),
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    selected = currentNavRoute == "home",
                                    onClick = { currentNavRoute = "home" },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = glassColors.glowColor,
                                        indicatorColor = glassColors.glowColor,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    selected = currentNavRoute == "music",
                                    onClick = { currentNavRoute = "music" },
                                    icon = { Icon(Icons.Filled.MusicNote, contentDescription = "Music") },
                                    label = { Text("Music") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = glassColors.glowColor,
                                        indicatorColor = glassColors.glowColor,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_music")
                                )
                                NavigationBarItem(
                                    selected = currentNavRoute == "video",
                                    onClick = { currentNavRoute = "video" },
                                    icon = { Icon(Icons.Filled.Videocam, contentDescription = "Video") },
                                    label = { Text("Videos") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = glassColors.glowColor,
                                        indicatorColor = glassColors.glowColor,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_video")
                                )
                                NavigationBarItem(
                                    selected = currentNavRoute == "photos",
                                    onClick = { currentNavRoute = "photos" },
                                    icon = { Icon(Icons.Filled.Album, contentDescription = "Photos") },
                                    label = { Text("Photos") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = glassColors.glowColor,
                                        indicatorColor = glassColors.glowColor,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_photos")
                                )
                                NavigationBarItem(
                                    selected = currentNavRoute == "share",
                                    onClick = { currentNavRoute = "share" },
                                    icon = { Icon(Icons.Filled.Share, contentDescription = "Share") },
                                    label = { Text("Share") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = glassColors.glowColor,
                                        indicatorColor = glassColors.glowColor,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("nav_share")
                                )
                            }
                        }
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(glassColors.backgroundGradient))
                            .padding(innerPadding)
                    ) {
                        when (currentNavRoute) {
                            "home" -> HomeScreen(
                                userProfile = userProfile,
                                songs = songs,
                                videos = videos,
                                photos = photos,
                                playlists = playlists,
                                onSongClick = { song ->
                                    getService()?.setQueue(songs, songs.indexOf(song))
                                    isFullPlayerVisible = true
                                },
                                onVideoClick = { video ->
                                    viewingVideo = video
                                },
                                onPhotoClick = { photo ->
                                    viewingPhoto = photo
                                },
                                onNavigateToSection = { route ->
                                    currentNavRoute = route
                                }
                            )

                            "music" -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Music Library 🎵",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${songs.size} tracks",
                                            fontSize = 12.sp,
                                            color = glassColors.glowColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (songs.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No songs detected on storage.\nTap the refresh icon to rescan audio tracks.",
                                                color = Color.White.copy(alpha = 0.6f),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(songs.size) { idx ->
                                                val song = songs[idx]
                                                SongRowItem(
                                                    song = song,
                                                    onClick = {
                                                        getService()?.setQueue(songs, idx)
                                                        isFullPlayerVisible = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "video" -> VideoScreen(
                                videos = videos,
                                onVideoSelected = { video -> viewingVideo = video },
                                onToggleFavorite = { id -> repository.toggleFavorite(id) },
                                onMoveToVault = { video ->
                                    repository.lockMedia(
                                        id = video.id.toString(),
                                        title = video.title,
                                        type = MediaType.VIDEO,
                                        originalPath = video.path,
                                        sizeBytes = video.sizeBytes,
                                        uri = video.uri
                                    )
                                }
                            )

                            "photos" -> PhotoGalleryScreen(
                                photos = photos,
                                onPhotoClick = { photo -> viewingPhoto = photo },
                                onToggleFavorite = { id -> repository.toggleFavorite(id) },
                                onMoveToVault = { photo ->
                                    repository.lockMedia(
                                        id = photo.id.toString(),
                                        title = photo.title,
                                        type = MediaType.PHOTO,
                                        originalPath = photo.path,
                                        sizeBytes = photo.sizeBytes,
                                        uri = photo.uri
                                    )
                                }
                            )

                            "share" -> MediaShareScreen(
                                userProfile = userProfile,
                                songs = songs,
                                videos = videos,
                                photos = photos
                            )

                            "manager" -> MediaManagerScreen(
                                songs = songs,
                                videos = videos,
                                photos = photos,
                                isScanning = isScanning,
                                onRescan = {
                                    coroutineScope.launch { repository.scanDeviceMedia() }
                                },
                                onRunSmartScan = {
                                    repository.runSmartScan()
                                }
                            )

                            "vault" -> VaultScreen(
                                vaultItems = vaultItems,
                                isConfigured = repository.isVaultConfigured(),
                                lockType = repository.getVaultLockType(),
                                isBiometricEnabled = repository.isBiometricEnabled(),
                                onSetupVault = { type, cred, bio ->
                                    repository.setupVault(type, cred, bio)
                                },
                                onVerifyCredential = { cred ->
                                    repository.verifyVaultCredential(cred)
                                },
                                onRestoreItem = { id -> repository.unlockMedia(id) },
                                onPlayAudio = { item ->
                                    item.uri?.let { uri ->
                                        val tempSong = SongItem(
                                            id = item.id.toLongOrNull() ?: 0L,
                                            title = item.title,
                                            artist = "Vault Audio",
                                            album = "Secure Vault",
                                            albumId = -1L,
                                            durationMs = 0L,
                                            uri = uri,
                                            path = item.originalPath,
                                            sizeBytes = item.sizeBytes,
                                            dateAdded = item.dateLocked
                                        )
                                        getService()?.setQueue(listOf(tempSong), 0)
                                        isFullPlayerVisible = true
                                    }
                                }
                            )

                            "settings" -> SettingsScreen(
                                userProfile = userProfile,
                                onUpdateProfile = { updated -> repository.updateUserProfile(updated) },
                                onThemeChanged = { newTheme -> onThemeChanged(newTheme) },
                                onSetNewVaultPin = { newPin -> repository.setVaultPin(newPin) },
                                onRescanLibrary = {
                                    coroutineScope.launch { repository.scanDeviceMedia() }
                                },
                                onNavigateToAbout = { currentNavRoute = "about" }
                            )

                            "about" -> AboutScreen()
                        }
                    }
                }
            }

            // Fullscreen Video Player Modal
            if (viewingVideo != null) {
                FullscreenVideoPlayer(
                    video = viewingVideo!!,
                    onClose = { viewingVideo = null }
                )
            }

            // Fullscreen Photo Viewer Modal
            if (viewingPhoto != null) {
                FullscreenPhotoViewer(
                    photo = viewingPhoto!!,
                    allPhotos = photos,
                    onClose = { viewingPhoto = null },
                    onToggleFavorite = { id -> repository.toggleFavorite(id) },
                    onMoveToVault = { photo ->
                        repository.lockMedia(
                            id = photo.id.toString(),
                            title = photo.title,
                            type = MediaType.PHOTO,
                            originalPath = photo.path,
                            sizeBytes = photo.sizeBytes,
                            uri = photo.uri
                        )
                    }
                )
            }

            // Fullscreen Music Player Modal (with Synced Lyrics & Equalizer)
            AnimatedVisibility(
                visible = isFullPlayerVisible && playbackState.currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FullMusicPlayerScreen(
                    playbackState = playbackState,
                    syncedLyrics = currentPlayingLyrics,
                    onPlayPause = { getService()?.togglePlayPause() },
                    onPrevious = { getService()?.playPrevious() },
                    onNext = { getService()?.playNext() },
                    onSeekTo = { pos -> getService()?.seekTo(pos) },
                    onToggleRepeat = { getService()?.toggleRepeat() },
                    onToggleShuffle = { getService()?.toggleShuffle() },
                    onSetSpeed = { spd -> getService()?.setPlaybackSpeed(spd) },
                    onSetSleepTimer = { mins -> getService()?.setSleepTimer(mins) },
                    onSetBassBoost = { level -> getService()?.setBassBoost(level) },
                    onSaveLyrics = { songId, lrc ->
                        repository.saveLyricsForSong(songId, lrc)
                        playbackState.currentSong?.let {
                            coroutineScope.launch {
                                currentPlayingLyrics = repository.getLyricsForSong(it)
                            }
                        }
                    },
                    onToggleFavorite = { id -> repository.toggleFavorite(id) },
                    onClose = { isFullPlayerVisible = false }
                )
            }
        }
    }
}

@Composable
fun DrawerNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) glassColors.surfaceGlassSecondary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) glassColors.glowColor else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) glassColors.glowColor else Color.White
        )
    }
}
