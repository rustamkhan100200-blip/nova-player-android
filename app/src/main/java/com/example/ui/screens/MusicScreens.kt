package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemData
import com.example.ui.components.MediaPermissionBanner
import com.example.ui.components.NovaNeonButton
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaGlassBorder
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSecondaryText
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.viewmodel.NovaPlayerViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MusicLibraryScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val audioList by viewModel.audioList.collectAsState()
    val isScanning by viewModel.isScanningMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPlayingMedia by viewModel.playbackManager.currentMedia.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val isGridMode by viewModel.isMusicGridMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Title") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showHeaderMoreMenu by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openImportedUri(uri)
        }
    }

    val categories = listOf("All", "Favorites", "Music", "Downloads", "Device Local")

    val infiniteTransition = rememberInfiniteTransition(label = "rescan_audio")
    val rescanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rescan_audio_angle"
    )

    val filteredAudio = remember(audioList, searchQuery, sortOrder, selectedCategory, favorites) {
        val filtered = audioList.filter { audio ->
            val matchesSearch = searchQuery.isBlank() ||
                    audio.title.contains(searchQuery, ignoreCase = true) ||
                    audio.artist.contains(searchQuery, ignoreCase = true) ||
                    audio.folder.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> favorites.any { it.mediaUri == audio.uri }
                "Music" -> audio.folder.contains("Music", ignoreCase = true) || audio.uri.contains("Music", ignoreCase = true)
                "Downloads" -> audio.folder.contains("Download", ignoreCase = true) || audio.uri.contains("Download", ignoreCase = true)
                "Device Local" -> !audio.isSample
                else -> true
            }
            matchesSearch && matchesCategory
        }

        when (sortOrder) {
            "Name A-Z", "Title" -> filtered.sortedBy { it.title.lowercase() }
            "Name Z-A" -> filtered.sortedByDescending { it.title.lowercase() }
            "Artist" -> filtered.sortedBy { it.artist.lowercase() }
            "Duration (Longest)", "Duration" -> filtered.sortedByDescending { it.durationMs }
            "Duration (Shortest)" -> filtered.sortedBy { it.durationMs }
            "Newest First" -> filtered.sortedByDescending { it.dateAdded }
            "Oldest First" -> filtered.sortedBy { it.dateAdded }
            "Largest File" -> filtered.sortedByDescending { it.sizeBytes }
            else -> filtered.sortedBy { it.title.lowercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        // Sticky Top Section: Header, Controls, Search Field & Folder Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Audio Count (Never crowded, strictly single-line ellipsis)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Music Library",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaPrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${filteredAudio.size} Audio Tracks Available",
                        fontSize = 12.sp,
                        color = NovaSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Compact, responsive 3-action toolbar: Grid/List, Sort, 3-dot More Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Grid / List View Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleMusicGridMode() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NovaSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = if (isGridMode) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridMode) "Switch to List View" else "Switch to Grid View",
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sort Menu Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NovaSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = NovaPrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(NovaSurfaceCard)
                        ) {
                            val musicSorts = listOf(
                                "Name A-Z",
                                "Name Z-A",
                                "Artist",
                                "Duration (Longest)",
                                "Duration (Shortest)",
                                "Newest First",
                                "Oldest First",
                                "Largest File"
                            )
                            musicSorts.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order,
                                            color = if (sortOrder == order) NovaElectricCyan else NovaPrimaryText,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        sortOrder = order
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 3-Dot More Menu Button (Rescan, Import, Play All, Shuffle)
                    Box {
                        IconButton(
                            onClick = { showHeaderMoreMenu = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NovaSurfaceElevated)
                                .testTag("music_more_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = NovaPrimaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showHeaderMoreMenu,
                            onDismissRequest = { showHeaderMoreMenu = false },
                            modifier = Modifier.background(NovaSurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isScanning) "Scanning Audio..." else "Rescan Audio", color = NovaPrimaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = NovaElectricCyan,
                                        modifier = Modifier.rotate(if (isScanning) rescanRotation else 0f)
                                    )
                                },
                                onClick = {
                                    viewModel.rescanMedia()
                                    showHeaderMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Audio File", color = NovaPrimaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = null,
                                        tint = NovaElectricCyan
                                    )
                                },
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("audio/*"))
                                    showHeaderMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Play All", color = NovaPrimaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = NovaElectricCyan
                                    )
                                },
                                onClick = {
                                    if (filteredAudio.isNotEmpty()) {
                                        viewModel.playbackManager.playQueue(filteredAudio, 0)
                                        viewModel.navigateTo(Screen.MusicPlayer)
                                    }
                                    showHeaderMoreMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Shuffle All", color = NovaPrimaryText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = null,
                                        tint = NovaElectricCyan
                                    )
                                },
                                onClick = {
                                    if (filteredAudio.isNotEmpty()) {
                                        viewModel.playbackManager.playQueue(filteredAudio.shuffled(), 0)
                                        viewModel.navigateTo(Screen.MusicPlayer)
                                    }
                                    showHeaderMoreMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Field with Clear Button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search tracks, artists, albums...", color = NovaSecondaryText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NovaElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = NovaSecondaryText
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("audio_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaElectricCyan,
                    unfocusedBorderColor = NovaCardBorder,
                    focusedContainerColor = NovaSurfaceCard,
                    unfocusedContainerColor = NovaSurfaceCard,
                    focusedTextColor = NovaPrimaryText,
                    unfocusedTextColor = NovaPrimaryText
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Folder & Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Brush.horizontalGradient(
                                    listOf(NovaPrimaryViolet, NovaElectricCyan)
                                ) else Brush.horizontalGradient(
                                    listOf(NovaSurfaceCard, NovaSurfaceCard)
                                )
                            )
                            .border(
                                1.dp,
                                if (isSelected) NovaElectricCyan else NovaCardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NovaDarkBackground else NovaPrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Play All & Shuffle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NovaNeonButton(
                    text = "Play All",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        if (filteredAudio.isNotEmpty()) {
                            viewModel.playbackManager.playQueue(filteredAudio, 0)
                            viewModel.navigateTo(Screen.MusicPlayer)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                NovaNeonButton(
                    text = "Shuffle",
                    icon = Icons.Default.Shuffle,
                    onClick = {
                        if (filteredAudio.isNotEmpty()) {
                            val shuffled = filteredAudio.shuffled()
                            viewModel.playbackManager.playQueue(shuffled, 0)
                            viewModel.navigateTo(Screen.MusicPlayer)
                        }
                    },
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Automatic MediaStore Permission Discovery Banner
        MediaPermissionBanner(
            onPermissionsGranted = { viewModel.loadMedia() }
        )

        // Scanning Status Indicator
        if (isScanning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = NovaElectricCyan,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scanning device storage for audio files...",
                    color = NovaElectricCyan,
                    fontSize = 12.sp
                )
            }
        }

        // Empty Search Results State
        if (filteredAudio.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NovaSecondaryText,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No audio tracks found",
                        color = NovaPrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedCategory == "Favorites") "You haven't starred any audio tracks yet. Tap the star icon on any track to add it to Favorites."
                        else "Clear your search or tap the folder button above to import music files from device storage.",
                        color = NovaSecondaryText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable {
                            viewModel.setSearchQuery("")
                            selectedCategory = "All"
                        }
                    ) {
                        Text(
                            text = "Reset Filter",
                            color = NovaElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Media List / Grid Rendering
        if (isGridMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredAudio,
                    key = { it.uri }
                ) { audio ->
                    val isCurrent = currentPlayingMedia?.uri == audio.uri
                    val isFav = favorites.any { it.mediaUri == audio.uri }
                    AudioGridItem(
                        audio = audio,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        isFavorite = isFav,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(audio.uri, audio.title, isVideo = false)
                        },
                        onClick = { viewModel.playAudio(audio) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
            ) {
                items(
                    items = filteredAudio,
                    key = { it.uri }
                ) { audio ->
                    val isCurrent = currentPlayingMedia?.uri == audio.uri
                    val isFav = favorites.any { it.mediaUri == audio.uri }
                    AudioListItem(
                        audio = audio,
                        isCurrent = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        isFavorite = isFav,
                        onToggleFavorite = {
                            viewModel.toggleFavorite(audio.uri, audio.title, isVideo = false)
                        },
                        onClick = { viewModel.playAudio(audio) }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioGridItem(
    audio: MediaItemData,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = if (isCurrent) NovaSurfaceElevated else NovaSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrent) NovaElectricCyan.copy(alpha = 0.8f) else NovaCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(
                        if (isCurrent) Brush.linearGradient(
                            listOf(NovaPrimaryViolet.copy(alpha = 0.4f), NovaElectricCyan.copy(alpha = 0.4f))
                        ) else Brush.linearGradient(
                            listOf(NovaSurfaceElevated, NovaDarkBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) NovaElectricCyan else NovaSecondaryText,
                    modifier = Modifier.size(36.dp)
                )

                // Favorite Star Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Unstar" else "Star",
                        tint = if (isFavorite) NovaAccentAmber else NovaSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Duration badge
                Surface(
                    color = NovaDarkBackground.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                ) {
                    Text(
                        text = audio.formattedDuration(),
                        color = NovaPrimaryText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = audio.title,
                    color = if (isCurrent) NovaElectricCyan else NovaPrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = audio.artist,
                    color = NovaSecondaryText,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AudioListItem(
    audio: MediaItemData,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrent) NovaSurfaceElevated else NovaSurfaceCard)
            .border(
                1.dp,
                if (isCurrent) NovaElectricCyan.copy(alpha = 0.8f) else NovaCardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isCurrent) Brush.linearGradient(
                        listOf(NovaPrimaryViolet, NovaElectricCyan)
                    ) else Brush.linearGradient(
                        listOf(NovaSurfaceElevated, NovaSurface)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isCurrent) NovaDarkBackground else NovaElectricCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = audio.title,
                color = if (isCurrent) NovaElectricCyan else NovaPrimaryText,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${audio.artist} • ${audio.formattedDuration()}",
                color = NovaSecondaryText,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        // Aligned Favorite Star and More Actions (No oversized unnecessary badges)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Unstar" else "Star",
                    tint = if (isFavorite) NovaAccentAmber else NovaSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = NovaSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentMedia by viewModel.playbackManager.currentMedia.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val currentPosMs by viewModel.playbackManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.playbackManager.durationMs.collectAsState()
    val volume by viewModel.playbackManager.volume.collectAsState()
    val isMuted by viewModel.playbackManager.isMuted.collectAsState()
    val queue by viewModel.playbackManager.queue.collectAsState()
    var showQueueSheet by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("Cinematic Bass") }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    BackHandler {
        viewModel.onBackPressed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onBackPressed() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NovaPrimaryText
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaSecondaryText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentMedia?.folder ?: "Music",
                        fontSize = 13.sp,
                        color = NovaElectricCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { showQueueSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = NovaPrimaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Vinyl Record Disk
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NovaSurfaceElevated, NovaSurfaceCard, NovaDarkBackground)
                        )
                    )
                    .border(3.dp, NovaPrimaryViolet.copy(alpha = 0.5f), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl grooves rings
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .border(1.dp, NovaCardBorder, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .border(1.dp, NovaGlassBorder, CircleShape)
                )

                // Center Label
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NovaPrimaryViolet, NovaElectricCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NovaDarkBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Track Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentMedia?.title ?: "Select a track",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaPrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentMedia?.artist ?: "Nova Player",
                    fontSize = 13.sp,
                    color = NovaSecondaryText
                )
            }

            // Timeline & Scrubber
            Column(modifier = Modifier.fillMaxWidth()) {
                val sliderValue = if (isDraggingSlider) dragPosition else {
                    if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                }

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isDraggingSlider = true
                        dragPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        val targetMs = (dragPosition * durationMs).toLong()
                        viewModel.playbackManager.seekTo(targetMs)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = NovaElectricCyan,
                        activeTrackColor = NovaElectricCyan,
                        inactiveTrackColor = NovaSurfaceElevated
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = viewModel.formatMs(currentPosMs),
                        color = NovaSecondaryText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = viewModel.formatMs(durationMs),
                        color = NovaSecondaryText,
                        fontSize = 12.sp
                    )
                }
            }

            // Primary Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.playbackManager.previous() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = NovaPrimaryText,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NovaPrimaryViolet, NovaElectricCyan)
                            )
                        )
                        .clickable { viewModel.playbackManager.togglePlayPause() }
                        .testTag("music_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = NovaDarkBackground,
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(onClick = { viewModel.playbackManager.next() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = NovaPrimaryText,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Volume Control Slider (Priority 5: Reliable Volume Controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.playbackManager.toggleMute() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted || volume <= 0f) Icons.Default.VolumeMute else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = NovaElectricCyan
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Slider(
                    value = volume,
                    onValueChange = { viewModel.playbackManager.setVolume(it) },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = NovaElectricCyan,
                        activeTrackColor = NovaElectricCyan,
                        inactiveTrackColor = NovaSurfaceElevated
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = NovaSecondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Audio DSP Equalizer Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Bass Boost", "Spatial 3D", "Vocal High", "Studio Flat").forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        color = if (isSelected) NovaSurfaceElevated else NovaSurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NovaElectricCyan else NovaCardBorder
                        ),
                        modifier = Modifier.clickable { selectedPreset = preset }
                    ) {
                        Text(
                            text = preset,
                            color = if (isSelected) NovaElectricCyan else NovaSecondaryText,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Playback Queue Bottom Sheet
        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = NovaSurfaceCard
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Playback Queue (${queue.size} Tracks)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaPrimaryText
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(
                            items = queue,
                            key = { it.uri }
                        ) { track ->
                            val isCurrent = currentMedia?.uri == track.uri
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) NovaSurfaceElevated else Color.Transparent)
                                    .clickable {
                                        viewModel.playbackManager.playQueue(queue, queue.indexOf(track))
                                        showQueueSheet = false
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCurrent) Icons.Default.Equalizer else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isCurrent) NovaElectricCyan else NovaSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = if (isCurrent) NovaElectricCyan else NovaPrimaryText,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = track.artist,
                                        color = NovaSecondaryText,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
