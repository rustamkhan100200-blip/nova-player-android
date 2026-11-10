package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemData
import com.example.ui.theme.NovaAccentAmber
import com.example.ui.theme.NovaAccentPink
import com.example.ui.theme.NovaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoActionBottomSheet(
    video: MediaItemData,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onMoveToTrashClick: () -> Unit,
    onShowDetailsClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = NovaTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.surfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header: Title & subtitle
            Text(
                text = video.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${video.resolution ?: "1080p"} • ${video.durationFormatted} • ${video.sizeFormatted}",
                fontSize = 12.sp,
                color = colors.secondaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Items
            ActionMenuItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                iconTint = if (isFavorite) NovaAccentPink else colors.primaryText,
                title = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.PlaylistAdd,
                iconTint = colors.accent,
                title = "Add to Playlist",
                onClick = {
                    onDismiss()
                    onAddToPlaylistClick()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Share,
                iconTint = colors.primaryText,
                title = "Share Video",
                onClick = {
                    onDismiss()
                    shareMedia(context, video)
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Info,
                iconTint = colors.primaryText,
                title = "File Details & Info",
                onClick = {
                    onDismiss()
                    onShowDetailsClick()
                }
            )

            ActionMenuItem(
                icon = Icons.Default.Delete,
                iconTint = NovaAccentAmber,
                title = "Move to Recycle Bin",
                onClick = {
                    onDismiss()
                    onMoveToTrashClick()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    val colors = NovaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = colors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecycleBinConfirmationDialog(
    media: MediaItemData,
    onDismiss: () -> Unit,
    onConfirmMoveToTrash: () -> Unit
) {
    val colors = NovaTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = NovaAccentAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Move to Recycle Bin?",
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "\"${media.title}\"",
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This item will be safely stored in the Recycle Bin. You can restore it anytime or permanently delete it from Tools > Recycle Bin.",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmMoveToTrash()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaAccentAmber)
            ) {
                Text("Move to Trash", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.secondaryText)
            }
        }
    )
}

@Composable
fun VideoDetailsDialog(
    media: MediaItemData,
    onDismiss: () -> Unit
) {
    val colors = NovaTheme.colors
    val dateStr = remember(media.dateAdded) {
        try {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(media.dateAdded))
        } catch (_: Exception) {
            "Unknown"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "File Details",
                    color = colors.primaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Title", media.title)
                DetailRow("Duration", media.durationFormatted)
                DetailRow("Resolution", media.resolution ?: "1080p Full HD")
                DetailRow("File Size", media.sizeFormatted)
                DetailRow("Folder", media.folderName)
                DetailRow("Format", media.extension.uppercase())
                DetailRow("Date Added", dateStr)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = NovaTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.secondaryText,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = colors.primaryText,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun AddToPlaylistDialog(
    media: MediaItemData,
    existingPlaylists: List<String>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (playlistName: String) -> Unit,
    onCreateAndAdd: (playlistName: String) -> Unit
) {
    val colors = NovaTheme.colors
    var newPlaylistName by remember { mutableStateOf("") }
    var showCreateField by remember { mutableStateOf(existingPlaylists.isEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceCard,
        title = {
            Text(
                text = "Add to Playlist",
                color = colors.primaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (existingPlaylists.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        items(existingPlaylists) { name ->
                            Surface(
                                color = colors.surfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onAddToPlaylist(name)
                                        onDismiss()
                                    }
                            ) {
                                Text(
                                    text = name,
                                    color = colors.primaryText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (!showCreateField) {
                    TextButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = colors.accent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create New Playlist", color = colors.accent)
                    }
                } else {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Playlist name (e.g. Action, Study)", color = colors.secondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (showCreateField) {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreateAndAdd(newPlaylistName.trim())
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Save to Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.secondaryText)
            }
        }
    )
}

private fun shareMedia(context: Context, media: MediaItemData) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (media.isVideo) "video/*" else "audio/*"
            putExtra(Intent.EXTRA_TITLE, media.title)
            putExtra(Intent.EXTRA_TEXT, "Playing \"${media.title}\" on Nova Player")
            val uri = Uri.parse(media.contentUri)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Media via"))
    } catch (_: Exception) {
        // Fallback simple share
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out \"${media.title}\" on Nova Player")
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}
