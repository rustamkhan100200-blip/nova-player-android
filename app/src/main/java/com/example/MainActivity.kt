package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.NovaBottomNavigationBar
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningModeScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.MusicLibraryScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SceneNotebookScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartTimelineScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubtitleStudioScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VideoLibraryScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.viewmodel.NovaPlayerViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: NovaPlayerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle external Intent data (e.g. user clicked a video or audio file in system file manager)
        intent?.data?.let { uri ->
            viewModel.openImportedUri(uri)
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val accentColorHex by viewModel.accentColor.collectAsState()
            MyApplicationTheme(
                appTheme = appTheme,
                accentColorHex = accentColorHex
            ) {
                NovaApp(
                    viewModel = viewModel,
                    onExitApp = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp(
    viewModel: NovaPlayerViewModel,
    onExitApp: () -> Unit = {}
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentMedia by viewModel.playbackManager.currentMedia.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val currentPosMs by viewModel.playbackManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.playbackManager.durationMs.collectAsState()

    // Centralized BackHandler for Android system back button
    BackHandler(enabled = true) {
        val handled = viewModel.onBackPressed()
        if (!handled) {
            onExitApp()
        }
    }

    val isFullScreenMedia = currentScreen == Screen.FullscreenVideo || currentScreen == Screen.Splash

    val isTabScreen = currentScreen in listOf(
        Screen.VideoLibrary,
        Screen.MusicLibrary,
        Screen.Tools,
        Screen.Me
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.NovaTheme.colors.background,
        bottomBar = {
            if (!isFullScreenMedia) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sticky MiniPlayer across browsing tabs
                    if (currentMedia != null && currentScreen != Screen.MusicPlayer) {
                        MiniPlayerBar(
                            currentMedia = currentMedia,
                            isPlaying = isPlaying,
                            currentPosMs = currentPosMs,
                            durationMs = durationMs,
                            onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                            onExpand = {
                                if (currentMedia!!.isVideo) {
                                    viewModel.navigateTo(Screen.FullscreenVideo)
                                } else {
                                    viewModel.navigateTo(Screen.MusicPlayer)
                                }
                            }
                        )
                    }

                    // Main Navigation Bar (VIDEOS | MUSIC | TOOLS | ME)
                    if (isTabScreen) {
                        NovaBottomNavigationBar(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenMedia) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
                .background(com.example.ui.theme.NovaTheme.colors.background)
        ) {
            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onFinished = { viewModel.navigateTo(Screen.VideoLibrary) }
                    )
                }

                Screen.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { viewModel.navigateTo(Screen.VideoLibrary) }
                    )
                }

                Screen.Home -> {
                    HomeScreen(viewModel = viewModel)
                }

                Screen.VideoLibrary -> {
                    VideoLibraryScreen(viewModel = viewModel)
                }

                Screen.FullscreenVideo -> {
                    VideoPlayerScreen(viewModel = viewModel)
                }

                Screen.MusicLibrary -> {
                    MusicLibraryScreen(viewModel = viewModel)
                }

                Screen.MusicPlayer -> {
                    MusicPlayerScreen(viewModel = viewModel)
                }

                Screen.SceneNotebook -> {
                    SceneNotebookScreen(viewModel = viewModel)
                }

                Screen.SmartTimeline -> {
                    SmartTimelineScreen(viewModel = viewModel)
                }

                Screen.SubtitleStudio -> {
                    SubtitleStudioScreen(viewModel = viewModel)
                }

                Screen.LearningMode -> {
                    LearningModeScreen(viewModel = viewModel)
                }

                Screen.FileManager -> {
                    FileManagerScreen(viewModel = viewModel)
                }

                Screen.Tools -> {
                    ToolsScreen(viewModel = viewModel)
                }

                Screen.AiStudio -> {
                    AiStudioScreen(viewModel = viewModel)
                }

                Screen.Me -> {
                    MeScreen(viewModel = viewModel)
                }

                Screen.Settings -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
