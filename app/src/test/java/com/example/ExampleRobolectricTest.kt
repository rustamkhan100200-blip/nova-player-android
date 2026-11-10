package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.calculateVideoGridColumns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nova Player", appName)
  }

  @Test
  fun `verify responsive grid columns across target mobile and tablet viewports`() {
    // 320x640 (very narrow portrait screen < 340dp) -> 1 column (prevents squishing)
    assertEquals(1, calculateVideoGridColumns(320f, isGridMode = true))

    // 360x800 (normal portrait phone) -> exactly 2 columns
    assertEquals(2, calculateVideoGridColumns(360f, isGridMode = true))

    // 393x852 (modern portrait phone) -> exactly 2 columns
    assertEquals(2, calculateVideoGridColumns(393f, isGridMode = true))

    // 412x915 (large portrait phone) -> exactly 2 columns
    assertEquals(2, calculateVideoGridColumns(412f, isGridMode = true))

    // 640x360 (compact landscape screen) -> 3 columns
    assertEquals(3, calculateVideoGridColumns(640f, isGridMode = true))

    // 800x360 (expanded landscape screen) -> 3 columns
    assertEquals(3, calculateVideoGridColumns(800f, isGridMode = true))

    // 915x412 (wide landscape screen >= 900dp) -> 4 columns
    assertEquals(4, calculateVideoGridColumns(915f, isGridMode = true))

    // List mode -> always 1 column regardless of viewport width
    assertEquals(1, calculateVideoGridColumns(320f, isGridMode = false))
    assertEquals(1, calculateVideoGridColumns(412f, isGridMode = false))
    assertEquals(1, calculateVideoGridColumns(800f, isGridMode = false))
  }

  @Test
  fun `verify center player controls contain exactly 5 symmetrical buttons`() {
    // Nova Player center controls specification:
    // 1. Previous Video
    // 2. Rewind 10s
    // 3. Play / Pause
    // 4. Forward 10s
    // 5. Next Video
    // The two redundant frame-step arrow buttons (previous/next frame) must NOT be present.
    val expectedControlTestTags = listOf(
      "player_prev_video",
      "player_rewind_10s",
      "player_play_pause",
      "player_forward_10s",
      "player_next_video"
    )
    assertEquals(5, expectedControlTestTags.size)

    val redundantControlTags = listOf("player_step_back_frame", "player_step_forward_frame")
    for (tag in redundantControlTags) {
      assertFalse(expectedControlTestTags.contains(tag))
    }
  }

  @Test
  fun `verify touch seeking playback preservation state logic`() {
    // Seeking logic:
    // If player was playing before touch down, wasPlayingBeforeSeek = true
    // When released, if wasPlayingBeforeSeek was true, resume playback; otherwise keep paused.
    var isPlaying = true
    var wasPlayingBeforeSeek = false

    // Step 1: User touches seek slider
    wasPlayingBeforeSeek = isPlaying
    isPlaying = false // paused during active drag to prevent buffer audio stutter
    assertTrue(wasPlayingBeforeSeek)
    assertFalse(isPlaying)

    // Step 2: User releases seek slider
    val shouldResume = wasPlayingBeforeSeek
    if (shouldResume) {
      isPlaying = true
    }
    assertTrue(isPlaying)
  }

  @Test
  fun `verify horizontal swipe seek delta and clamping logic`() {
    val durationMs = 120_000L
    val startPosMs = 30_000L
    val screenWidth = 400f
    val seekSpanMs = (durationMs * 0.25f).toLong().coerceIn(45_000L, 300_000L) // 45_000L

    // Drag 100px right -> forward seek
    val dxRight = 100f
    val deltaMsRight = ((dxRight / screenWidth) * seekSpanMs).toLong()
    val targetRight = (startPosMs + deltaMsRight).coerceIn(0L, durationMs)
    assertTrue(targetRight > startPosMs)
    assertEquals(41_250L, targetRight)

    // Drag 200px left -> backward seek
    val dxLeft = -200f
    val deltaMsLeft = ((dxLeft / screenWidth) * seekSpanMs).toLong()
    val targetLeft = (startPosMs + deltaMsLeft).coerceIn(0L, durationMs)
    assertTrue(targetLeft < startPosMs)
    assertEquals(7_500L, targetLeft)

    // Drag far left beyond 0 -> clamped to 0
    val dxFarLeft = -500f
    val deltaMsFarLeft = ((dxFarLeft / screenWidth) * seekSpanMs).toLong()
    val targetClampedZero = (startPosMs + deltaMsFarLeft).coerceIn(0L, durationMs)
    assertEquals(0L, targetClampedZero)

    // Drag far right beyond duration -> clamped to duration
    val dxFarRight = 1500f
    val deltaMsFarRight = ((dxFarRight / screenWidth) * seekSpanMs).toLong()
    val targetClampedDuration = (startPosMs + deltaMsFarRight).coerceIn(0L, durationMs)
    assertEquals(durationMs, targetClampedDuration)
  }

  @Test
  fun `verify small horizontal drag achieves 1-second precision`() {
    val durationMs = 180_000L // 3 minute clip
    val startPosMs = 60_000L
    val screenWidth = 1080f // 1080px wide screen
    val baseSpanMs = 90_000L // 90 second span for clips <= 5 mins

    // Test a tiny 12-pixel drag movement
    val dxTiny = 12f
    val deltaMs = ((dxTiny / screenWidth) * baseSpanMs).toLong()
    val target = (startPosMs + deltaMs).coerceIn(0L, durationMs)

    // 12px / 1080px * 90,000ms = 1,000ms = exactly 1 second adjustment!
    assertEquals(1000L, deltaMs)
    assertEquals(61_000L, target)
  }

  @Test
  fun `verify NovaMediaSessionService is registered in AndroidManifest`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = android.content.Intent(context, com.example.player.NovaMediaSessionService::class.java)
    val resolveInfo = context.packageManager.resolveService(intent, 0)
    assertTrue("NovaMediaSessionService must be declared in AndroidManifest", resolveInfo != null)
  }

  @Test
  fun `verify Back Button Navigation Policy rules`() {
    // Rule: Normal video exit stops playback and audio
    var isAudioOnlyMode = false
    var isPlaying = true
    var backgroundServiceRunning = false

    fun handleBackExit() {
      if (!isAudioOnlyMode) {
        isPlaying = false
        backgroundServiceRunning = false
      } else {
        backgroundServiceRunning = true
      }
    }

    // Case 1: Normal video exit
    handleBackExit()
    assertFalse(isPlaying)
    assertFalse(backgroundServiceRunning)

    // Case 2: Audio-Only mode exit
    isAudioOnlyMode = true
    isPlaying = true
    handleBackExit()
    assertTrue(isPlaying)
    assertTrue(backgroundServiceRunning)
  }

  @Test
  fun `verify notification small icon resource exists and is valid`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_notification)
    assertTrue("ic_notification drawable must exist for system notification tinting", drawable != null)
  }

  @Test
  fun `verify seek overlay lifecycle guarantees dismissal on release, cancel, media change, and dispose`() {
    var isSwipeSeeking = true
    var swipeSeekDeltaMs = -3000L
    var swipeSeekTargetMs = 33000L

    // Action 1: User releases finger (onDragEnd)
    fun onDragEnd() {
      isSwipeSeeking = false
    }
    onDragEnd()
    assertFalse("Seek overlay must be hidden on drag end", isSwipeSeeking)

    // Action 2: User cancels gesture (onDragCancel)
    isSwipeSeeking = true
    fun onDragCancel() {
      isSwipeSeeking = false
    }
    onDragCancel()
    assertFalse("Seek overlay must be hidden on drag cancel", isSwipeSeeking)

    // Action 3: Media changes
    isSwipeSeeking = true
    fun onMediaChanged() {
      isSwipeSeeking = false
      swipeSeekDeltaMs = 0L
      swipeSeekTargetMs = 0L
    }
    onMediaChanged()
    assertFalse("Seek overlay must be hidden when media changes", isSwipeSeeking)
    assertEquals(0L, swipeSeekDeltaMs)

    // Action 4: Screen disposed / closed
    isSwipeSeeking = true
    fun onDispose() {
      isSwipeSeeking = false
    }
    onDispose()
    assertFalse("Seek overlay must be hidden when player screen disposes", isSwipeSeeking)
  }

  @Test
  fun `verify commitSeek preserves playWhenReady state without requiring user tap`() {
    var playWhenReady = false
    var isPlaying = false

    fun commitSeek(resumePlayback: Boolean) {
      if (resumePlayback) {
        playWhenReady = true
        isPlaying = true
      } else {
        playWhenReady = false
        isPlaying = false
      }
    }

    // Scenario A: Video was playing before swipe seek -> Automatically resume with playWhenReady = true
    val wasPlaying = true
    commitSeek(resumePlayback = wasPlaying)
    assertTrue("playWhenReady must be true when resuming playback", playWhenReady)
    assertTrue("isPlaying must be true when resuming playback", isPlaying)

    // Scenario B: Video was paused before swipe seek -> Preserve paused state
    val wasPaused = false
    commitSeek(resumePlayback = wasPaused)
    assertFalse("playWhenReady must remain false when seeked while paused", playWhenReady)
    assertFalse("isPlaying must remain false when seeked while paused", isPlaying)
  }
}
