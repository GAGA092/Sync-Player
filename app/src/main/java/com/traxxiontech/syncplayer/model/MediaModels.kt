package com.traxxiontech.syncplayer.model

import android.net.Uri

enum class MediaType {
    AUDIO, VIDEO, PHOTO
}

data class SongItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long = 0L,
    val durationMs: Long = 0L,
    val uri: Uri,
    val path: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val isFavorite: Boolean = false,
    val hasLyrics: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSec = durationMs / 1000
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val artworkUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")
}

data class VideoItem(
    val id: Long,
    val title: String,
    val durationMs: Long = 0L,
    val resolution: String = "1080p",
    val uri: Uri,
    val path: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val isFavorite: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSec = durationMs / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }

    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                "%.2f GB".format(mb / 1024.0)
            } else {
                "%.1f MB".format(mb)
            }
        }
}

data class PhotoItem(
    val id: Long,
    val title: String,
    val uri: Uri,
    val path: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val albumName: String = "Gallery",
    val isFavorite: Boolean = false
) {
    val sizeFormatted: String
        get() = "%.1f MB".format(sizeBytes / (1024.0 * 1024.0))
}

data class VaultMediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val originalPath: String,
    val dateLocked: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0L,
    val uri: Uri? = null
)

data class Playlist(
    val id: String,
    val name: String,
    val createdDate: Long = System.currentTimeMillis(),
    val songIds: List<Long> = emptyList(),
    val description: String = ""
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class SyncedLyrics(
    val songId: Long,
    val songTitle: String,
    val lines: List<LyricLine> = emptyList(),
    val isTimecoded: Boolean = true
)

enum class ThemeType(val displayName: String, val isDark: Boolean = true) {
    SYSTEM("System Default", isDark = true),
    LIGHT("Pure Light", isDark = false),
    DARK("True Dark", isDark = true),
    AMOLED("OLED Pitch Black", isDark = true),
    NEON("Neon Glow", isDark = true),
    SPACE("Cosmic Space", isDark = true),
    OCEAN("Deep Ocean", isDark = true),
    SUNSET("Amber Sunset", isDark = true),
    FOREST("Emerald Forest", isDark = true),
    MIDNIGHT("Midnight Blue", isDark = true),
    AURORA("Borealis Aurora", isDark = true),
    GALAXY("Nebula Galaxy", isDark = true),
    CYBERPUNK("Cyberpunk 2077", isDark = true),
    ICE("Glacial Ice", isDark = false),
    GOLD("Imperial Gold", isDark = true)
}

data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val avatarPreset: Int = 0,
    val avatarUriString: String? = null,
    val favoriteTheme: ThemeType = ThemeType.NEON,
    val totalSongsPlayed: Int = 0,
    val totalVideosWatched: Int = 0,
    val totalListeningMinutes: Long = 0L,
    val totalWatchingMinutes: Long = 0L
)

data class SmartScanReport(
    val totalFilesScanned: Int = 0,
    val duplicateFiles: List<String> = emptyList(),
    val corruptedFiles: List<String> = emptyList(),
    val missingArtworkCount: Int = 0,
    val potentialSavedBytes: Long = 0L
)
