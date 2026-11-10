package com.example.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.example.MainActivity
import com.example.data.local.NovaDatabase
import com.example.data.local.PlaybackHistoryItem
import com.example.data.model.MediaItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class NovaPlaybackManager(
    private val context: Context,
    private val database: NovaDatabase
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val exoPlayer: ExoPlayer by lazy {
        // Robust HTTP Data Source with browser User-Agent and redirect follow support
        // Prevents HTTP 403 Forbidden on CDNs, cloud buckets, and media hosts
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 NovaPlayer/1.0")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .setSeekBackIncrementMs(10000L)
            .setSeekForwardIncrementMs(10000L)
            .build().apply {
                addListener(playerListener)
            }
    }

    val mediaSession: MediaSession by lazy {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sessionCallback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val availablePlayerCommands = session.player.availableCommands.buildUpon()
                    .add(Player.COMMAND_PLAY_PAUSE)
                    .add(Player.COMMAND_STOP)
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailablePlayerCommands(availablePlayerCommands)
                    .build()
            }
        }

        MediaSession.Builder(context, exoPlayer)
            .setId("NovaPlayerSession")
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(sessionCallback)
            .build().also {
                activeMediaSession = it
            }
    }

    companion object {
        var activeMediaSession: MediaSession? = null
    }

    private val _currentMedia = MutableStateFlow<MediaItemData?>(null)
    val currentMedia: StateFlow<MediaItemData?> = _currentMedia.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _queue = MutableStateFlow<List<MediaItemData>>(emptyList())
    val queue: StateFlow<List<MediaItemData>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isLoopingAB = MutableStateFlow(false)
    val isLoopingAB: StateFlow<Boolean> = _isLoopingAB.asStateFlow()

    private val _loopPointA = MutableStateFlow(0L)
    val loopPointA: StateFlow<Long> = _loopPointA.asStateFlow()

    private val _loopPointB = MutableStateFlow(0L)
    val loopPointB: StateFlow<Long> = _loopPointB.asStateFlow()

    // Audio-Only Mode for videos (Priority 7)
    private val _isAudioOnlyMode = MutableStateFlow(false)
    val isAudioOnlyMode: StateFlow<Boolean> = _isAudioOnlyMode.asStateFlow()

    // Auto Play Next preference
    private val _autoPlayNext = MutableStateFlow(true)
    val autoPlayNext: StateFlow<Boolean> = _autoPlayNext.asStateFlow()

    // Reliable volume controls
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private var previousVolume = 1.0f

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Resume position notification pill
    private val _resumedPositionMs = MutableStateFlow<Long?>(null)
    val resumedPositionMs: StateFlow<Long?> = _resumedPositionMs.asStateFlow()

    private val _videoDimensions = MutableStateFlow(Pair(1920, 1080))
    val videoDimensions: StateFlow<Pair<Int, Int>> = _videoDimensions.asStateFlow()

    private var tickerJob: Job? = null
    private var tickerCycleCount = 0

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startPositionTicker()
            } else {
                stopPositionTicker()
                saveProgressToHistory()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.value = state
            _isBuffering.value = (state == Player.STATE_BUFFERING)

            if (state == Player.STATE_READY) {
                _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                _errorMessage.value = null
            } else if (state == Player.STATE_ENDED) {
                saveProgressToHistory()
                // Priority 3: Auto Play Next video when current finishes
                if (_autoPlayNext.value && hasNext()) {
                    next()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val cause = error.cause
            val message = when {
                cause is HttpDataSource.InvalidResponseCodeException -> {
                    "Stream HTTP ${cause.responseCode} error. Access was denied or source was moved."
                }
                cause is HttpDataSource.HttpDataSourceException -> {
                    "Network error connecting to media stream. Please check connection."
                }
                else -> error.localizedMessage ?: "Playback error: codec or stream format unsupported"
            }
            _errorMessage.value = message
            _isBuffering.value = false
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (videoSize.width > 0 && videoSize.height > 0) {
                _videoDimensions.value = Pair(videoSize.width, videoSize.height)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val itemIndex = exoPlayer.currentMediaItemIndex
            if (itemIndex in _queue.value.indices) {
                _queueIndex.value = itemIndex
                _currentMedia.value = _queue.value[itemIndex]
                saveProgressToHistory()
            }
        }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val current = exoPlayer.currentPosition
                _currentPositionMs.value = current
                _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)

                // AB Repeat Check for Learning Mode
                if (_isLoopingAB.value && _loopPointB.value > _loopPointA.value) {
                    if (current >= _loopPointB.value) {
                        exoPlayer.seekTo(_loopPointA.value)
                    }
                }

                // Save playback progress periodically every ~3 seconds
                tickerCycleCount++
                if (tickerCycleCount % 15 == 0) {
                    saveProgressToHistory()
                }

                delay(200)
            }
        }
    }

    private fun stopPositionTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun startBackgroundService() {
        try {
            val intent = Intent(context, NovaMediaSessionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w("NovaPlaybackManager", "Unable to start foreground service: ${e.message}")
        }
    }

    fun stopBackgroundService() {
        try {
            val intent = Intent(context, NovaMediaSessionService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            Log.w("NovaPlaybackManager", "Unable to stop foreground service: ${e.message}")
        }
    }

    fun playMedia(media: MediaItemData, startPositionMs: Long? = null) {
        _errorMessage.value = null
        _resumedPositionMs.value = null
        _currentMedia.value = media

        // Ensure MediaSession is active for background audio and system notification controls
        val session = mediaSession

        val metadata = MediaMetadata.Builder()
            .setTitle(media.title)
            .setArtist(media.artist)
            .setDisplayTitle(media.title)
            .setArtworkUri(media.thumbnailUri?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(media.uri))
            .setMediaMetadata(metadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        if (startPositionMs != null && startPositionMs > 1000) {
            exoPlayer.seekTo(startPositionMs)
            _resumedPositionMs.value = startPositionMs
        } else {
            // Check Room history for previously played position
            scope.launch(Dispatchers.IO) {
                val history = database.mediaDao().getHistoryItem(media.uri)
                if (history != null && history.lastPositionMs > 2000 && history.lastPositionMs < (history.durationMs - 5000)) {
                    launch(Dispatchers.Main) {
                        exoPlayer.seekTo(history.lastPositionMs)
                        _resumedPositionMs.value = history.lastPositionMs
                    }
                }
            }
        }

        exoPlayer.play()
        saveProgressToHistory()

        if (!media.isVideo || _isAudioOnlyMode.value) {
            startBackgroundService()
        }
    }

    fun restartFromBeginning() {
        exoPlayer.seekTo(0L)
        _currentPositionMs.value = 0L
        _resumedPositionMs.value = null
        saveProgressToHistory()
    }

    fun clearResumeBanner() {
        _resumedPositionMs.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun playQueue(items: List<MediaItemData>, startIndex: Int = 0) {
        if (items.isEmpty()) return
        _queue.value = items
        val validIndex = startIndex.coerceIn(0, items.size - 1)
        _queueIndex.value = validIndex
        val media = items[validIndex]
        _currentMedia.value = media
        _errorMessage.value = null
        _resumedPositionMs.value = null

        val session = mediaSession

        val mediaItems = items.map { item ->
            val metadata = MediaMetadata.Builder()
                .setTitle(item.title)
                .setArtist(item.artist)
                .setDisplayTitle(item.title)
                .setArtworkUri(item.thumbnailUri?.let { Uri.parse(it) })
                .build()

            MediaItem.Builder()
                .setUri(Uri.parse(item.uri))
                .setMediaMetadata(metadata)
                .setTag(item)
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, validIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        saveProgressToHistory()

        if (!media.isVideo || _isAudioOnlyMode.value) {
            startBackgroundService()
        }
    }

    fun playVideoQueue(items: List<MediaItemData>, startIndex: Int = 0) {
        playQueue(items, startIndex)
    }

    fun hasNext(): Boolean {
        val list = _queue.value
        return list.isNotEmpty() && _queueIndex.value < list.size - 1
    }

    fun hasPrevious(): Boolean {
        val list = _queue.value
        return list.isNotEmpty() && _queueIndex.value > 0
    }

    fun next() {
        val list = _queue.value
        if (list.isNotEmpty() && hasNext()) {
            val nextIdx = _queueIndex.value + 1
            _queueIndex.value = nextIdx
            if (exoPlayer.mediaItemCount > nextIdx) {
                exoPlayer.seekToDefaultPosition(nextIdx)
                exoPlayer.play()
            } else {
                playMedia(list[nextIdx])
            }
        }
    }

    fun previous() {
        // Industry-standard UX: If user is more than 3 seconds into media, rewind to start;
        // if within first 3 seconds, jump to previous item in queue.
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }
        val list = _queue.value
        if (list.isNotEmpty() && hasPrevious()) {
            val prevIdx = _queueIndex.value - 1
            _queueIndex.value = prevIdx
            if (exoPlayer.mediaItemCount > prevIdx) {
                exoPlayer.seekToDefaultPosition(prevIdx)
                exoPlayer.play()
            } else {
                playMedia(list[prevIdx])
            }
        } else {
            seekTo(0L)
        }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _autoPlayNext.value = enabled
    }

    fun setAudioOnlyMode(enabled: Boolean) {
        _isAudioOnlyMode.value = enabled
        if (enabled && exoPlayer.isPlaying) {
            startBackgroundService()
        }
    }

    /**
     * Cleanly stops video playback when leaving player (Priority 1 & 2).
     * If Audio-Only mode is enabled, allows background audio to keep playing with OS media notification.
     * Otherwise, stops video playback immediately, saves position, stops background service, and stops audio.
     */
    fun stopVideoPlayback(forceStop: Boolean = false) {
        saveProgressToHistory()
        if (!_isAudioOnlyMode.value || forceStop) {
            exoPlayer.pause()
            _isPlaying.value = false
            stopPositionTicker()
            stopBackgroundService()
            if (forceStop) {
                exoPlayer.stop()
                _currentMedia.value = null
            }
        } else {
            // Audio-Only mode is enabled: keep background audio playing with the system notification
            startBackgroundService()
        }
    }

    private var lastSeekPreviewTime = 0L

    /**
     * Throttled live preview seek during horizontal touch drag.
     * Updates current position state immediately and calls exoPlayer.seekTo
     * every ~75ms to smoothly update the video frame without overloading decoder.
     */
    fun seekPreview(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        _currentPositionMs.value = target
        val now = System.currentTimeMillis()
        if (now - lastSeekPreviewTime >= 75L) {
            lastSeekPreviewTime = now
            exoPlayer.seekTo(target)
        }
    }

    /**
     * Commits seeking when finger is lifted.
     * Genuinely updates player position, saves to history, and resumes playback
     * only if it was playing before drag started.
     */
    fun commitSeek(positionMs: Long, resumePlayback: Boolean) {
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
        _currentPositionMs.value = target
        saveProgressToHistory()
        if (resumePlayback) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
            _isPlaying.value = true
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
        exoPlayer.seekTo(target)
        _currentPositionMs.value = target
        saveProgressToHistory()
    }

    /**
     * Frame-by-frame stepping for paused video (Priority 15).
     * Steps approximately 42ms (~1 frame at 24fps) forward or backward.
     */
    fun stepFrame(forward: Boolean) {
        if (!exoPlayer.isPlaying) {
            val stepMs = 42L
            val current = exoPlayer.currentPosition
            val target = if (forward) current + stepMs else (current - stepMs).coerceAtLeast(0L)
            seekTo(target)
        }
    }

    fun seekForward(ms: Long = 10000L) {
        val current = exoPlayer.currentPosition
        val duration = exoPlayer.duration.coerceAtLeast(0L)
        val target = if (duration > 0) (current + ms).coerceAtMost(duration) else current + ms
        seekTo(target)
    }

    fun seekBackward(ms: Long = 10000L) {
        val current = exoPlayer.currentPosition
        val target = (current - ms).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    fun setLoopAB(pointA: Long, pointB: Long) {
        _loopPointA.value = pointA
        _loopPointB.value = pointB
        _isLoopingAB.value = true
        exoPlayer.seekTo(pointA)
    }

    fun clearLoopAB() {
        _isLoopingAB.value = false
        _loopPointA.value = 0L
        _loopPointB.value = 0L
    }

    // Reliable Volume Control
    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        exoPlayer.volume = clamped
        _isMuted.value = (clamped == 0f)
        if (clamped > 0f) {
            previousVolume = clamped
        }
    }

    fun adjustVolumeBy(delta: Float) {
        setVolume(_volume.value + delta)
    }

    fun toggleMute() {
        if (_isMuted.value) {
            setVolume(if (previousVolume > 0f) previousVolume else 1.0f)
            _isMuted.value = false
        } else {
            previousVolume = _volume.value
            setVolume(0f)
            _isMuted.value = true
        }
    }

    fun saveProgressToHistory() {
        val media = _currentMedia.value ?: return
        val pos = exoPlayer.currentPosition
        val dur = exoPlayer.duration.coerceAtLeast(0L)
        scope.launch(Dispatchers.IO) {
            database.mediaDao().insertOrUpdateHistory(
                PlaybackHistoryItem(
                    mediaUri = media.uri,
                    title = media.title,
                    artist = media.artist,
                    durationMs = if (dur > 0) dur else media.durationMs,
                    lastPositionMs = pos,
                    timestamp = System.currentTimeMillis(),
                    isVideo = media.isVideo,
                    thumbnailUri = media.thumbnailUri
                )
            )
        }
    }

    fun release() {
        stopPositionTicker()
        saveProgressToHistory()
        stopBackgroundService()
        mediaSession.release()
        activeMediaSession = null
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
