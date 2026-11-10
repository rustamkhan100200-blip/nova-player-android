package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.database.ContentObserver
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.example.data.model.MediaItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

    private var videoObserver: ContentObserver? = null
    private var audioObserver: ContentObserver? = null

    fun registerContentObserver(onChange: () -> Unit) {
        try {
            val handler = Handler(Looper.getMainLooper())
            videoObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    onChange()
                }
            }
            audioObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    onChange()
                }
            }
            context.contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                videoObserver!!
            )
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                audioObserver!!
            )
        } catch (_: Exception) {}
    }

    fun unregisterContentObserver() {
        try {
            videoObserver?.let { context.contentResolver.unregisterContentObserver(it) }
            audioObserver?.let { context.contentResolver.unregisterContentObserver(it) }
        } catch (_: Exception) {}
    }

    // Default open-source demo media with verified accessible HTTP streaming
    val sampleVideos = listOf(
        MediaItemData(
            uri = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            title = "Big Buck Bunny (Animation)",
            artist = "Blender Open Movie",
            durationMs = 596000L,
            sizeBytes = 64657027L,
            mimeType = "video/mp4",
            isVideo = true,
            resolution = "1080p 60fps",
            folder = "Animation Showcase",
            thumbnailUri = null,
            isSample = true
        ),
        MediaItemData(
            uri = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_5MB.mp4",
            title = "Big Buck Bunny 1080p (HD Cinema)",
            artist = "Peach Open Movie",
            durationMs = 10000L,
            sizeBytes = 5238714L,
            mimeType = "video/mp4",
            isVideo = true,
            resolution = "1080p Cinema",
            folder = "Sci-Fi Showcase",
            thumbnailUri = null,
            isSample = true
        ),
        MediaItemData(
            uri = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
            title = "Cosmic Bloom (Macro HDR)",
            artist = "Mozilla Open Media",
            durationMs = 5000L,
            sizeBytes = 1128375L,
            mimeType = "video/mp4",
            isVideo = true,
            resolution = "HD HDR",
            folder = "Nature Showcase",
            thumbnailUri = null,
            isSample = true
        ),
        MediaItemData(
            uri = "https://filesamples.com/samples/video/mp4/sample_960x540.mp4",
            title = "Neon Wildlife Sequence",
            artist = "Open Video Archive",
            durationMs = 30000L,
            sizeBytes = 5400000L,
            mimeType = "video/mp4",
            isVideo = true,
            resolution = "720p HD",
            folder = "Showcase",
            thumbnailUri = null,
            isSample = true
        )
    )

    val sampleAudio = listOf(
        MediaItemData(
            uri = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            title = "Nova Cyber Odyssey",
            artist = "Google ExoPlayer Audio",
            durationMs = 60000L,
            sizeBytes = 965128L,
            mimeType = "audio/mp3",
            isVideo = false,
            resolution = "320 kbps",
            folder = "Cyber Synth",
            thumbnailUri = null,
            isSample = true
        ),
        MediaItemData(
            uri = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3",
            title = "Jazz In Paris (High Fidelity)",
            artist = "Media Soundscape Studio",
            durationMs = 108000L,
            sizeBytes = 4110593L,
            mimeType = "audio/mp3",
            isVideo = false,
            resolution = "320 kbps",
            folder = "Jazz & Ambient",
            thumbnailUri = null,
            isSample = true
        ),
        MediaItemData(
            uri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            title = "Electric Horizon (Synthwave)",
            artist = "Vektor Neon",
            durationMs = 372000L,
            sizeBytes = 6120000L,
            mimeType = "audio/mp3",
            isVideo = false,
            resolution = "320 kbps",
            folder = "Synthwave",
            thumbnailUri = null,
            isSample = true
        )
    )

    suspend fun queryLocalVideos(): List<MediaItemData> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItemData>()
        try {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val dispNameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = if (idCol != -1) cursor.getLong(idCol) else continue
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val title = (if (dispNameCol != -1) cursor.getString(dispNameCol) else null)
                        ?: (if (titleCol != -1) cursor.getString(titleCol) else null)
                        ?: "Video $id"
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                    val bucket = if (bucketCol != -1) cursor.getString(bucketCol) ?: "Device Videos" else "Device Videos"

                    list.add(
                        MediaItemData(
                            uri = contentUri.toString(),
                            title = title,
                            artist = bucket,
                            durationMs = duration,
                            sizeBytes = size,
                            mimeType = mime,
                            isVideo = true,
                            resolution = "HD Video",
                            folder = bucket,
                            thumbnailUri = contentUri.toString(),
                            isSample = false
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Permission not granted or query issue
        }

        // Also perform direct file system discovery on public media directories
        try {
            val publicVideoDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ).filter { it != null && it.exists() && it.canRead() }

            for (dir in publicVideoDirs) {
                dir.walkTopDown().maxDepth(3).filter { file ->
                    file.isFile && isVideoFile(file.name)
                }.forEach { file ->
                    val fileUri = Uri.fromFile(file).toString()
                    if (list.none { it.uri == fileUri }) {
                        list.add(
                            MediaItemData(
                                uri = fileUri,
                                title = file.name,
                                artist = file.parentFile?.name ?: "Storage",
                                durationMs = 0L,
                                sizeBytes = file.length(),
                                mimeType = getMimeType(file.name),
                                isVideo = true,
                                resolution = "Local Video",
                                folder = file.parentFile?.name ?: "Storage",
                                thumbnailUri = fileUri,
                                isSample = false
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // Prioritize discovered local device media files; provide samples only if storage has no media yet
        if (list.isNotEmpty()) {
            list.distinctBy { it.uri }
        } else {
            sampleVideos
        }
    }

    suspend fun queryLocalAudio(): List<MediaItemData> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItemData>()
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val dispNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)

                while (cursor.moveToNext()) {
                    val id = if (idCol != -1) cursor.getLong(idCol) else continue
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val title = (if (titleCol != -1) cursor.getString(titleCol) else null)
                        ?: (if (dispNameCol != -1) cursor.getString(dispNameCol) else null)
                        ?: "Track $id"
                    val artist = (if (artistCol != -1) cursor.getString(artistCol) else null) ?: "Unknown Artist"
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val album = (if (albumCol != -1) cursor.getString(albumCol) else null) ?: "Music"

                    list.add(
                        MediaItemData(
                            uri = contentUri.toString(),
                            title = title,
                            artist = artist,
                            durationMs = duration,
                            sizeBytes = size,
                            mimeType = "audio/*",
                            isVideo = false,
                            resolution = "Stereo Audio",
                            folder = album,
                            thumbnailUri = null,
                            isSample = false
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Direct directory scanning fallback for music
        try {
            val publicAudioDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ).filter { it != null && it.exists() && it.canRead() }

            for (dir in publicAudioDirs) {
                dir.walkTopDown().maxDepth(3).filter { file ->
                    file.isFile && isAudioFile(file.name)
                }.forEach { file ->
                    val fileUri = Uri.fromFile(file).toString()
                    if (list.none { it.uri == fileUri }) {
                        list.add(
                            MediaItemData(
                                uri = fileUri,
                                title = file.nameWithoutExtension,
                                artist = file.parentFile?.name ?: "Audio",
                                durationMs = 0L,
                                sizeBytes = file.length(),
                                mimeType = "audio/*",
                                isVideo = false,
                                resolution = "Stereo Audio",
                                folder = file.parentFile?.name ?: "Music",
                                thumbnailUri = null,
                                isSample = false
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // Prioritize discovered local device audio files; provide samples only if storage has no media yet
        if (list.isNotEmpty()) {
            list.distinctBy { it.uri }
        } else {
            sampleAudio
        }
    }

    suspend fun resolveMediaFromUri(uri: Uri): MediaItemData = withContext(Dispatchers.IO) {
        var name = uri.lastPathSegment ?: "Imported Media"
        var size = 0L
        var mime = context.contentResolver.getType(uri) ?: "video/*"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {}

        val isVideo = !mime.startsWith("audio")
        MediaItemData(
            uri = uri.toString(),
            title = name,
            artist = "Local File",
            durationMs = 0L,
            sizeBytes = size,
            mimeType = mime,
            isVideo = isVideo,
            resolution = if (isVideo) "Device Video" else "Local Audio",
            folder = "Imported",
            thumbnailUri = uri.toString()
        )
    }

    suspend fun getDirectoryFiles(path: String = Environment.getExternalStorageDirectory().absolutePath): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) {
            val standardDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            ).filter { it != null && it.exists() }
            return@withContext standardDirs.map {
                FileItem(it.name, it.absolutePath, isDirectory = true, size = 0L, itemCount = it.list()?.size ?: 0)
            }
        }

        val files = dir.listFiles() ?: emptyArray()
        files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).map { file ->
            val isMedia = isMediaFile(file.name)
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                isMedia = isMedia,
                itemCount = if (file.isDirectory) file.list()?.size ?: 0 else 0
            )
        }
    }

    private fun isVideoFile(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext in listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "ts", "flv", "wmv", "m4v")
    }

    private fun isAudioFile(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext in listOf("mp3", "m4a", "wav", "flac", "aac", "ogg", "opus", "mid", "wma")
    }

    private fun isMediaFile(filename: String): Boolean {
        return isVideoFile(filename) || isAudioFile(filename)
    }

    private fun getMimeType(filename: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "mp4", "m4v" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            else -> "video/*"
        }
    }
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val isMedia: Boolean = false,
    val itemCount: Int = 0
)
