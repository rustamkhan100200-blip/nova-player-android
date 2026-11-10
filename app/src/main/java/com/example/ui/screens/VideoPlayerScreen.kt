package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.components.AudioOnlyVisualizer
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaAccentPink
import com.example.ui.theme.NovaBookmarkGold
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSecondaryText
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.NovaPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VideoPlayerScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentMedia by viewModel.playbackManager.currentMedia.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val isBuffering by viewModel.playbackManager.isBuffering.collectAsState()
    val errorMessage by viewModel.playbackManager.errorMessage.collectAsState()
    val currentPosMs by viewModel.playbackManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.playbackManager.durationMs.collectAsState()
    val volume by viewModel.playbackManager.volume.collectAsState()
    val isMuted by viewModel.playbackManager.isMuted.collectAsState()
    val resumedPos by viewModel.playbackManager.resumedPositionMs.collectAsState()
    val playbackSpeed by viewModel.playbackManager.playbackSpeed.collectAsState()
    val aspectRatio by viewModel.playerAspectRatio.collectAsState()
    val isLocked by viewModel.isControlsLocked.collectAsState()
    val isAudioOnly by viewModel.playbackManager.isAudioOnlyMode.collectAsState()

    // Subtitles state
    val subtitlesEnabled by viewModel.subtitlesEnabled.collectAsState()
    val selectedSubtitle by viewModel.selectedSubtitle.collectAsState()
    val subtitleTextSizeSp by viewModel.subtitleTextSizeSp.collectAsState()
    val subtitleTextColor by viewModel.subtitleTextColor.collectAsState()
    val subtitleBackgroundColor by viewModel.subtitleBackgroundColor.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showTopMoreMenu by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showVolumeSheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showBookmarkDialog by remember { mutableStateOf(false) }

    // Double-tap visual feedback states
    var showRewindIndicator by remember { mutableStateOf(false) }
    var showForwardIndicator by remember { mutableStateOf(false) }
    var showVolumeGestureIndicator by remember { mutableStateOf(false) }
    var showBrightnessGestureIndicator by remember { mutableStateOf(false) }

    // Screen brightness state initialized from window attributes
    var brightness by remember {
        val act = context.findActivity()
        val currentB = act?.window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (currentB < 0f) 0.5f else currentB)
    }

    var newNoteText by remember { mutableStateOf("") }
    var newBookmarkLabel by remember { mutableStateOf("") }

    // Touch seeking states (Priority 6)
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }
    var wasPlayingBeforeSeek by remember { mutableStateOf(false) }

    // Horizontal swipe gesture seeking states (Bug 2 & 3: Reliable gesture lifecycle)
    var isSwipeSeeking by remember { mutableStateOf(false) }
    var swipeSeekTargetMs by remember { mutableLongStateOf(0L) }
    var swipeSeekDeltaMs by remember { mutableLongStateOf(0L) }
    var wasPlayingBeforeSwipeSeek by remember { mutableStateOf(false) }

    // Use rememberUpdatedState for dynamic player states so pointerInput never gets cancelled prematurely
    val currentPosState = rememberUpdatedState(currentPosMs)
    val durationState = rememberUpdatedState(durationMs)
    val isPlayingState = rememberUpdatedState(isPlaying)
    val brightnessState = rememberUpdatedState(brightness)

    // Immediately dismiss any active seek overlay when the media changes
    LaunchedEffect(currentMedia?.uri) {
        isSwipeSeeking = false
        swipeSeekDeltaMs = 0L
        swipeSeekTargetMs = 0L
    }

    // Resume playback prompt state (Task 6: Non-obstructive dialog, auto-dismissed, never floats permanently)
    var showResumeDialog by remember { mutableStateOf(false) }
    var hasHandledResumeForSession by remember { mutableStateOf(false) }

    LaunchedEffect(resumedPos) {
        if (resumedPos != null && resumedPos!! > 2000L && !hasHandledResumeForSession) {
            showResumeDialog = true
        }
    }

    LaunchedEffect(showResumeDialog) {
        if (showResumeDialog) {
            delay(6000)
            showResumeDialog = false
            hasHandledResumeForSession = true
        }
    }

    // Helper to safely exit player screen and reset display attributes
    val exitPlayer = {
        isSwipeSeeking = false
        val activity = context.findActivity()
        activity?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            act.window.attributes = lp
            if (act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
        viewModel.playbackManager.stopVideoPlayback(forceStop = false)
        viewModel.onBackPressed()
    }

    BackHandler {
        exitPlayer()
    }

    DisposableEffect(Unit) {
        val activity = context.findActivity()
        onDispose {
            isSwipeSeeking = false
            activity?.let { act ->
                val lp = act.window.attributes
                lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                act.window.attributes = lp
            }
        }
    }

    // Auto-hide controls after 3.5 seconds of inactivity if playing
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && !isLocked) {
            delay(3500)
            showControls = false
        }
    }

    // Double tap rewind indicator auto-dismiss
    LaunchedEffect(showRewindIndicator) {
        if (showRewindIndicator) {
            delay(700)
            showRewindIndicator = false
        }
    }

    // Double tap forward indicator auto-dismiss
    LaunchedEffect(showForwardIndicator) {
        if (showForwardIndicator) {
            delay(700)
            showForwardIndicator = false
        }
    }

    // Volume gesture indicator auto-dismiss
    LaunchedEffect(showVolumeGestureIndicator) {
        if (showVolumeGestureIndicator) {
            delay(1200)
            showVolumeGestureIndicator = false
        }
    }

    // Brightness gesture indicator auto-dismiss
    LaunchedEffect(showBrightnessGestureIndicator) {
        if (showBrightnessGestureIndicator) {
            delay(1200)
            showBrightnessGestureIndicator = false
        }
    }

    val resizeMode = when (aspectRatio) {
        "FILL" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        "16:9" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        "ZOOM" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // Gesture detector for Double-Tap Skip/Rewind and Single-Tap HUD
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectTapGestures(
                    onTap = {
                        if (!isSwipeSeeking) {
                            showControls = !showControls
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!isSwipeSeeking) {
                            if (offset.x < size.width / 2) {
                                viewModel.playbackManager.seekBackward(10000L)
                                showRewindIndicator = true
                                showForwardIndicator = false
                            } else {
                                viewModel.playbackManager.seekForward(10000L)
                                showForwardIndicator = true
                                showRewindIndicator = false
                            }
                        }
                    }
                )
            }
            // Multi-direction Drag Gesture: Horizontal Swipe Seek, Left-Vertical Brightness, Right-Vertical Volume
            // Keyed ONLY on isLocked so that current position updates never cancel or stall active gestures
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                var dragMode = 0 // 0: undecided, 1: horizontal seek, 2: brightness, 3: volume
                var accumulatedDx = 0f
                var accumulatedDy = 0f
                var swipeStartPosMs = 0L

                try {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragMode = 0
                            accumulatedDx = 0f
                            accumulatedDy = 0f
                            swipeStartPosMs = currentPosState.value
                            swipeSeekTargetMs = swipeStartPosMs
                            swipeSeekDeltaMs = 0L
                            wasPlayingBeforeSwipeSeek = isPlayingState.value
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDx += dragAmount.x
                            accumulatedDy += dragAmount.y

                            val totalDuration = durationState.value

                            if (dragMode == 0) {
                                val absX = kotlin.math.abs(accumulatedDx)
                                val absY = kotlin.math.abs(accumulatedDy)
                                // Threshold to distinguish intentional horizontal seek from vertical adjustments
                                if (absX > 8f || absY > 12f) {
                                    if (absX >= absY) {
                                        dragMode = 1 // Horizontal swipe seeking
                                        isSwipeSeeking = true
                                        // Bug 3 fix: Do NOT pause ExoPlayer during gesture; preserve player state
                                    } else {
                                        dragMode = if (change.position.x < size.width / 2) 2 else 3
                                        if (dragMode == 2) showBrightnessGestureIndicator = true else showVolumeGestureIndicator = true
                                    }
                                }
                            }

                            when (dragMode) {
                                1 -> {
                                    // Horizontal Seek: Immediate, proportional sensitivity supporting ~1-3s adjustments
                                    isSwipeSeeking = true
                                    val baseSpanMs = when {
                                        totalDuration <= 60_000L -> 30_000L
                                        totalDuration <= 300_000L -> 90_000L
                                        totalDuration <= 1800_000L -> 180_000L
                                        else -> 360_000L
                                    }
                                    val fraction = accumulatedDx / size.width.coerceAtLeast(1)
                                    val deltaMs = (fraction * baseSpanMs).toLong()
                                    swipeSeekDeltaMs = deltaMs
                                    val target = (swipeStartPosMs + deltaMs).coerceIn(0L, totalDuration.coerceAtLeast(1L))
                                    swipeSeekTargetMs = target
                                    // Live video frame preview update (throttled ~75ms inside seekPreview)
                                    viewModel.playbackManager.seekPreview(target)
                                }
                                2 -> {
                                    // Brightness (Left vertical drag)
                                    val delta = -dragAmount.y / 400f
                                    val newB = (brightnessState.value + delta).coerceIn(0.01f, 1.0f)
                                    brightness = newB
                                    val act = context.findActivity()
                                    act?.let { a ->
                                        val lp = a.window.attributes
                                        lp.screenBrightness = newB
                                        a.window.attributes = lp
                                    }
                                    showBrightnessGestureIndicator = true
                                }
                                3 -> {
                                    // Volume (Right vertical drag)
                                    val delta = -dragAmount.y / 400f
                                    viewModel.playbackManager.adjustVolumeBy(delta)
                                    showVolumeGestureIndicator = true
                                }
                            }
                        },
                        onDragEnd = {
                            if (dragMode == 1) {
                                // On finger release: commit seek and restore pre-seek playback state
                                viewModel.playbackManager.commitSeek(swipeSeekTargetMs, wasPlayingBeforeSwipeSeek)
                            }
                            dragMode = 0
                            isSwipeSeeking = false
                            showBrightnessGestureIndicator = false
                            showVolumeGestureIndicator = false
                        },
                        onDragCancel = {
                            if (dragMode == 1) {
                                viewModel.playbackManager.commitSeek(swipeSeekTargetMs, wasPlayingBeforeSwipeSeek)
                            }
                            dragMode = 0
                            isSwipeSeeking = false
                            showBrightnessGestureIndicator = false
                            showVolumeGestureIndicator = false
                        }
                    )
                } finally {
                    // Guaranteed lifecycle cleanup: seek overlay is never left stuck on screen
                    if (dragMode == 1 && isSwipeSeeking) {
                        viewModel.playbackManager.commitSeek(swipeSeekTargetMs, wasPlayingBeforeSwipeSeek)
                    }
                    dragMode = 0
                    isSwipeSeeking = false
                    showBrightnessGestureIndicator = false
                    showVolumeGestureIndicator = false
                }
            }
    ) {
        val screenWidth = maxWidth

        // Media Surface: Audio-Only Visualizer OR PlayerView
        if (isAudioOnly) {
            AudioOnlyVisualizer(
                media = currentMedia,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // AndroidView embedding ExoPlayer PlayerView with stable player binding
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.playbackManager.exoPlayer
                        useController = false
                        this.resizeMode = resizeMode
                    }
                },
                update = { playerView ->
                    if (playerView.player != viewModel.playbackManager.exoPlayer) {
                        playerView.player = viewModel.playbackManager.exoPlayer
                    }
                    playerView.resizeMode = resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Double-Tap Rewind Animation Indicator (Left side)
        AnimatedVisibility(
            visible = showRewindIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NovaElectricCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = NovaElectricCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "-10s",
                        color = NovaElectricCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Double-Tap Fast Forward Animation Indicator (Right side)
        AnimatedVisibility(
            visible = showForwardIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 36.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NovaElectricCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+10s",
                        color = NovaElectricCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        tint = NovaElectricCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // On-Screen Brightness Drag Feedback Pill
        AnimatedVisibility(
            visible = showBrightnessGestureIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            Surface(
                color = NovaSurfaceCard.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = null,
                        tint = NovaElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Brightness ${(brightness * 100).toInt()}%",
                        color = NovaPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // On-Screen Volume Drag Feedback Pill
        AnimatedVisibility(
            visible = showVolumeGestureIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            Surface(
                color = NovaSurfaceCard.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricCyan)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (volume <= 0f) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = NovaElectricCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volume ${(volume * 100).toInt()}%",
                        color = NovaPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Floating Horizontal Swipe Seek Feedback Indicator (Bug 2: Dismisses cleanly within 200ms)
        AnimatedVisibility(
            visible = isSwipeSeeking,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = NovaSurfaceCard.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, NovaElectricCyan),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (swipeSeekDeltaMs >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val sign = if (swipeSeekDeltaMs >= 0) "+" else "-"
                        val absDelta = kotlin.math.abs(swipeSeekDeltaMs)
                        Text(
                            text = "$sign${viewModel.formatMs(absDelta)}",
                            color = NovaElectricCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${viewModel.formatMs(swipeSeekTargetMs)} / ${viewModel.formatMs(durationMs)}",
                        color = NovaPrimaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (durationMs > 0) (swipeSeekTargetMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(180.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NovaElectricCyan,
                        trackColor = NovaCardBorder
                    )
                }
            }
        }

        // Buffering / Loading Spinner State (suppressed during active swipe seek to prevent flickering)
        if (isBuffering && !isSwipeSeeking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = NovaElectricCyan,
                        modifier = Modifier.size(46.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Buffering Stream...",
                        color = NovaPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Playback Error State Banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = NovaSurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaAccentPink),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NovaAccentPink,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Playback Error",
                            color = NovaPrimaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "Unable to stream media.",
                            color = NovaSecondaryText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.playbackManager.clearError()
                                    currentMedia?.let { viewModel.playbackManager.playMedia(it) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                            ) {
                                Text("Retry", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { exitPlayer() }) {
                                Text("Go Back", color = NovaSecondaryText)
                            }
                        }
                    }
                }
            }
        }

        // Subtitle display overlay if subtitles are enabled and track selected
        if (subtitlesEnabled && selectedSubtitle != null && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (showControls) 110.dp else 36.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(subtitleBackgroundColor),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(subtitleTextColor).copy(alpha = 0.25f))
                ) {
                    val subtitleText = when (selectedSubtitle?.language) {
                        "ps" -> "نووا پلیر — فراتر له لیدلو (د پښتو فرعي سرلیک ژباړه)"
                        "fa" -> "نووا پلیر — فراتر از تماشا (پشتیبانی کامل از متن دری)"
                        else -> "Nova Player — Beyond Watching (Cinema Quality Subtitles)"
                    }
                    Text(
                        text = subtitleText,
                        color = Color(subtitleTextColor),
                        fontSize = subtitleTextSizeSp.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = if (selectedSubtitle!!.isRtl) TextAlign.Right else TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Lock icon overlay when locked
        if (isLocked) {
            IconButton(
                onClick = { viewModel.toggleControlsLock() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(NovaSurfaceCard.copy(alpha = 0.9f))
                    .border(1.dp, NovaElectricCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Unlock Controls",
                    tint = NovaElectricCyan
                )
            }
        }

        // HUD Controls Overlay
        AnimatedVisibility(
            visible = showControls && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Top Action Bar & Resume Position Notification Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = { exitPlayer() },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NovaPrimaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Responsive Title and Subtitle: Single line with ellipsis (Never character-by-character wrap!)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = currentMedia?.title ?: "Nova Player",
                                color = NovaPrimaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentMedia?.resolution ?: "1080p"} • ${currentMedia?.artist ?: ""}",
                                color = NovaSecondaryText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Top Action Controls: Compact 3-Dot More Menu on portrait, expanded on landscape
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (screenWidth > 600.dp) {
                                // Audio-Only Mode Toggle
                                IconButton(
                                    onClick = { viewModel.playbackManager.setAudioOnlyMode(!isAudioOnly) },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = "Audio-Only Mode",
                                        tint = if (isAudioOnly) NovaElectricCyan else NovaSecondaryText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Screen Rotation / Landscape Toggle
                                IconButton(
                                    onClick = {
                                        val activity = context.findActivity()
                                        activity?.let {
                                            it.requestedOrientation = if (it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                            } else {
                                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = "Rotate Screen",
                                        tint = NovaElectricCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Volume Sheet Toggle
                                IconButton(
                                    onClick = { showVolumeSheet = !showVolumeSheet },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted || volume <= 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                        contentDescription = "Volume Controls",
                                        tint = NovaElectricCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Aspect Ratio Toggle
                                IconButton(
                                    onClick = {
                                        val next = when (aspectRatio) {
                                            "FIT" -> "FILL"
                                            "FILL" -> "16:9"
                                            "16:9" -> "ZOOM"
                                            else -> "FIT"
                                        }
                                        viewModel.setAspectRatio(next)
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = NovaElectricCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // PiP Button
                                IconButton(
                                    onClick = {
                                        val activity = context.findActivity()
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                            val params = PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(16, 9))
                                                .build()
                                            activity.enterPictureInPictureMode(params)
                                        }
                                    },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "Picture in Picture",
                                        tint = NovaPrimaryText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Lock Controls
                                IconButton(
                                    onClick = { viewModel.toggleControlsLock() },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Lock Controls",
                                        tint = NovaPrimaryText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                // Compact 3-Dot More Menu on portrait: guarantees maximum space for filename so it never wraps vertically
                                Box {
                                    IconButton(
                                        onClick = { showTopMoreMenu = true },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .testTag("player_more_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = NovaPrimaryText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showTopMoreMenu,
                                        onDismissRequest = { showTopMoreMenu = false },
                                        modifier = Modifier.background(NovaSurfaceCard)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Audio-Only Mode", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.Headphones, null, tint = if (isAudioOnly) NovaElectricCyan else NovaSecondaryText) },
                                            onClick = {
                                                viewModel.playbackManager.setAudioOnlyMode(!isAudioOnly)
                                                showTopMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Rotate Screen", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.ScreenRotation, null, tint = NovaElectricCyan) },
                                            onClick = {
                                                val activity = context.findActivity()
                                                activity?.let {
                                                    it.requestedOrientation = if (it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                                    } else {
                                                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                                    }
                                                }
                                                showTopMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Aspect: $aspectRatio", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.AspectRatio, null, tint = NovaElectricCyan) },
                                            onClick = {
                                                val next = when (aspectRatio) {
                                                    "FIT" -> "FILL"
                                                    "FILL" -> "16:9"
                                                    "16:9" -> "ZOOM"
                                                    else -> "FIT"
                                                }
                                                viewModel.setAspectRatio(next)
                                                showTopMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Volume Controls", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.VolumeUp, null, tint = NovaElectricCyan) },
                                            onClick = {
                                                showVolumeSheet = true
                                                showTopMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Picture in Picture", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.PictureInPictureAlt, null, tint = NovaPrimaryText) },
                                            onClick = {
                                                val activity = context.findActivity()
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
                                                    val params = PictureInPictureParams.Builder()
                                                        .setAspectRatio(Rational(16, 9))
                                                        .build()
                                                    activity.enterPictureInPictureMode(params)
                                                }
                                                showTopMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Lock Controls", color = NovaPrimaryText) },
                                            leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = NovaPrimaryText) },
                                            onClick = {
                                                viewModel.toggleControlsLock()
                                                showTopMoreMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // CENTER PLAYBACK CONTROLS (EXACTLY 5 CONTROLS, SYMMETRICAL & HORIZONTAL)
                // Redundant left/right arrows completely removed as requested
                // =========================================================================
                val queue by viewModel.playbackManager.queue.collectAsState()
                val queueIndex by viewModel.playbackManager.queueIndex.collectAsState()
                val hasPrev = queue.isNotEmpty() && queueIndex > 0
                val hasNext = queue.isNotEmpty() && queueIndex < queue.size - 1

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Previous Video Button
                    IconButton(
                        onClick = { viewModel.playbackManager.previous() },
                        enabled = hasPrev,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hasPrev) NovaSurfaceCard.copy(alpha = 0.85f) else NovaSurfaceCard.copy(alpha = 0.35f))
                            .border(1.dp, if (hasPrev) NovaCardBorder else Color.Transparent, CircleShape)
                            .testTag("player_prev_video_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Video",
                            tint = if (hasPrev) NovaPrimaryText else NovaSecondaryText.copy(alpha = 0.35f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 2. Seek Backward 10s Button
                    IconButton(
                        onClick = { viewModel.playbackManager.seekBackward(10000L) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NovaSurfaceCard.copy(alpha = 0.85f))
                            .border(1.dp, NovaCardBorder, CircleShape)
                            .testTag("player_seek_back_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Seek Back 10s",
                            tint = NovaPrimaryText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 3. Main Glowing Center Play/Pause Button (Prominent)
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NovaPrimaryViolet, NovaElectricCyan)
                                )
                            )
                            .clickable { viewModel.playbackManager.togglePlayPause() }
                            .testTag("player_play_pause_center"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = NovaDarkBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // 4. Seek Forward 10s Button
                    IconButton(
                        onClick = { viewModel.playbackManager.seekForward(10000L) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NovaSurfaceCard.copy(alpha = 0.85f))
                            .border(1.dp, NovaCardBorder, CircleShape)
                            .testTag("player_seek_fwd_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Seek Forward 10s",
                            tint = NovaPrimaryText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 5. Next Video Button
                    IconButton(
                        onClick = { viewModel.playbackManager.next() },
                        enabled = hasNext,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hasNext) NovaSurfaceCard.copy(alpha = 0.85f) else NovaSurfaceCard.copy(alpha = 0.35f))
                            .border(1.dp, if (hasNext) NovaCardBorder else Color.Transparent, CircleShape)
                            .testTag("player_next_video_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Video",
                            tint = if (hasNext) NovaPrimaryText else NovaSecondaryText.copy(alpha = 0.35f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // =========================================================================
                // BOTTOM CONTROLS & TIMELINE (TOUCH SEEKING WITH TIMESTAMP PREVIEW)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val sliderValue = if (isDraggingSlider) dragPosition else {
                        if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                    }

                    val displayCurrentMs = if (isDraggingSlider) {
                        (dragPosition * durationMs).toLong().coerceIn(0L, durationMs)
                    } else {
                        currentPosMs
                    }

                    // Timestamps: Live Drag Preview on Left, Total Duration on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewModel.formatMs(displayCurrentMs),
                                color = NovaElectricCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDraggingSlider) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NovaElectricCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SEEKING",
                                        color = NovaElectricCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = viewModel.formatMs(durationMs),
                            color = NovaSecondaryText,
                            fontSize = 12.sp
                        )
                    }

                    // Smooth Touch-Based Timeline Slider (Priority 6)
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            if (!isDraggingSlider) {
                                wasPlayingBeforeSeek = isPlaying
                            }
                            isDraggingSlider = true
                            dragPosition = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            val targetMs = (dragPosition * durationMs).toLong().coerceIn(0L, durationMs)
                            viewModel.playbackManager.seekTo(targetMs)
                            if (wasPlayingBeforeSeek && !viewModel.playbackManager.isPlaying.value) {
                                viewModel.playbackManager.play()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_timeline_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = NovaElectricCyan,
                            activeTrackColor = NovaElectricCyan,
                            inactiveTrackColor = NovaSurfaceElevated
                        )
                    )

                    // Secondary Bottom Feature Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Bookmark Button
                            IconButton(
                                onClick = { showBookmarkDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = "Add Bookmark",
                                    tint = NovaBookmarkGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Note Button
                            IconButton(
                                onClick = { showNoteDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Scene Note",
                                    tint = NovaPrimaryViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // Subtitles Button
                            IconButton(
                                onClick = { showSubtitleSheet = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = if (subtitlesEnabled && selectedSubtitle != null) NovaElectricCyan else NovaSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Speed Selector
                            Box {
                                TextButton(
                                    onClick = { showSpeedMenu = true },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = NovaElectricCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false },
                                    modifier = Modifier.background(NovaSurfaceCard)
                                ) {
                                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${speed}x Speed",
                                                    color = if (playbackSpeed == speed) NovaElectricCyan else NovaPrimaryText
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSpeed(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Audio Mute Toggle
                            IconButton(
                                onClick = { viewModel.playbackManager.toggleMute() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted || volume <= 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = NovaPrimaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick On-screen Volume Control Card
        AnimatedVisibility(
            visible = showVolumeSheet,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
        ) {
            Surface(
                color = NovaSurfaceCard.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricCyan.copy(alpha = 0.6f)),
                modifier = Modifier.width(260.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Volume: ${(volume * 100).toInt()}%",
                            color = NovaPrimaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        IconButton(
                            onClick = { viewModel.playbackManager.toggleMute() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted || volume <= 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Toggle Mute",
                                tint = NovaElectricCyan
                            )
                        }
                    }
                    Slider(
                        value = volume,
                        onValueChange = { viewModel.playbackManager.setVolume(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = NovaElectricCyan,
                            activeTrackColor = NovaElectricCyan,
                            inactiveTrackColor = NovaSurfaceElevated
                        )
                    )
                }
            }
        }

        // Add Scene Note Dialog
        if (showNoteDialog) {
            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = {
                    Text(
                        text = "Add Scene Note at ${viewModel.formatMs(currentPosMs)}",
                        color = NovaPrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Write scene insight or study note...", color = NovaSecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NovaPrimaryText,
                            unfocusedTextColor = NovaPrimaryText,
                            focusedBorderColor = NovaElectricCyan,
                            unfocusedBorderColor = NovaCardBorder
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                viewModel.addSceneNote(newNoteText)
                                newNoteText = ""
                            }
                            showNoteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                    ) {
                        Text("Save Note", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Cancel", color = NovaSecondaryText)
                    }
                },
                containerColor = NovaSurfaceCard
            )
        }

        // Add Bookmark Dialog
        if (showBookmarkDialog) {
            AlertDialog(
                onDismissRequest = { showBookmarkDialog = false },
                title = {
                    Text(
                        text = "Bookmark Scene at ${viewModel.formatMs(currentPosMs)}",
                        color = NovaPrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newBookmarkLabel,
                        onValueChange = { newBookmarkLabel = it },
                        placeholder = { Text("Bookmark title (e.g. Climax, Key Concept)", color = NovaSecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NovaPrimaryText,
                            unfocusedTextColor = NovaPrimaryText,
                            focusedBorderColor = NovaElectricCyan,
                            unfocusedBorderColor = NovaCardBorder
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addBookmarkAtCurrentPosition(newBookmarkLabel)
                            newBookmarkLabel = ""
                            showBookmarkDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaBookmarkGold)
                    ) {
                        Text("Save", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookmarkDialog = false }) {
                        Text("Cancel", color = NovaSecondaryText)
                    }
                },
                containerColor = NovaSurfaceCard
            )
        }

        // Subtitle Studio Bottom Sheet (OFF by default, Pashto & Dari RTL, Styling)
        if (showSubtitleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSubtitleSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = NovaSurfaceCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Subtitles",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaPrimaryText
                            )
                            Text(
                                text = "OFF by default • Pashto, Dari & English",
                                fontSize = 12.sp,
                                color = NovaSecondaryText
                            )
                        }
                        Switch(
                            checked = subtitlesEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleSubtitles(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NovaElectricCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (subtitlesEnabled) {
                        Text(
                            text = "SELECT LANGUAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaElectricCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        viewModel.supportedLanguages.forEach { track ->
                            val isSelected = selectedSubtitle?.id == track.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NovaSurfaceElevated else Color.Transparent)
                                    .clickable {
                                        viewModel.setSubtitle(track)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = track.name,
                                        color = if (isSelected) NovaElectricCyan else NovaPrimaryText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                    if (track.isRtl) {
                                        Text(
                                            text = "RTL Shaping Active",
                                            fontSize = 10.sp,
                                            color = NovaAccentAmber
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = NovaElectricCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Subtitle Font Size Controls
                        Text(
                            text = "TEXT SIZE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaSecondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Small" to 14f,
                                "Medium" to 18f,
                                "Large" to 22f,
                                "XL" to 26f
                            ).forEach { (label, sp) ->
                                val selected = subtitleTextSizeSp == sp
                                Surface(
                                    color = if (selected) NovaElectricCyan else NovaSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { viewModel.setSubtitleTextSize(sp) }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.Black else NovaPrimaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Subtitle Text Color Controls
                        Text(
                            text = "TEXT COLOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaSecondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "White" to 0xFFFFFFFF,
                                "Yellow" to 0xFFFFD166,
                                "Cyan" to 0xFF00D9FF
                            ).forEach { (label, hex) ->
                                val selected = subtitleTextColor == hex
                                Surface(
                                    color = if (selected) NovaElectricCyan.copy(alpha = 0.25f) else NovaSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, NovaElectricCyan) else null,
                                    modifier = Modifier.clickable { viewModel.setSubtitleTextColor(hex) }
                                ) {
                                    Text(
                                        text = label,
                                        color = Color(hex),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showSubtitleSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done", color = NovaElectricCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Compact, accessible Resume / Start Over dialog (Task 6)
        // Non-obstructive dialog, auto-dismisses after 6s or on user selection, never floats over controls permanently
        if (showResumeDialog && resumedPos != null && resumedPos!! > 2000L) {
            Dialog(
                onDismissRequest = {
                    showResumeDialog = false
                    hasHandledResumeForSession = true
                }
            ) {
                Surface(
                    color = NovaSurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, NovaElectricCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Resume Playback?",
                            color = NovaPrimaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Continue watching from ${viewModel.formatMs(resumedPos!!)} or start over from the beginning?",
                            color = NovaSecondaryText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.playbackManager.restartFromBeginning()
                                    showResumeDialog = false
                                    hasHandledResumeForSession = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NovaCardBorder)
                            ) {
                                Text("Start Over", color = NovaPrimaryText, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    showResumeDialog = false
                                    hasHandledResumeForSession = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                            ) {
                                Text(
                                    text = "Resume",
                                    color = NovaDarkBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
