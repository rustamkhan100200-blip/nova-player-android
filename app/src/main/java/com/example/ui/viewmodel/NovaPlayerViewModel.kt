package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BookmarkItem
import com.example.data.local.FavoriteItem
import com.example.data.local.NovaDatabase
import com.example.data.local.PlaybackHistoryItem
import com.example.data.local.SceneNoteItem
import com.example.data.local.StudyCollection
import com.example.data.model.MediaItemData
import com.example.data.model.PlaybackSettings
import com.example.data.model.SmartTimelineMarker
import com.example.data.model.SubtitleTrack
import com.example.data.repository.FileItem
import com.example.data.repository.MediaRepository
import com.example.player.NovaPlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.local.VideoPlaylistEntity
import com.example.data.local.PlaylistItemEntity
import com.example.data.local.RecycleBinItem
import com.example.data.local.UserPreference

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object VideoLibrary : Screen("video_library")
    data object FullscreenVideo : Screen("fullscreen_video")
    data object MusicLibrary : Screen("music_library")
    data object MusicPlayer : Screen("music_player")
    data object SceneNotebook : Screen("scene_notebook")
    data object SmartTimeline : Screen("smart_timeline")
    data object SubtitleStudio : Screen("subtitle_studio")
    data object LearningMode : Screen("learning_mode")
    data object FileManager : Screen("file_manager")
    data object Tools : Screen("tools")
    data object AiStudio : Screen("ai_studio")
    data object Settings : Screen("settings")
    data object Me : Screen("me")
}

class NovaPlayerViewModel(application: Application) : AndroidViewModel(application) {

    val database = NovaDatabase.getDatabase(application)
    val mediaRepository = MediaRepository(application)
    val playbackManager = NovaPlaybackManager(application, database)

    // Current Screen defaults to VideoLibrary (Requirement 14)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.VideoLibrary)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation Stack for robust back handling
    private val screenStack = mutableListOf<Screen>(Screen.VideoLibrary)

    // Originating screen for back navigation from video player (Requirement 1)
    private var videoOriginatingScreen: Screen = Screen.VideoLibrary

    // Media Libraries
    private val _videoList = MutableStateFlow<List<MediaItemData>>(emptyList())
    val videoList: StateFlow<List<MediaItemData>> = _videoList.asStateFlow()

    private val _audioList = MutableStateFlow<List<MediaItemData>>(emptyList())
    val audioList: StateFlow<List<MediaItemData>> = _audioList.asStateFlow()

    private val _isScanningMedia = MutableStateFlow(false)
    val isScanningMedia: StateFlow<Boolean> = _isScanningMedia.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortOrder = MutableStateFlow("Recent") // Recent, Name, Duration, Size
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    // Grid vs List View toggles
    private val _isVideoGridMode = MutableStateFlow(true)
    val isVideoGridMode: StateFlow<Boolean> = _isVideoGridMode.asStateFlow()

    private val _isMusicGridMode = MutableStateFlow(false)
    val isMusicGridMode: StateFlow<Boolean> = _isMusicGridMode.asStateFlow()

    // Database flows
    val history: StateFlow<List<PlaybackHistoryItem>> = database.mediaDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteItem>> = database.mediaDao().getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<VideoPlaylistEntity>> = database.mediaDao().getAllVideoPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recycleBin: StateFlow<List<RecycleBinItem>> = database.mediaDao().getAllRecycleBin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sceneNotes: StateFlow<List<SceneNoteItem>> = database.mediaDao().getAllSceneNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkItem>> = database.mediaDao().getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyCollections: StateFlow<List<StudyCollection>> = database.mediaDao().getAllStudyCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Appearance & Library Controls (Me Screen & Preferences)
    private val _appTheme = MutableStateFlow("Dark") // Dark, Light, System
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _accentColor = MutableStateFlow(0xFF00D9FF) // Cyan, Violet, Emerald, Sunset
    val accentColor: StateFlow<Long> = _accentColor.asStateFlow()

    private val _libraryLayout = MutableStateFlow("Comfortable") // Compact, Comfortable, Large
    val libraryLayout: StateFlow<String> = _libraryLayout.asStateFlow()

    val autoPlayNext: StateFlow<Boolean> = playbackManager.autoPlayNext

    // File Manager
    private val _currentDirPath = MutableStateFlow("")
    val currentDirPath: StateFlow<String> = _currentDirPath.asStateFlow()

    private val _directoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val directoryFiles: StateFlow<List<FileItem>> = _directoryFiles.asStateFlow()

