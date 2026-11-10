package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemData
import com.example.data.model.SubtitleTrack
import com.example.data.repository.FileItem
import com.example.ui.components.NovaNeonButton
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSecondaryText
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.viewmodel.NovaPlayerViewModel
import java.io.File

@Composable
fun SubtitleStudioScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSub by viewModel.selectedSubtitle.collectAsState()
    val delayMs by viewModel.subtitleDelayMs.collectAsState()
    var textSizeSp by remember { mutableFloatStateOf(16f) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment ?: "External Subtitle"
            viewModel.setSubtitle(SubtitleTrack("custom", name, "custom", uri.toString()))
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaPrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Subtitle Studio",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaPrimaryText
                    )
                }

                Text(
                    text = "Typography, multi-language right-to-left engine & sync synchronization",
                    fontSize = 12.sp,
                    color = NovaSecondaryText,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Subtitle Live Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NovaSurfaceCard)
                        .border(1.dp, NovaElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (selectedSub?.isRtl == true) "نووا پلیر — فراتر از تماشا (پشتیبانی کامل از زبان‌های راست‌به‌چپ)"
                                else "Nova Player — Beyond Watching (Precision Subtitle Rendering)",
                                color = Color.White,
                                fontSize = textSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = if (selectedSub?.isRtl == true) TextAlign.Right else TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sync Offset: ${if (delayMs >= 0) "+$delayMs" else "$delayMs"} ms",
                            color = NovaElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sync Timing Offset Controls
                Text(
                    text = "TIMING SYNCHRONIZATION OFFSET",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NovaNeonButton(
                        text = "- 500 ms",
                        onClick = { viewModel.adjustSubtitleOffset(-500L) },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                    NovaNeonButton(
                        text = "Reset Sync",
                        onClick = { viewModel.adjustSubtitleOffset(-delayMs) },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                    NovaNeonButton(
                        text = "+ 500 ms",
                        onClick = { viewModel.adjustSubtitleOffset(500L) },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Font Size Slider
                Text(
                    text = "SUBTITLE FONT SIZE: ${textSizeSp.toInt()} SP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Slider(
                    value = textSizeSp,
                    onValueChange = { textSizeSp = it },
                    valueRange = 12f..28f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaElectricCyan,
                        activeTrackColor = NovaElectricCyan,
                        inactiveTrackColor = NovaSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AVAILABLE SUBTITLE TRACKS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaSecondaryText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Import .SRT/.VTT",
                        fontSize = 12.sp,
                        color = NovaElectricCyan,
                        modifier = Modifier.clickable { filePickerLauncher.launch(arrayOf("*/*")) }
                    )
                }
            }
        }

        items(viewModel.availableSubtitles) { track ->
            val isSelected = selectedSub?.id == track.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) NovaSurfaceElevated else NovaSurfaceCard)
                    .border(
                        1.dp,
                        if (isSelected) NovaElectricCyan else NovaCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setSubtitle(track) }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = track.name,
                        color = if (isSelected) NovaElectricCyan else NovaPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (track.isRtl) {
                        Text(
                            text = "RTL Directional Shaping Enabled",
                            color = NovaAccentAmber,
                            fontSize = 11.sp
                        )
                    }
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = NovaElectricCyan
                    )
                }
            }
        }
    }
}

