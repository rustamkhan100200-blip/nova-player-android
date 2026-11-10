package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryItem(
    @PrimaryKey
    val mediaUri: String,
    val title: String,
    val artist: String = "",
    val durationMs: Long = 0L,
    val lastPositionMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val isVideo: Boolean = true,
    val thumbnailUri: String? = null
)

@Entity(tableName = "scene_notes")
data class SceneNoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaUri: String,
    val videoTitle: String,
    val timestampMs: Long,
    val noteText: String,
    val tag: String = "Important",
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val colorHex: Long = 0xFF8257FF
)

@Entity(tableName = "bookmarks")
data class BookmarkItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaUri: String,
    val videoTitle: String,
    val timestampMs: Long,
    val label: String,
    val chapter: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_collections")
data class StudyCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val mediaUri: String,
    val loopStartMs: Long = 0L,
    val loopEndMs: Long = 0L,
    val notesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey
    val mediaUri: String,
    val title: String,
    val isVideo: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_playlists")
data class VideoPlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val mediaUri: String,
    val title: String,
    val durationMs: Long = 0L,
    val isVideo: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recycle_bin")
data class RecycleBinItem(
    @PrimaryKey
    val mediaUri: String,
    val title: String,
    val isVideo: Boolean = true,
    val durationMs: Long = 0L,
    val originalPath: String = "",
    val sizeBytes: Long = 0L,
    val deletedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val key: String,
    val value: String
)