    // Playback Settings & Controls
    private val _settings = MutableStateFlow(PlaybackSettings())
    val settings: StateFlow<PlaybackSettings> = _settings.asStateFlow()

    private val _playerAspectRatio = MutableStateFlow("FIT") // FIT, FILL, 16:9, ZOOM
    val playerAspectRatio: StateFlow<String> = _playerAspectRatio.asStateFlow()

    private val _isControlsLocked = MutableStateFlow(false)
    val isControlsLocked: StateFlow<Boolean> = _isControlsLocked.asStateFlow()

    // Subtitles: Initially supported: Pashto, Dari, English (Requirement 11 - OFF by default)
    val supportedLanguages = listOf(
        SubtitleTrack("en", "English", "en", null, isRtl = false),
        SubtitleTrack("ps", "پښتو (Pashto) [RTL]", "ps", null, isRtl = true),
        SubtitleTrack("fa", "دری (Dari) [RTL]", "fa", null, isRtl = true)
    )
    val availableSubtitles: List<SubtitleTrack> get() = supportedLanguages

    private val _subtitlesEnabled = MutableStateFlow(false)
    val subtitlesEnabled: StateFlow<Boolean> = _subtitlesEnabled.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow<SubtitleTrack?>(null) // OFF by default!
    val selectedSubtitle: StateFlow<SubtitleTrack?> = _selectedSubtitle.asStateFlow()

    private val _subtitleDelayMs = MutableStateFlow(0L)
    val subtitleDelayMs: StateFlow<Long> = _subtitleDelayMs.asStateFlow()

    private val _subtitleTextSizeSp = MutableStateFlow(18f)
    val subtitleTextSizeSp: StateFlow<Float> = _subtitleTextSizeSp.asStateFlow()

    private val _subtitleTextColor = MutableStateFlow(0xFFFFFFFF)
    val subtitleTextColor: StateFlow<Long> = _subtitleTextColor.asStateFlow()

    private val _subtitleBackgroundColor = MutableStateFlow(0x99000000)
    val subtitleBackgroundColor: StateFlow<Long> = _subtitleBackgroundColor.asStateFlow()

    // AI Studio Status
    private val _aiModelStatus = MutableStateFlow("On-Device Engine Standby")
    val aiModelStatus: StateFlow<String> = _aiModelStatus.asStateFlow()

    private val _aiSearchPrompt = MutableStateFlow("")
    val aiSearchPrompt: StateFlow<String> = _aiSearchPrompt.asStateFlow()

    init {
        loadMedia()
        loadDirectory("")
        mediaRepository.registerContentObserver {
            loadMedia()
        }

        // Restore persistent preferences on startup (Requirement 4)
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().getPreference("app_theme")?.let {
                _appTheme.value = it
            }
            database.mediaDao().getPreference("accent_color")?.toLongOrNull()?.let {
                _accentColor.value = it
            }
            database.mediaDao().getPreference("auto_play_next")?.let {
                playbackManager.setAutoPlayNext(it.toBoolean())
            }
            database.mediaDao().getPreference("library_layout")?.let {
                _libraryLayout.value = it
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return onBackPressed()
    }

    /**
     * Unified back navigation satisfying Priority 1 & 2:
     * - Stops video playback and audio when leaving player (unless Audio-Only mode is enabled)
     * - Returns to exact originating screen (Videos library, folder, search, favorites, or playlist)
     * - Preserves originating screen state
     */
    fun onBackPressed(): Boolean {
        if (_currentScreen.value == Screen.FullscreenVideo) {
            playbackManager.stopVideoPlayback()
            val target = videoOriginatingScreen
            _currentScreen.value = target
            screenStack.removeAll { it == Screen.FullscreenVideo }
            return true
        }

        if (_currentScreen.value == Screen.MusicPlayer) {
            if (screenStack.isNotEmpty()) {
                val prev = screenStack.removeAt(screenStack.size - 1)
                _currentScreen.value = prev
                return true
            }
            _currentScreen.value = Screen.MusicLibrary
            return true
        }

        if (screenStack.isNotEmpty()) {
            val prev = screenStack.removeAt(screenStack.size - 1)
            if (prev != _currentScreen.value) {
                _currentScreen.value = prev
                return true
            }
        }

        if (_currentScreen.value != Screen.VideoLibrary) {
            _currentScreen.value = Screen.VideoLibrary
            return true
        }

        return false
    }

    fun toggleAudioOnlyMode() {
        val next = !playbackManager.isAudioOnlyMode.value
        playbackManager.setAudioOnlyMode(next)
    }

    fun setSubtitleStyle(sizeSp: Float, textColorHex: Long, bgHex: Long) {
        _subtitleTextSizeSp.value = sizeSp
        _subtitleTextColor.value = textColorHex
        _subtitleBackgroundColor.value = bgHex
    }

    fun loadMedia() {
        viewModelScope.launch {
            _isScanningMedia.value = true
            _videoList.value = mediaRepository.queryLocalVideos()
            _audioList.value = mediaRepository.queryLocalAudio()
            _isScanningMedia.value = false
        }
    }

    fun rescanMedia() {
        loadMedia()
    }

    fun toggleVideoGridMode() {
        _isVideoGridMode.value = !_isVideoGridMode.value
    }

    fun toggleMusicGridMode() {
        _isMusicGridMode.value = !_isMusicGridMode.value
    }

    fun toggleFavorite(media: MediaItemData) {
        toggleFavorite(media.uri, media.title, media.isVideo)
    }

    fun toggleFavorite(uri: String, title: String, isVideo: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = database.mediaDao().isFavorite(uri)
            if (isFav) {
                database.mediaDao().deleteFavorite(uri)
            } else {
                database.mediaDao().insertFavorite(
                    FavoriteItem(
                        mediaUri = uri,
                        title = title,
                        isVideo = isVideo
                    )
                )
            }
        }
    }