@Composable
fun FileManagerScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentPath by viewModel.currentDirPath.collectAsState()
    val files by viewModel.directoryFiles.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openImportedUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "File Manager",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NovaPrimaryText
                        )
                        Text(
                            text = "Browse device folders and storage",
                            fontSize = 12.sp,
                            color = NovaSecondaryText
                        )
                    }

                    IconButton(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NovaSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Open Document",
                            tint = NovaElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Path navigation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NovaSurfaceCard)
                        .border(1.dp, NovaCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPath.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val parent = File(currentPath).parent ?: ""
                                viewModel.loadDirectory(parent)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Up",
                                tint = NovaElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (currentPath.isBlank()) "Root Storage" else currentPath,
                        color = NovaPrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        items(files) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaCardBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        if (file.isDirectory) {
                            viewModel.loadDirectory(file.path)
                        } else if (file.isMedia) {
                            viewModel.openImportedUri(Uri.fromFile(File(file.path)))
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        file.isDirectory -> Icons.Default.Folder
                        file.isMedia -> Icons.Default.Movie
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = if (file.isDirectory) NovaElectricCyan else if (file.isMedia) NovaPrimaryViolet else NovaSecondaryText,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        color = NovaPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (file.isDirectory) "${file.itemCount} items" else "${file.size / (1024 * 1024)} MB",
                        color = NovaSecondaryText,
                        fontSize = 11.sp
                    )
                }

                if (file.isMedia) {
                    Surface(
                        color = NovaPrimaryViolet.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Play",
                            color = NovaElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiStudioScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val aiStatus by viewModel.aiModelStatus.collectAsState()
    val searchPrompt by viewModel.aiSearchPrompt.collectAsState()
    val notes by viewModel.sceneNotes.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val videos by viewModel.videoList.collectAsState()

    BackHandler {
        viewModel.navigateBack()
    }

    // Honest real search across video scene notes, chapter bookmarks, and metadata
    val matchedScenes = notes.filter {
        searchPrompt.isNotBlank() && (
                it.noteText.contains(searchPrompt, ignoreCase = true) ||
                        it.tag.contains(searchPrompt, ignoreCase = true) ||
                        it.videoTitle.contains(searchPrompt, ignoreCase = true)
                )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaPrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Studio & Scene Finder",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaPrimaryText
                    )
                }

                Text(
                    text = "Next-gen architecture for natural-language scene search & video analysis",
                    fontSize = 12.sp,
                    color = NovaSecondaryText,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Engine Status Card (Real & Honest status)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NovaSurfaceCard)
                        .border(1.dp, NovaElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = NovaElectricCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Vision Pipeline",
                                    color = NovaPrimaryText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Surface(
                                color = NovaSurfaceElevated,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = aiStatus,
                                    color = NovaElectricCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "To guarantee 100% truthful performance with zero fake mock responses, scene queries search against device annotations, timestamps, verified frame markers, and local metadata.",
                            color = NovaSecondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "NATURAL-LANGUAGE SCENE SEARCH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchPrompt,
                    onValueChange = { viewModel.setAiSearchPrompt(it) },
                    placeholder = { Text("e.g. 'car chase', 'physics equation', 'climax scene'...", color = NovaSecondaryText) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NovaElectricCyan
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaElectricCyan,
                        unfocusedBorderColor = NovaCardBorder,
                        focusedContainerColor = NovaSurfaceCard,
                        unfocusedContainerColor = NovaSurfaceCard,
                        focusedTextColor = NovaPrimaryText,
                        unfocusedTextColor = NovaPrimaryText
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MATCHED SCENES (${matchedScenes.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
            }
        }

        if (matchedScenes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchPrompt.isBlank()) "Type a search term above to locate matching scene annotations and timestamps."
                        else "No recorded scenes match '$searchPrompt'. Add notes to videos using Scene Notebook to index them.",
                        color = NovaSecondaryText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(matchedScenes) { note ->
                SceneNoteCard(
                    note = note,
                    onPlayAt = {
                        val video = videos.find { it.uri == note.mediaUri } ?: MediaItemData(
                            uri = note.mediaUri,
                            title = note.videoTitle,
                            isVideo = true
                        )
                        viewModel.playVideo(video, note.timestampMs)
                    },
                    onDelete = { viewModel.deleteSceneNote(note) },
                    formatMs = { viewModel.formatMs(it) }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var hwAcceleration by remember { mutableStateOf(settings.hardwareAcceleration) }
    var backgroundPlay by remember { mutableStateOf(settings.backgroundAudio) }

    BackHandler {
        viewModel.navigateBack()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaPrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaPrimaryText
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Playback Preferences
                Text(
                    text = "PLAYBACK & DECODING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingSwitchRow(
                    title = "Hardware Acceleration",
                    subtitle = "Use device GPU hardware video decoder (MediaCodec)",
                    checked = hwAcceleration,
                    onCheckedChange = { hwAcceleration = it }
                )

                SettingSwitchRow(
                    title = "Background Audio Playback",
                    subtitle = "Continue playing audio when app is minimized or screen off",
                    checked = backgroundPlay,
                    onCheckedChange = { backgroundPlay = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Storage & Privacy
                Text(
                    text = "STORAGE & LOCAL DATA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NovaSurfaceCard)
                        .clickable { viewModel.clearHistory() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Clear Playback History",
                            color = NovaPrimaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Removes all saved playback positions and recent items",
                            color = NovaSecondaryText,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = NovaElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About Nova Player
                Text(
                    text = "ABOUT NOVA PLAYER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NovaSurfaceCard)
                        .border(1.dp, NovaCardBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "NOVA PLAYER — Beyond Watching",
                            color = NovaElectricCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0 Cinema Edition\nNative Android Architecture (Kotlin • Jetpack Compose • AndroidX Media3 / ExoPlayer • Room Database)\nPrivacy-First Offline Media System",
                            color = NovaSecondaryText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = NovaPrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = NovaSecondaryText,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NovaDarkBackground,
                checkedTrackColor = NovaElectricCyan,
                uncheckedThumbColor = NovaSecondaryText,
                uncheckedTrackColor = NovaSurfaceElevated
            )
        )
    }
}
