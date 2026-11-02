package com.traxxiontech.syncplayer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val albumId: Long = 0L,
    val durationMs: Long = 0L,
    val uriString: String,
    val path: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val mediaType: String, // "AUDIO", "VIDEO", "PHOTO"
    val isFavorite: Boolean = false,
    val hasLyrics: Boolean = false,
    val resolution: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val bucketName: String = "",
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L
)