    fun isFavorite(uri: String): Boolean {
        return favorites.value.any { it.mediaUri == uri }
    }

    fun playVideo(item: MediaItemData, startPositionMs: Long? = null, customQueue: List<MediaItemData>? = null) {
        videoOriginatingScreen = _currentScreen.value
        val q = customQueue ?: if (_videoList.value.any { it.uri == item.uri }) _videoList.value else listOf(item)
        val idx = q.indexOfFirst { it.uri == item.uri }.coerceAtLeast(0)
        playbackManager.playVideoQueue(q, idx)
        if (startPositionMs != null && startPositionMs > 1000) {
            playbackManager.seekTo(startPositionMs)
        }
        navigateTo(Screen.FullscreenVideo)
    }

    fun playVideoList(items: List<MediaItemData>, startIndex: Int = 0) {
        if (items.isEmpty()) return
        videoOriginatingScreen = _currentScreen.value
        playbackManager.playVideoQueue(items, startIndex.coerceIn(0, items.size - 1))
        navigateTo(Screen.FullscreenVideo)
    }

    fun playAudio(item: MediaItemData) {
        playbackManager.playQueue(_audioList.value, _audioList.value.indexOf(item).coerceAtLeast(0))
        navigateTo(Screen.MusicPlayer)
    }

    fun openImportedUri(uri: Uri) {
        viewModelScope.launch {
            val item = mediaRepository.resolveMediaFromUri(uri)
            if (item.isVideo) {
                _videoList.value = listOf(item) + _videoList.value.filter { it.uri != item.uri }
                playVideo(item)
            } else {
                _audioList.value = listOf(item) + _audioList.value.filter { it.uri != item.uri }
                playAudio(item)
            }
        }
    }

    fun setAspectRatio(ratio: String) {
        _playerAspectRatio.value = ratio
    }

    fun toggleControlsLock() {
        _isControlsLocked.value = !_isControlsLocked.value
    }

    fun setSpeed(speed: Float) {
        playbackManager.setSpeed(speed)
        _settings.value = _settings.value.copy(playbackSpeed = speed)
    }

    fun setSubtitle(track: SubtitleTrack?) {
        _selectedSubtitle.value = track
        _subtitlesEnabled.value = (track != null)
    }

    fun toggleSubtitles(enabled: Boolean) {
        _subtitlesEnabled.value = enabled
        if (!enabled) {
            _selectedSubtitle.value = null
        } else if (_selectedSubtitle.value == null) {
            _selectedSubtitle.value = supportedLanguages.firstOrNull()
        }
    }

    fun setSubtitleTextSize(sp: Float) {
        _subtitleTextSizeSp.value = sp
    }

    fun setSubtitleTextColor(colorHex: Long) {
        _subtitleTextColor.value = colorHex
    }

    fun setSubtitleBackgroundColor(colorHex: Long) {
        _subtitleBackgroundColor.value = colorHex
    }

    fun adjustSubtitleOffset(deltaMs: Long) {
        _subtitleDelayMs.value += deltaMs
    }

