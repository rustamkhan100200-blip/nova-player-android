package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    // Playback History
    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history WHERE isVideo = 1 ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentVideos(limit: Int = 10): Flow<List<PlaybackHistoryItem>>

    @Query("SELECT * FROM playback_history WHERE mediaUri = :uri LIMIT 1")
    suspend fun getHistoryItem(uri: String): PlaybackHistoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(item: PlaybackHistoryItem)

    @Query("DELETE FROM playback_history WHERE mediaUri = :uri")
    suspend fun deleteHistory(uri: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearAllHistory()

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteItem)

    @Query("DELETE FROM favorites WHERE mediaUri = :uri")
    suspend fun deleteFavorite(uri: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaUri = :uri)")
    suspend fun isFavorite(uri: String): Boolean

    // Scene Notes
    @Query("SELECT * FROM scene_notes ORDER BY timestampMs ASC")
    fun getAllSceneNotes(): Flow<List<SceneNoteItem>>

    @Query("SELECT * FROM scene_notes WHERE mediaUri = :uri ORDER BY timestampMs ASC")
    fun getNotesForMedia(uri: String): Flow<List<SceneNoteItem>>

    @Query("SELECT * FROM scene_notes WHERE noteText LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotes(query: String): Flow<List<SceneNoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSceneNote(note: SceneNoteItem): Long

    @Delete
    suspend fun deleteSceneNote(note: SceneNoteItem)

    // Bookmarks & Chapters
    @Query("SELECT * FROM bookmarks WHERE mediaUri = :uri ORDER BY timestampMs ASC")
    fun getBookmarksForMedia(uri: String): Flow<List<BookmarkItem>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkItem): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkItem)

    // Study Collections (Learning Mode)
    @Query("SELECT * FROM study_collections ORDER BY createdAt DESC")
    fun getAllStudyCollections(): Flow<List<StudyCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyCollection(collection: StudyCollection): Long

    @Delete
    suspend fun deleteStudyCollection(collection: StudyCollection)

    // Video Playlists
    @Query("SELECT * FROM video_playlists ORDER BY createdAt DESC")
    fun getAllVideoPlaylists(): Flow<List<VideoPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoPlaylist(playlist: VideoPlaylistEntity): Long

    @Query("UPDATE video_playlists SET name = :newName WHERE id = :id")
    suspend fun updatePlaylistName(id: Long, newName: String)

    @Query("DELETE FROM video_playlists WHERE id = :id")
    suspend fun deleteVideoPlaylist(id: Long)

    // Playlist Items
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY addedAt ASC")
    fun getItemsForPlaylist(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity): Long

    @Query("DELETE FROM playlist_items WHERE id = :id")
    suspend fun deletePlaylistItem(id: Long)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylistItems(playlistId: Long)

    // Recycle Bin (Safe Deletion & Restore)
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAllRecycleBin(): Flow<List<RecycleBinItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycleBin(item: RecycleBinItem)

    @Query("DELETE FROM recycle_bin WHERE mediaUri = :uri")
    suspend fun deleteFromRecycleBin(uri: String)

    @Query("DELETE FROM recycle_bin")
    suspend fun clearRecycleBin()

    // User Preferences (Theme, Sort, Auto-Play)
    @Query("SELECT value FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): String?

    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: UserPreference)
}
