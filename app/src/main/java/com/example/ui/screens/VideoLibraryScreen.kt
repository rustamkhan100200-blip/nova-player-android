package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.PlaybackHistoryItem
import com.example.data.model.MediaItemData
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.MediaPermissionBanner
import com.example.ui.components.RecycleBinConfirmationDialog
import com.example.ui.components.VideoActionBottomSheet
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.NovaPlayerViewModel

/**
 * Responsive calculation for video grid columns:
 * - Very narrow screens (<340dp): 1 column
 * - Normal portrait phones (340dp - 599dp): 2 columns (never squished into 3 columns)
 * - Landscape / compact tablets (600dp - 899dp): 3 columns
 * - Wide landscape / large tablets (>=900dp): 4 columns
 * - List mode: always 1 column
 */
fun calculateVideoGridColumns(widthDp: Float, isGridMode: Boolean): Int {
    if (!isGridMode) return 1
    return when {
        widthDp < 340f -> 1
        widthDp < 600f -> 2
        widthDp < 900f -> 3
        else -> 4
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoLibraryScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val colors = NovaTheme.colors
    val videos by viewModel.videoList.collectAsState()
    val isScanning by viewModel.isScanningMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val history by viewModel.history.collectAsState()
    val isGridMode by viewModel.isVideoGridMode.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val libraryLayout by viewModel.libraryLayout.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showHeaderMoreMenu by remember { mutableStateOf(false) }

    // Contextual Dialog States for 3-dot and long-press
    var selectedActionVideo by remember { mutableStateOf<MediaItemData?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showRecycleDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openImportedUri(uri)
        }
    }

    val categories = listOf("All", "Favorites", "DCIM", "Movies", "Downloads", "Device Local")

    val infiniteTransition = rememberInfiniteTransition(label = "rescan_rot")
    val rescanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rescan_angle"
    )

    // Filter and Sort with memoization for fast scrolling
    val filteredVideos = remember(
        videos, searchQuery, selectedCategory, sortOrder, favorites, history
    ) {
        val filtered = videos.filter { video ->
            val matchesSearch = searchQuery.isBlank() ||
                    video.title.contains(searchQuery, ignoreCase = true) ||
                    video.artist.contains(searchQuery, ignoreCase = true) ||
                    video.folder.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> favorites.any { it.mediaUri == video.uri }
                "Device Local" -> !video.isSample
                "DCIM" -> video.folder.contains("DCIM", ignoreCase = true) || video.uri.contains("DCIM", ignoreCase = true)
                "Movies" -> video.folder.contains("Movies", ignoreCase = true) || video.uri.contains("Movies", ignoreCase = true)
                "Downloads" -> video.folder.contains("Download", ignoreCase = true) || video.uri.contains("Download", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesCategory
        }

        when (sortOrder) {
            "Name A-Z", "Name" -> filtered.sortedBy { it.title.lowercase() }
            "Name Z-A" -> filtered.sortedByDescending { it.title.lowercase() }
            "Newest First", "Newest" -> filtered.sortedByDescending { it.dateAdded }
            "Oldest First", "Oldest" -> filtered.sortedBy { it.dateAdded }
            "Largest File", "Size" -> filtered.sortedByDescending { it.sizeBytes }
            "Smallest File" -> filtered.sortedBy { it.sizeBytes }
            "Duration (Longest)", "Duration" -> filtered.sortedByDescending { it.durationMs }
            "Duration (Shortest)" -> filtered.sortedBy { it.durationMs }
            "Recently Played", "Recent" -> {
                val histMap = history.associate { it.mediaUri to it.timestamp }
                filtered.sortedByDescending { histMap[it.uri] ?: 0L }
            }
            else -> filtered
        }
    }

    // Continue Watching Videos (from recent history with progress)
    val continueWatchingList = remember(history, videos) {
        history.filter { it.isVideo && it.lastPositionMs > 2000L }
            .take(6)
            .mapNotNull { hist ->
                videos.find { it.uri == hist.mediaUri }?.let { video ->
                    video to hist
                }
            }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val screenWidth = maxWidth

        val gridCols = calculateVideoGridColumns(maxWidth.value, isGridMode)

        Column(modifier = Modifier.fillMaxSize()) {
            // Simplified, Clean, Compact Header (Priority 8)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Row: Title, Video Count, and Compact Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Video Library",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${filteredVideos.size} Videos • Hardware Decoding",
                            fontSize = 12.sp,
                            color = colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Compact Actions: Grid/List Toggle, Sort Button, and 3-Dot More Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Grid / List View Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleVideoGridMode() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceElevated)
                        ) {
                            Icon(
                                imageVector = if (isGridMode) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = if (isGridMode) "Switch to List View" else "Switch to Grid View",
                                tint = colors.accent,
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
                                    .background(colors.surfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = colors.primaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(colors.surfaceCard)
                            ) {
                                val sortList = listOf(
                                    "Recently Played",
                                    "Name A-Z",
                                    "Name Z-A",
                                    "Newest First",
                                    "Oldest First",
                                    "Largest File",
                                    "Smallest File",
                                    "Duration (Longest)",
                                    "Duration (Shortest)"
                                )
                                sortList.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order,
                                                color = if (sortOrder == order) colors.accent else colors.primaryText,
                                                fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3-Dot Header Overflow Menu (Replaces messy row of circular buttons)
                        Box {
                            IconButton(
                                onClick = { showHeaderMoreMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = colors.primaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showHeaderMoreMenu,
                                onDismissRequest = { showHeaderMoreMenu = false },
                                modifier = Modifier.background(colors.surfaceCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Import Video File", color = colors.primaryText) },
                                    leadingIcon = { Icon(Icons.Default.FileOpen, null, tint = colors.accent) },
                                    onClick = {
                                        filePickerLauncher.launch(arrayOf("video/*"))
                                        showHeaderMoreMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rescan Device Media", color = colors.primaryText) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier.rotate(if (isScanning) rescanRotation else 0f)
                                        )
                                    },
                                    onClick = {
                                        viewModel.rescanMedia()
                                        showHeaderMoreMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Density: Compact", color = colors.primaryText) },
                                    leadingIcon = { Icon(Icons.Default.ViewAgenda, null, tint = colors.secondaryText) },
                                    onClick = {
                                        viewModel.setLibraryLayout("Compact")
                                        showHeaderMoreMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Density: Comfortable", color = colors.primaryText) },
                                    leadingIcon = { Icon(Icons.Default.ViewAgenda, null, tint = colors.secondaryText) },
                                    onClick = {
                                        viewModel.setLibraryLayout("Comfortable")
                                        showHeaderMoreMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search videos, films, folders...", color = colors.secondaryText, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = colors.secondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("video_search_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.surfaceCard,
                        unfocusedContainerColor = colors.surfaceCard,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Single Clean Row of Folder / Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceElevated,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors.accent) else androidx.compose.foundation.BorderStroke(0.5.dp, colors.cardBorder),
                            modifier = Modifier.clickable { viewModel.setCategory(category) }
                        ) {
                            Text(
                                text = category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accent else colors.secondaryText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Media Permission Discovery Banner
            MediaPermissionBanner(
                onPermissionsGranted = { viewModel.loadMedia() }
            )

            // Scanning Status Indicator
            if (isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanning device storage for videos...",
                        color = colors.accent,
                        fontSize = 12.sp
                    )
                }
            }

            // Empty State
            if (filteredVideos.isEmpty() && !isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = colors.secondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No videos match your filter",
                            color = colors.primaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedCategory == "Favorites") "You haven't starred any videos yet."
                            else "Try clearing your search query or import a video file.",
                            color = colors.secondaryText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = colors.surfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                viewModel.setSearchQuery("")
                                viewModel.setCategory("All")
                            }
                        ) {
                            Text(
                                text = "Reset Filter",
                                color = colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Main Media Content: Grid View or List View
            if (isGridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridCols),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Continue Watching carousel at top of library
                    if (continueWatchingList.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
                        item(span = { GridItemSpan(gridCols) }) {
                            ContinueWatchingSection(
                                continueList = continueWatchingList,
                                onResumeClick = { video, hist ->
                                    viewModel.playVideo(video, hist.lastPositionMs)
                                }
                            )
                        }
                        item(span = { GridItemSpan(gridCols) }) {
                            Text(
                                text = "All Videos (${filteredVideos.size})",
                                color = colors.primaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }

                    items(
                        items = filteredVideos,
                        key = { it.uri }
                    ) { video ->
                        val historyItem = history.find { it.mediaUri == video.uri }
                        val isFav = favorites.any { it.mediaUri == video.uri }
                        VideoGridItem(
                            video = video,
                            historyItem = historyItem,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                viewModel.toggleFavorite(video.uri, video.title, isVideo = true)
                            },
                            onMenuClick = {
                                selectedActionVideo = video
                                showActionSheet = true
                            },
                            onClick = {
                                val resumeMs = historyItem?.lastPositionMs
                                viewModel.playVideo(video, resumeMs)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
                ) {
                    // Continue Watching carousel
                    if (continueWatchingList.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
                        item {
                            ContinueWatchingSection(
                                continueList = continueWatchingList,
                                onResumeClick = { video, hist ->
                                    viewModel.playVideo(video, hist.lastPositionMs)
                                }
                            )
                        }
                        item {
                            Text(
                                text = "All Videos (${filteredVideos.size})",
                                color = colors.primaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }

                    items(
                        items = filteredVideos,
                        key = { it.uri }
                    ) { video ->
                        val historyItem = history.find { it.mediaUri == video.uri }
                        val isFav = favorites.any { it.mediaUri == video.uri }
                        VideoListItem(
                            video = video,
                            historyItem = historyItem,
                            isFavorite = isFav,
                            onToggleFavorite = {
                                viewModel.toggleFavorite(video.uri, video.title, isVideo = true)
                            },
                            onMenuClick = {
                                selectedActionVideo = video
                                showActionSheet = true
                            },
                            onClick = {
                                val resumeMs = historyItem?.lastPositionMs
                                viewModel.playVideo(video, resumeMs)
                            }
                        )
                    }
                }
            }
        }

        // 3-Dot / Long Press Action Bottom Sheet
        if (showActionSheet && selectedActionVideo != null) {
            val v = selectedActionVideo!!
            val isFav = favorites.any { it.mediaUri == v.uri }
            VideoActionBottomSheet(
                video = v,
                isFavorite = isFav,
                onDismiss = { showActionSheet = false },
                onToggleFavorite = {
                    viewModel.toggleFavorite(v.uri, v.title, isVideo = true)
                },
                onAddToPlaylistClick = {
                    showPlaylistDialog = true
                },
                onMoveToTrashClick = {
                    showRecycleDialog = true
                },
                onShowDetailsClick = {
                    showDetailsDialog = true
                }
            )
        }

        // Move to Recycle Bin Confirmation Dialog
        if (showRecycleDialog && selectedActionVideo != null) {
            RecycleBinConfirmationDialog(
                media = selectedActionVideo!!,
                onDismiss = { showRecycleDialog = false },
                onConfirmMoveToTrash = {
                    viewModel.moveToRecycleBin(selectedActionVideo!!)
                }
            )
        }

        // File Details Dialog
        if (showDetailsDialog && selectedActionVideo != null) {
            VideoDetailsDialog(
                media = selectedActionVideo!!,
                onDismiss = { showDetailsDialog = false }
            )
        }

        // Add to Playlist Dialog
        if (showPlaylistDialog && selectedActionVideo != null) {
            val v = selectedActionVideo!!
            AddToPlaylistDialog(
                media = v,
                existingPlaylists = playlists.map { it.name },
                onDismiss = { showPlaylistDialog = false },
                onAddToPlaylist = { playlistName ->
                    playlists.find { it.name == playlistName }?.let { entity ->
                        viewModel.addVideoToPlaylist(entity.id, v)
                    }
                },
                onCreateAndAdd = { newName ->
                    viewModel.createVideoPlaylist(newName)
                    viewModel.playlists.value.find { it.name == newName }?.let { entity ->
                        viewModel.addVideoToPlaylist(entity.id, v)
                    }
                }
            )
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    continueList: List<Pair<MediaItemData, PlaybackHistoryItem>>,
    onResumeClick: (MediaItemData, PlaybackHistoryItem) -> Unit
) {
    val colors = NovaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = "Continue Watching",
            color = colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(continueList) { (video, hist) ->
                val progress = if (hist.durationMs > 0) {
                    (hist.lastPositionMs.toFloat() / hist.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Surface(
                    color = colors.surfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onResumeClick(video, hist) }
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            VideoThumbnailView(
                                uriString = video.uri,
                                durationText = video.formattedDuration(),
                                resolutionText = video.resolution,
                                progress = progress,
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = video.title,
                                color = colors.primaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Resume at ${formatHistorySecs(hist.lastPositionMs)}",
                                color = NovaAccentAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridItem(
    video: MediaItemData,
    historyItem: PlaybackHistoryItem?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMenuClick: () -> Unit,
    onClick: () -> Unit
) {
    val colors = NovaTheme.colors
    val progress = if (historyItem != null && historyItem.durationMs > 0) {
        (historyItem.lastPositionMs.toFloat() / historyItem.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = colors.surfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMenuClick
            )
    ) {
        Column {
            // Real Video Frame Thumbnail with 16:9 ratio, duration, and resolution
            Box(modifier = Modifier.fillMaxWidth()) {
                VideoThumbnailView(
                    uriString = video.uri,
                    durationText = video.formattedDuration(),
                    resolutionText = video.resolution,
                    progress = progress,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )

                // Favorite button (top-end)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Unstar" else "Star",
                        tint = if (isFavorite) NovaAccentAmber else colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Video Title (Max 2 lines, ellipsis), Info, and 3-dot Menu Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = colors.primaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.heightIn(min = 30.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${video.artist} • ${video.formattedSize()}",
                        color = colors.secondaryText,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (historyItem != null && historyItem.lastPositionMs > 2000) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Resume ${formatHistorySecs(historyItem.lastPositionMs)}",
                            color = NovaAccentAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Video Options",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(
    video: MediaItemData,
    historyItem: PlaybackHistoryItem?,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMenuClick: () -> Unit,
    onClick: () -> Unit
) {
    val colors = NovaTheme.colors
    val progress = if (historyItem != null && historyItem.durationMs > 0) {
        (historyItem.lastPositionMs.toFloat() / historyItem.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMenuClick
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real Video Frame Thumbnail with 16:9 ratio
                VideoThumbnailView(
                    uriString = video.uri,
                    durationText = video.formattedDuration(),
                    resolutionText = video.resolution,
                    progress = progress,
                    modifier = Modifier.width(110.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = colors.primaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${video.artist} • ${video.formattedSize()}",
                        color = colors.secondaryText,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = NovaPrimaryViolet.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, NovaPrimaryViolet)
                        ) {
                            Text(
                                text = video.resolution,
                                color = colors.accent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        // Saved resume position badge if previously played
                        if (historyItem != null && historyItem.lastPositionMs > 2000) {
                            Surface(
                                color = colors.surfaceElevated,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Resume ${formatHistorySecs(historyItem.lastPositionMs)}",
                                    color = NovaAccentAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Favorite Star Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Unstar" else "Star",
                        tint = if (isFavorite) NovaAccentAmber else colors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3-Dot Menu Button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Video Options",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Saved progress bar along bottom of card
            if (progress > 0.02f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = colors.accent,
                    trackColor = colors.surface
                )
            }
        }
    }
}

fun formatHistorySecs(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}