    fun addBookmarkAtCurrentPosition(label: String) {
        val currentMedia = playbackManager.currentMedia.value ?: return
        val currentPos = playbackManager.currentPositionMs.value
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertBookmark(
                BookmarkItem(
                    mediaUri = currentMedia.uri,
                    videoTitle = currentMedia.title,
                    timestampMs = currentPos,
                    label = if (label.isBlank()) "Bookmark at ${formatMs(currentPos)}" else label
                )
            )
        }
    }

    fun addSceneNote(noteText: String, tag: String = "Important", category: String = "Study") {
        val currentMedia = playbackManager.currentMedia.value ?: return
        val currentPos = playbackManager.currentPositionMs.value
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertSceneNote(
                SceneNoteItem(
                    mediaUri = currentMedia.uri,
                    videoTitle = currentMedia.title,
                    timestampMs = currentPos,
                    noteText = noteText,
                    tag = tag,
                    category = category
                )
            )
        }
    }

    fun createStudyCollection(title: String, desc: String, startMs: Long, endMs: Long) {
        val currentMedia = playbackManager.currentMedia.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertStudyCollection(
                StudyCollection(
                    title = title,
                    description = desc,
                    mediaUri = currentMedia.uri,
                    loopStartMs = startMs,
                    loopEndMs = endMs
                )
            )
        }
    }

    fun deleteSceneNote(note: SceneNoteItem) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deleteSceneNote(note)
        }
    }

    fun deleteBookmark(bookmark: BookmarkItem) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deleteBookmark(bookmark)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().clearAllHistory()
        }
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _currentDirPath.value = path
            _directoryFiles.value = mediaRepository.getDirectoryFiles(path)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun setAiSearchPrompt(prompt: String) {
        _aiSearchPrompt.value = prompt
    }

    // App Theme & Appearance Preferences
    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().setPreference(UserPreference("app_theme", theme))
        }
    }

    fun setAccentColor(colorHex: Long) {
        _accentColor.value = colorHex
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().setPreference(UserPreference("accent_color", colorHex.toString()))
        }
    }

    fun setLibraryLayout(layout: String) {
        _libraryLayout.value = layout
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().setPreference(UserPreference("library_layout", layout))
        }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        playbackManager.setAutoPlayNext(enabled)
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().setPreference(UserPreference("auto_play_next", enabled.toString()))
        }
    }

    // Video Playlists Management
    fun createVideoPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertVideoPlaylist(VideoPlaylistEntity(name = name.trim()))
        }
    }

    fun renameVideoPlaylist(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().updatePlaylistName(id, newName.trim())
        }
    }

    fun deleteVideoPlaylist(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deleteVideoPlaylist(id)
            database.mediaDao().clearPlaylistItems(id)
        }
    }

    fun addVideoToPlaylist(playlistId: Long, video: MediaItemData) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertPlaylistItem(
                PlaylistItemEntity(
                    playlistId = playlistId,
                    mediaUri = video.uri,
                    title = video.title,
                    durationMs = video.durationMs,
                    isVideo = video.isVideo
                )
            )
        }
    }

    fun removePlaylistItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deletePlaylistItem(id)
        }
    }

    fun getPlaylistItems(playlistId: Long): kotlinx.coroutines.flow.Flow<List<PlaylistItemEntity>> {
        return database.mediaDao().getItemsForPlaylist(playlistId)
    }

    // Recycle Bin Management (Safe Deletion & Restore)
    fun moveToRecycleBin(media: MediaItemData) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().insertRecycleBin(
                RecycleBinItem(
                    mediaUri = media.uri,
                    title = media.title,
                    isVideo = media.isVideo,
                    durationMs = media.durationMs,
                    originalPath = media.folder,
                    sizeBytes = media.sizeBytes
                )
            )
            // Remove from active in-memory list
            if (media.isVideo) {
                _videoList.value = _videoList.value.filter { it.uri != media.uri }
            } else {
                _audioList.value = _audioList.value.filter { it.uri != media.uri }
            }
        }
    }

    fun restoreFromRecycleBin(item: RecycleBinItem) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deleteFromRecycleBin(item.mediaUri)
            // Trigger rescan to re-include file
            loadMedia()
        }
    }

    fun permanentlyDeleteTrash(item: RecycleBinItem) {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().deleteFromRecycleBin(item.mediaUri)
        }
    }

    fun clearAllRecycleBin() {
        viewModelScope.launch(Dispatchers.IO) {
            database.mediaDao().clearRecycleBin()
        }
    }

    fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%02d:%02d", m, s)
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format("%.2f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRepository.unregisterContentObserver()
        playbackManager.release()
    }
}
