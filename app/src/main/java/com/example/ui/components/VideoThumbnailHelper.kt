package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance video thumbnail loader with memory caching (LruCache)
 * and hardware-accelerated MediaStore extraction on API 29+ with
 * MediaMetadataRetriever fallback that avoids initial black frames.
 */
object VideoThumbnailHelper {
    // 24MB maximum cache for responsive 60/120fps scrolling
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceIn(16 * 1024, 32 * 1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getFromCache(uriString: String): Bitmap? {
        return memoryCache.get(uriString)
    }

    fun putInCache(uriString: String, bitmap: Bitmap) {
        memoryCache.put(uriString, bitmap)
    }

    suspend fun loadThumbnail(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        // Fast path: in-memory cache hit
        memoryCache.get(uriString)?.let { return@withContext it }

        if (uriString.isBlank()) return@withContext null

        try {
            val uri = Uri.parse(uriString)

            // Method 1: Hardware-accelerated ContentResolver on API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uriString.startsWith("content://")) {
                try {
                    val bitmap = context.contentResolver.loadThumbnail(
                        uri,
                        Size(360, 202), // 16:9 optimal resolution
                        null
                    )
                    if (bitmap != null) {
                        putInCache(uriString, bitmap)
                        return@withContext bitmap
                    }
                } catch (_: Throwable) {
                    // Fall back to retriever
                }
            }

            // Method 2: MediaMetadataRetriever with 1.5s offset to skip black opening frames
            val retriever = MediaMetadataRetriever()
            try {
                if (uriString.startsWith("content://")) {
                    retriever.setDataSource(context, uri)
                } else if (uriString.startsWith("file://") || uriString.startsWith("/")) {
                    val path = if (uriString.startsWith("file://")) uri.path ?: uriString else uriString
                    retriever.setDataSource(path)
                } else {
                    // HTTP / Network / Sample stream
                    retriever.setDataSource(uriString, HashMap())
                }

                // Retrieve frame at 1.5s (1,500,000 microseconds)
                val timeUs = 1_500_000L
                val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    retriever.getScaledFrameAtTime(
                        timeUs,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                        360,
                        202
                    ) ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } else {
                    retriever.getFrameAtTime(timeUs)
                }

                if (rawBitmap != null) {
                    // Downscale if unreasonably large
                    val finalBitmap = if (rawBitmap.width > 480) {
                        val targetHeight = (480 * 9) / 16
                        Bitmap.createScaledBitmap(rawBitmap, 480, targetHeight, true)
                    } else {
                        rawBitmap
                    }
                    putInCache(uriString, finalBitmap)
                    return@withContext finalBitmap
                }
            } finally {
                try {
                    retriever.release()
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {
            // Graceful fallback on unsupported or corrupt files
        }

        return@withContext null
    }
}

/**
 * Polished, 16:9 responsive video thumbnail with badges and caching
 */
@Composable
fun VideoThumbnailView(
    uriString: String,
    modifier: Modifier = Modifier,
    durationText: String? = null,
    resolutionText: String? = null,
    progress: Float = 0f,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    val context = LocalContext.current
    val bitmapState = remember(uriString) {
        mutableStateOf(VideoThumbnailHelper.getFromCache(uriString))
    }

    LaunchedEffect(uriString) {
        if (bitmapState.value == null) {
            val loaded = VideoThumbnailHelper.loadThumbnail(context, uriString)
            bitmapState.value = loaded
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(NovaSurfaceCard)
            .border(1.dp, NovaCardBorder, shape),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = bitmapState.value

        Crossfade(targetState = bitmap != null, label = "thumb_crossfade") { hasBitmap ->
            if (hasBitmap && bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // High-aesthetic cinematic placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    NovaSurfaceElevated,
                                    NovaSurfaceCard,
                                    NovaDarkBackground
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = NovaElectricCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Subdued dark vignette at bottom for badges readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.6f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.75f)
                    )
                )
        )

        // Resolution Badge (Bottom-Left)
        if (!resolutionText.isNullOrBlank()) {
            Surface(
                color = NovaPrimaryViolet.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, NovaElectricCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = resolutionText,
                    color = NovaElectricCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Duration Badge (Bottom-Right)
        if (!durationText.isNullOrBlank()) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = durationText,
                    color = NovaPrimaryText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Progress line if partially watched
        if (progress > 0.02f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxSize()
            ) {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxSize()
                        .aspectRatio(16f / 9f)
                        .padding(top = 100.dp), // only bottom edge
                    color = NovaElectricCyan,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
