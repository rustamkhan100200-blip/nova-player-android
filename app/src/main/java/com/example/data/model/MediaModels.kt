package com.example.data.model

data class MediaItemData(
    val uri: String,
    val title: String,
    val artist: String = "Nova Media",
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val mimeType: String = "video/mp4",
    val isVideo: Boolean = true,
    val resolution: String = "1080p",
    val folder: String = "Internal",
    val thumbnailUri: String? = null,
    val isSample: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formattedSize(): String {
        if (sizeBytes <= 0) return "Web Stream"
        val mb = sizeBytes.toDouble() / (1024 * 1024)
        return if (mb >= 1000) {
            String.format("%.1f GB", mb / 1024)
        } else {
            String.format("%.1f MB", mb)
        }
    }

    val extension: String
        get() = title.substringAfterLast('.', "").ifBlank { if (isVideo) "mp4" else "mp3" }

    val folderName: String
        get() = folder

    val durationFormatted: String
        get() = formattedDuration()

    val sizeFormatted: String
        get() = formattedSize()

    val contentUri: String
        get() = uri
}

data class SmartTimelineMarker(
    val timestampMs: Long,
    val label: String,
    val type: MarkerType = MarkerType.BOOKMARK,
    val colorHex: Long = 0xFF00D9FF
)

enum class MarkerType {
    CHAPTER,
    BOOKMARK,
    SCENE_NOTE,
    KEYFRAME
}

data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String,
    val uri: String? = null,
    val isRtl: Boolean = false
)

data class PlaybackSettings(
    val playbackSpeed: Float = 1.0f,
    val repeatMode: Int = 0, // 0: None, 1: One, 2: All
    val isLoopingAB: Boolean = false,
    val loopPointA: Long = 0L,
    val loopPointB: Long = 0L,
    val hardwareAcceleration: Boolean = true,
    val backgroundAudio: Boolean = true,
    val subtitleOffsetMs: Long = 0L,
    val subtitleTextSizeSp: Float = 18f,
    val subtitleLanguage: String = "en"
)
