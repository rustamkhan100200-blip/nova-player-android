package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.FavoriteItem
import com.example.data.local.RecycleBinItem
import com.example.data.local.VideoPlaylistEntity
import com.example.data.model.MediaItemData
import com.example.ui.components.VideoThumbnailView
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaAccentGreen
import com.example.ui.theme.NovaAccentPink
import com.example.ui.theme.NovaCardBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Subtitles
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryText
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaSecondaryText
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.NovaPlayerViewModel

@Composable
fun MeScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val appTheme by viewModel.appTheme.collectAsState()
    val accentColorHex by viewModel.accentColor.collectAsState()
    val autoPlayNext by viewModel.autoPlayNext.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recycleBin by viewModel.recycleBin.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val videos by viewModel.videoList.collectAsState()
    val audios by viewModel.audioList.collectAsState()

    // Sub-dialog states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showRenamePlaylistDialog by remember { mutableStateOf<VideoPlaylistEntity?>(null) }
    var playlistToView by remember { mutableStateOf<VideoPlaylistEntity?>(null) }
    var showClearTrashDialog by remember { mutableStateOf(false) }
    var itemToDeletePermanently by remember { mutableStateOf<RecycleBinItem?>(null) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showMediaManagerDialog by remember { mutableStateOf(false) }
    var activeSubSection by remember { mutableStateOf<String?>(null) } // "playlists", "trash", "favorites"

    val accentColor = remember(accentColorHex) { Color(accentColorHex) }

    val accentOptions = listOf(
        Pair("Cyan", 0xFF00D9FF),
        Pair("Violet", 0xFF8257FF),
        Pair("Amber", 0xFFFFB74D),
        Pair("Emerald", 0xFF00E676),
        Pair("Neon Pink", 0xFFFF007F)
    )

    val colors = NovaTheme.colors

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Me Header Profile Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NovaSurfaceElevated,
                                NovaSurfaceCard,
                                NovaPrimaryViolet.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(1.dp, NovaCardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NovaPrimaryViolet, accentColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NovaDarkBackground,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NOVA User",
                            color = NovaPrimaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Beyond Watching • Native Android Player",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "${videos.size} Videos",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "•",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${audios.size} Tracks",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "•",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${playlists.size} Playlists",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Personal Media Collections
        item {
            Text(
                text = "MEDIA COLLECTIONS",
                color = NovaSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Video Playlists Row Card
                MeMenuCard(
                    icon = Icons.Default.PlaylistPlay,
                    iconTint = NovaElectricCyan,
                    title = "Video Playlists",
                    subtitle = "${playlists.size} custom video collections",
                    onClick = { activeSubSection = if (activeSubSection == "playlists") null else "playlists" },
                    trailingText = if (playlists.isNotEmpty()) "${playlists.size}" else "Create"
                )

                AnimatedVisibility(visible = activeSubSection == "playlists") {
                    PlaylistsSubSection(
                        playlists = playlists,
                        onCreatePlaylist = { showCreatePlaylistDialog = true },
                        onRename = { showRenamePlaylistDialog = it },
                        onDelete = { viewModel.deleteVideoPlaylist(it.id) },
                        onSelect = { playlistToView = it }
                    )
                }

                // Favorites Row Card
                MeMenuCard(
                    icon = Icons.Default.Star,
                    iconTint = NovaAccentAmber,
                    title = "Favorites",
                    subtitle = "${favorites.size} starred videos & songs",
                    onClick = { activeSubSection = if (activeSubSection == "favorites") null else "favorites" },
                    trailingText = "${favorites.size}"
                )

                AnimatedVisibility(visible = activeSubSection == "favorites") {
                    FavoritesSubSection(
                        favorites = favorites,
                        videos = videos,
                        audios = audios,
                        onPlayVideo = { viewModel.playVideo(it) },
                        onPlayAudio = { viewModel.playAudio(it) },
                        onRemoveFavorite = { viewModel.toggleFavorite(it.mediaUri, it.title, it.isVideo) }
                    )
                }

                // Recycle Bin Row Card
                val trashSpace = remember(recycleBin) {
                    recycleBin.sumOf { it.sizeBytes }
                }
                MeMenuCard(
                    icon = Icons.Default.Delete,
                    iconTint = NovaAccentPink,
                    title = "Recycle Bin",
                    subtitle = if (recycleBin.isEmpty()) "Empty" else "${recycleBin.size} items • ${viewModel.formatBytes(trashSpace)} recoverable",
                    onClick = { activeSubSection = if (activeSubSection == "trash") null else "trash" },
                    trailingText = if (recycleBin.isNotEmpty()) "${recycleBin.size}" else null
                )

                AnimatedVisibility(visible = activeSubSection == "trash") {
                    RecycleBinSubSection(
                        recycleBin = recycleBin,
                        onRestore = { viewModel.restoreFromRecycleBin(it) },
                        onDeletePermanently = { itemToDeletePermanently = it },
                        onClearAll = { showClearTrashDialog = true },
                        viewModel = viewModel
                    )
                }

                // Media Manager Row Card
                MeMenuCard(
                    icon = Icons.Default.Storage,
                    iconTint = NovaAccentGreen,
                    title = "Media Manager",
                    subtitle = "Storage breakdown, largest files & duplicates",
                    onClick = { showMediaManagerDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section: Playback & App Preferences
        item {
            Text(
                text = "PLAYER & APPEARANCE",
                color = NovaSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item {
            Surface(
                color = NovaSurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auto Play Next Toggle (Priority 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Play Next Video",
                                color = NovaPrimaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically play the next video in queue when current ends",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoPlayNext,
                            onCheckedChange = { viewModel.setAutoPlayNext(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NovaDarkBackground,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = NovaSecondaryText,
                                uncheckedTrackColor = NovaSurfaceElevated
                            ),
                            modifier = Modifier.testTag("autoplay_next_switch")
                        )
                    }

                    Divider(
                        color = NovaCardBorder,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // App Theme Selector
                    Text(
                        text = "App Theme",
                        color = NovaPrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Dark", "Light", "System").forEach { mode ->
                            val isSelected = appTheme.equals(mode, ignoreCase = true)
                            Surface(
                                color = if (isSelected) accentColor.copy(alpha = 0.2f) else NovaSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) accentColor else NovaCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAppTheme(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            "Dark" -> Icons.Default.DarkMode
                                            "Light" -> Icons.Default.LightMode
                                            else -> Icons.Default.Palette
                                        },
                                        contentDescription = mode,
                                        tint = if (isSelected) accentColor else NovaSecondaryText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mode,
                                        color = if (isSelected) NovaPrimaryText else NovaSecondaryText,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Accent Color Selection
                    Text(
                        text = "Accent Color",
                        color = NovaPrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accentOptions) { (name, hex) ->
                            val isChosen = accentColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        2.dp,
                                        if (isChosen) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section: System & Support
        item {
            Text(
                text = "SUPPORT & PRIVACY",
                color = NovaSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MeMenuCard(
                    icon = Icons.Default.HelpOutline,
                    iconTint = NovaElectricCyan,
                    title = "Help Center",
                    subtitle = "Gesture guides, media troubleshooting & FAQ",
                    onClick = { showHelpCenterDialog = true }
                )

                MeMenuCard(
                    icon = Icons.Default.Security,
                    iconTint = NovaAccentGreen,
                    title = "Privacy Policy",
                    subtitle = "100% on-device processing • No remote telemetry",
                    onClick = { showPrivacyPolicyDialog = true }
                )

                // Version Info Card
                Surface(
                    color = NovaSurfaceCard.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NovaSecondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NOVA PLAYER — Beyond Watching",
                                color = NovaPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version 1.2.0 • Media3 ExoPlayer • Android Compose",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & SUB-SCREENS
    // ==========================================

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Video Playlist", color = NovaPrimaryText) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaElectricCyan,
                        unfocusedBorderColor = NovaCardBorder,
                        focusedTextColor = NovaPrimaryText,
                        unfocusedTextColor = NovaPrimaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createVideoPlaylist(playlistName)
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                ) {
                    Text("Create", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = NovaSecondaryText)
                }
            },
            containerColor = NovaSurfaceCard
        )
    }

    // Rename Playlist Dialog
    if (showRenamePlaylistDialog != null) {
        val target = showRenamePlaylistDialog!!
        var newName by remember { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { showRenamePlaylistDialog = null },
            title = { Text("Rename Playlist", color = NovaPrimaryText) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaElectricCyan,
                        unfocusedBorderColor = NovaCardBorder,
                        focusedTextColor = NovaPrimaryText,
                        unfocusedTextColor = NovaPrimaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renameVideoPlaylist(target.id, newName)
                            showRenamePlaylistDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                ) {
                    Text("Save", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenamePlaylistDialog = null }) {
                    Text("Cancel", color = NovaSecondaryText)
                }
            },
            containerColor = NovaSurfaceCard
        )
    }

    // Permanent Delete Confirmation Dialog
    if (itemToDeletePermanently != null) {
        val target = itemToDeletePermanently!!
        AlertDialog(
            onDismissRequest = { itemToDeletePermanently = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = NovaAccentPink) },
            title = { Text("Permanent Deletion", color = NovaPrimaryText) },
            text = {
                Text(
                    "Are you sure you want to permanently remove \"${target.title}\" from your Recycle Bin? This action cannot be undone.",
                    color = NovaSecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteTrash(target)
                        itemToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaAccentPink)
                ) {
                    Text("Delete Forever", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeletePermanently = null }) {
                    Text("Cancel", color = NovaSecondaryText)
                }
            },
            containerColor = NovaSurfaceCard
        )
    }

    // Clear All Trash Dialog
    if (showClearTrashDialog) {
        AlertDialog(
            onDismissRequest = { showClearTrashDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = NovaAccentPink) },
            title = { Text("Empty Recycle Bin?", color = NovaPrimaryText) },
            text = {
                Text(
                    "Permanently delete all ${recycleBin.size} items from the Recycle Bin?",
                    color = NovaSecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllRecycleBin()
                        showClearTrashDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaAccentPink)
                ) {
                    Text("Empty Bin", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearTrashDialog = false }) {
                    Text("Cancel", color = NovaSecondaryText)
                }
            },
            containerColor = NovaSurfaceCard
        )
    }

    // Help Center Modal
    if (showHelpCenterDialog) {
        HelpCenterDialog(onDismiss = { showHelpCenterDialog = false })
    }

    // Privacy Policy Modal
    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicyDialog = false })
    }

    // Media Manager Modal
    if (showMediaManagerDialog) {
        MediaManagerDialog(
            videos = videos,
            audios = audios,
            viewModel = viewModel,
            onDismiss = { showMediaManagerDialog = false }
        )
    }
}

@Composable
fun MeMenuCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingText: String? = null
) {
    Surface(
        color = NovaSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = NovaPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (trailingText != null) {
                Surface(
                    color = iconTint.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = trailingText,
                        color = iconTint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NovaSecondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PlaylistsSubSection(
    playlists: List<VideoPlaylistEntity>,
    onCreatePlaylist: () -> Unit,
    onRename: (VideoPlaylistEntity) -> Unit,
    onDelete: (VideoPlaylistEntity) -> Unit,
    onSelect: (VideoPlaylistEntity) -> Unit
) {
    Surface(
        color = NovaSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Playlists",
                    color = NovaPrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Button(
                    onClick = onCreatePlaylist,
                    colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = NovaDarkBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New Playlist",
                        color = NovaDarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (playlists.isEmpty()) {
                Text(
                    text = "No custom playlists created yet. Create a playlist to group your favorite videos.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                playlists.forEach { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaSurfaceCard)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = playlist.name,
                            color = NovaPrimaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onRename(playlist) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = NovaSecondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(playlist) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = NovaAccentPink,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesSubSection(
    favorites: List<FavoriteItem>,
    videos: List<MediaItemData>,
    audios: List<MediaItemData>,
    onPlayVideo: (MediaItemData) -> Unit,
    onPlayAudio: (MediaItemData) -> Unit,
    onRemoveFavorite: (FavoriteItem) -> Unit
) {
    Surface(
        color = NovaSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Favorite Items",
                color = NovaPrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (favorites.isEmpty()) {
                Text(
                    text = "No favorites yet. Tap the star icon on any video or song to add it here.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                favorites.forEach { fav ->
                    val matchingMedia = if (fav.isVideo) {
                        videos.find { it.uri == fav.mediaUri }
                    } else {
                        audios.find { it.uri == fav.mediaUri }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaSurfaceCard)
                            .clickable {
                                if (matchingMedia != null) {
                                    if (matchingMedia.isVideo) onPlayVideo(matchingMedia) else onPlayAudio(matchingMedia)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (fav.isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (fav.isVideo) NovaElectricCyan else NovaPrimaryViolet,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fav.title,
                                color = NovaPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (fav.isVideo) "Video" else "Audio Track",
                                color = NovaSecondaryText,
                                fontSize = 10.sp
                            )
                        }
                        IconButton(
                            onClick = { onRemoveFavorite(fav) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Unstar",
                                tint = NovaAccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecycleBinSubSection(
    recycleBin: List<RecycleBinItem>,
    onRestore: (RecycleBinItem) -> Unit,
    onDeletePermanently: (RecycleBinItem) -> Unit,
    onClearAll: () -> Unit,
    viewModel: NovaPlayerViewModel
) {
    Surface(
        color = NovaSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recycle Bin",
                    color = NovaPrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (recycleBin.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Empty Bin", color = NovaAccentPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recycleBin.isEmpty()) {
                Text(
                    text = "Recycle Bin is empty. Safely deleted files appear here before permanent deletion.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                recycleBin.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaSurfaceCard)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = NovaPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${viewModel.formatBytes(item.sizeBytes)} • ${viewModel.formatMs(item.durationMs)}",
                                color = NovaSecondaryText,
                                fontSize = 10.sp
                            )
                        }
                        IconButton(
                            onClick = { onRestore(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore",
                                tint = NovaAccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeletePermanently(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Permanent Delete",
                                tint = NovaAccentPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCenterDialog(onDismiss: () -> Unit) {
    val colors = NovaTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = colors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("NOVA Player Help Center", color = colors.primaryText, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(440.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Gesture Controls Card
                item {
                    HelpGuideCard(
                        title = "1. Gesture Controls",
                        icon = Icons.Default.TouchApp,
                        iconTint = colors.accent,
                        items = listOf(
                            "Double-Tap Right Half: Skip forward 10s (+10s indicator)",
                            "Double-Tap Left Half: Rewind 10s (-10s indicator)",
                            "Vertical Drag Left: Precision screen brightness adjustment (0-100%)",
                            "Vertical Drag Right: Audio volume control (0-100%)",
                            "Single Tap Screen: Toggle HUD player controls visibility",
                            "Frame Stepping: Frame-by-frame forward / back when paused"
                        )
                    )
                }

                // 2. Audio-Only Mode Card
                item {
                    HelpGuideCard(
                        title = "2. Audio-Only Mode",
                        icon = Icons.Default.Headphones,
                        iconTint = NovaAccentAmber,
                        items = listOf(
                            "Tap the Headphone icon in the video player top bar",
                            "Turns off the video surface and displays an animated visualizer",
                            "Saves significant battery, CPU, and GPU power",
                            "Allows continuous background playback when switching apps or locking screen"
                        )
                    )
                }

                // 3. Playlists & Queue Card
                item {
                    HelpGuideCard(
                        title = "3. Video Playlists & Queue",
                        icon = Icons.Default.PlaylistPlay,
                        iconTint = NovaPrimaryViolet,
                        items = listOf(
                            "Long-press or tap 3-dots on any video to add to a playlist",
                            "Organize study series, movie collections, or workout tutorials",
                            "Use Skip Next / Previous to navigate playlist queues",
                            "Enable 'Auto Play Next Video' in Me settings for continuous watching"
                        )
                    )
                }

                // 4. Safe Recycle Bin Card
                item {
                    HelpGuideCard(
                        title = "4. Safe Recycle Bin System",
                        icon = Icons.Default.Delete,
                        iconTint = NovaAccentPink,
                        items = listOf(
                            "Deleting a video asks for confirmation and moves it to the Recycle Bin",
                            "Never lose files accidentally — restore anytime with 1 tap",
                            "Permanently empty the bin from Tools > Recycle Bin whenever ready"
                        )
                    )
                }

                // 5. Subtitle Studio Card
                item {
                    HelpGuideCard(
                        title = "5. Subtitle Studio & RTL",
                        icon = Icons.Default.Subtitles,
                        iconTint = colors.accent,
                        items = listOf(
                            "Subtitles are turned OFF by default for an unobstructed view",
                            "Supported tracks: English, Pashto (پښتو) [RTL], and Dari (دری) [RTL]",
                            "Automatic Right-to-Left (RTL) text shaping for Arabic & Persian scripts",
                            "Customize text size (Small to XL) and color (White, Yellow, Cyan)"
                        )
                    )
                }

                // 6. App Themes & Accent Colors Card
                item {
                    HelpGuideCard(
                        title = "6. App Themes & Custom Accents",
                        icon = Icons.Default.Palette,
                        iconTint = NovaAccentGreen,
                        items = listOf(
                            "Choose from Dark, Light, or System default modes",
                            "5 Dynamic Accent colors: Electric Cyan, Neon Violet, Amber, Emerald, Pink",
                            "All theme settings persist across app restarts"
                        )
                    )
                }

                // 7. Automatic Media Discovery Card
                item {
                    HelpGuideCard(
                        title = "7. Media Discovery & Permissions",
                        icon = Icons.Default.Storage,
                        iconTint = colors.accent,
                        items = listOf(
                            "NOVA automatically scans internal storage & SD cards for videos and audio",
                            "Supported formats: MP4, MKV, WebM, 3GP, AVI, MP3, FLAC, WAV, M4A",
                            "100% private and on-device — no media is ever uploaded"
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.surfaceCard
    )
}

@Composable
private fun HelpGuideCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    items: List<String>
) {
    val colors = NovaTheme.colors
    Surface(
        color = colors.surfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { stepText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        color = iconTint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = stepText,
                        color = colors.secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = NovaAccentGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Privacy Policy", color = NovaPrimaryText)
            }
        },
        text = {
            Column(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "100% On-Device & Private",
                    color = NovaAccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "NOVA Player is designed with a strict zero-telemetry architecture. All media decoding, thumbnail generation, playlist data, and playback history remain strictly on your local device.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Permissions Transparency",
                    color = NovaAccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "• READ_MEDIA_VIDEO / READ_MEDIA_AUDIO: Used solely to discover and index your local video and audio files.\n" +
                            "• FOREGROUND_SERVICE: Used to play audio in the background while you multitask.\n" +
                            "• INTERNET: Used only if you explicitly choose to stream a network video URL.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "No Third-Party Trackers",
                    color = NovaAccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "NOVA Player contains zero analytics SDKs, advertising SDKs, or background tracking services.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NovaAccentGreen)
            ) {
                Text("Understood", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = NovaSurfaceCard
    )
}

@Composable
fun MediaManagerDialog(
    videos: List<MediaItemData>,
    audios: List<MediaItemData>,
    viewModel: NovaPlayerViewModel,
    onDismiss: () -> Unit
) {
    val totalVideoBytes = remember(videos) { videos.sumOf { it.sizeBytes } }
    val totalAudioBytes = remember(audios) { audios.sumOf { it.sizeBytes } }
    val totalBytes = totalVideoBytes + totalAudioBytes

    // Find largest 5 files
    val largestFiles = remember(videos) {
        videos.sortedByDescending { it.sizeBytes }.take(5)
    }

    // Duplicate detection based on matching file size and duration
    val duplicates = remember(videos) {
        videos.groupBy { Pair(it.sizeBytes, it.durationMs) }
            .filter { it.value.size > 1 && it.key.first > 0L }
            .values.flatten()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = NovaElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Media Storage Manager", color = NovaPrimaryText)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Total Media Storage",
                                color = NovaSecondaryText,
                                fontSize = 11.sp
                            )
                            Text(
                                viewModel.formatBytes(totalBytes),
                                color = NovaElectricCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Videos: ${videos.size} (${viewModel.formatBytes(totalVideoBytes)})",
                                    color = NovaPrimaryText,
                                    fontSize = 11.sp
                                )
                                Text(
                                    "Music: ${audios.size} (${viewModel.formatBytes(totalAudioBytes)})",
                                    color = NovaPrimaryText,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Largest Video Files",
                        color = NovaPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(largestFiles) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaSurfaceElevated)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Movie,
                            contentDescription = null,
                            tint = NovaElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            file.title,
                            color = NovaPrimaryText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            viewModel.formatBytes(file.sizeBytes),
                            color = NovaSecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (duplicates.isNotEmpty()) {
                    item {
                        Text(
                            "Potential Duplicates Found (${duplicates.size})",
                            color = NovaAccentPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(duplicates) { dup ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaSurfaceElevated)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                dup.title,
                                color = NovaPrimaryText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                viewModel.formatBytes(dup.sizeBytes),
                                color = NovaAccentPink,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
            ) {
                Text("Close", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = NovaSurfaceCard
    )
}
