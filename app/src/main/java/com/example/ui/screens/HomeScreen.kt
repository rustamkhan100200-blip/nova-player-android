package com.example.ui.screens

import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaybackHistoryItem
import com.example.data.model.MediaItemData
import com.example.ui.components.MediaPermissionBanner
import com.example.ui.components.NovaNeonButton
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaAccentPink
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSecondaryText
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.viewmodel.NovaPlayerViewModel
import com.example.ui.viewmodel.Screen
import java.io.File

@Composable
fun HomeScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videoList.collectAsState()
    val audioList by viewModel.audioList.collectAsState()
    val history by viewModel.history.collectAsState()

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
        // Welcoming & Functional Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(NovaPrimaryViolet, NovaElectricCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NovaDarkBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NOVA PLAYER",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NovaPrimaryText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Media Player",
                            fontSize = 12.sp,
                            color = NovaElectricCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.rescanMedia() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NovaSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan Media",
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { filePickerLauncher.launch(arrayOf("video/*", "audio/*")) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NovaSurfaceElevated)
                            .testTag("open_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Open Media",
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Automatic MediaStore Permission Discovery Banner
        item {
            MediaPermissionBanner(
                onPermissionsGranted = { viewModel.loadMedia() }
            )
        }

        // Continue Watching / Recently Played
        if (history.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTINUE WATCHING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaSecondaryText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Clear History",
                            fontSize = 12.sp,
                            color = NovaElectricCyan,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { viewModel.clearHistory() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = history,
                            key = { it.mediaUri }
                        ) { item ->
                            HistoryCard(
                                item = item,
                                onClick = {
                                    val match = videos.find { it.uri == item.mediaUri }
                                        ?: audioList.find { it.uri == item.mediaUri }
                                        ?: MediaItemData(
                                            uri = item.mediaUri,
                                            title = item.title,
                                            artist = item.artist,
                                            durationMs = item.durationMs,
                                            isVideo = item.isVideo
                                        )
                                    if (item.isVideo) {
                                        viewModel.playVideo(match, item.lastPositionMs)
                                    } else {
                                        viewModel.playAudio(match)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Quick Access to Storage Folders
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    text = "QUICK FOLDERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        FolderChip(
                            title = "Camera / DCIM",
                            icon = Icons.Default.CameraAlt,
                            onClick = {
                                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
                                viewModel.loadDirectory(path)
                                viewModel.navigateTo(Screen.FileManager)
                            }
                        )
                    }
                    item {
                        FolderChip(
                            title = "Movies",
                            icon = Icons.Default.Movie,
                            onClick = {
                                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
                                viewModel.loadDirectory(path)
                                viewModel.navigateTo(Screen.FileManager)
                            }
                        )
                    }
                    item {
                        FolderChip(
                            title = "Downloads",
                            icon = Icons.Default.Download,
                            onClick = {
                                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                                viewModel.loadDirectory(path)
                                viewModel.navigateTo(Screen.FileManager)
                            }
                        )
                    }
                    item {
                        FolderChip(
                            title = "Music",
                            icon = Icons.Default.MusicNote,
                            onClick = {
                                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
                                viewModel.loadDirectory(path)
                                viewModel.navigateTo(Screen.FileManager)
                            }
                        )
                    }
                    item {
                        FolderChip(
                            title = "All Files",
                            icon = Icons.Default.Folder,
                            onClick = {
                                viewModel.loadDirectory("")
                                viewModel.navigateTo(Screen.FileManager)
                            }
                        )
                    }
                }
            }
        }

        // All Videos Section (Home Shortcut)
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT VIDEOS (${videos.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaSecondaryText,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.VideoLibrary) }
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            color = NovaElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = videos.take(8),
                        key = { it.uri }
                    ) { video ->
                        HomeVideoCard(
                            video = video,
                            onClick = { viewModel.playVideo(video) }
                        )
                    }
                }
            }
        }

        // All Music Section (Home Shortcut)
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT MUSIC (${audioList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaSecondaryText,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.MusicLibrary) }
                    ) {
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            color = NovaElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    audioList.take(3).forEach { audio ->
                        HomeAudioRow(
                            audio = audio,
                            onClick = { viewModel.playAudio(audio) }
                        )
                    }
                }
            }
        }

        // Discreet Power Features Quick Links
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "TOOLS & UTILITIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaSecondaryText,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolPill(
                        title = "Notebook",
                        icon = Icons.Default.EditNote,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.SceneNotebook) }
                    )
                    ToolPill(
                        title = "Timeline",
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.SmartTimeline) }
                    )
                    ToolPill(
                        title = "Subtitles",
                        icon = Icons.Default.Subtitles,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.SubtitleStudio) }
                    )
                    ToolPill(
                        title = "Learning",
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.LearningMode) }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderChip(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = NovaSurfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NovaElectricCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = NovaPrimaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ToolPill(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NovaSurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NovaElectricCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = NovaPrimaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HomeVideoCard(
    video: MediaItemData,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VideoThumbnailView(
                uriString = video.uri,
                durationText = video.formattedDuration(),
                resolutionText = video.resolution,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    color = NovaPrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = video.artist,
                    color = NovaSecondaryText,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeAudioRow(
    audio: MediaItemData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NovaSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = NovaElectricCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = audio.title,
                color = NovaPrimaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${audio.artist} • ${audio.formattedDuration()}",
                color = NovaSecondaryText,
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = NovaElectricCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HistoryCard(
    item: PlaybackHistoryItem,
    onClick: () -> Unit
) {
    val progress = if (item.durationMs > 0) {
        (item.lastPositionMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (item.isVideo) {
                VideoThumbnailView(
                    uriString = item.mediaUri,
                    durationText = "Resume",
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(NovaSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NovaPrimaryViolet,
                        modifier = Modifier.size(26.dp)
                    )

                    Surface(
                        color = NovaDarkBackground.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Resume",
                            color = NovaElectricCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = NovaElectricCyan,
                    trackColor = NovaDarkBackground
                )
            }

            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = item.title,
                    color = NovaPrimaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${(progress * 100).toInt()}% watched",
                    color = NovaSecondaryText,
                    fontSize = 9.sp
                )
            }
        }
    }
}
