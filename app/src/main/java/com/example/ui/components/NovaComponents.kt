package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.example.data.model.MediaItemData
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
import com.example.ui.viewmodel.Screen

@Composable
fun NovaGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = NovaSurfaceCard.copy(alpha = 0.9f),
    borderColor: Color = NovaCardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
fun NovaNeonButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    testTag: String = "neon_button"
) {
    val brush = if (isPrimary) {
        Brush.horizontalGradient(listOf(NovaPrimaryViolet, NovaElectricCyan))
    } else {
        Brush.horizontalGradient(listOf(NovaSurfaceElevated, NovaSurface))
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .border(
                1.dp,
                if (isPrimary) NovaElectricCyan.copy(alpha = 0.6f) else NovaCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) NovaDarkBackground else NovaPrimaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (isPrimary) NovaDarkBackground else NovaPrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun NovaBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    // Exactly 4 primary tabs: Videos | Music | Tools | Me (Requirement 14)
    val items = listOf(
        NavigationItem(Screen.VideoLibrary, "Videos", Icons.Default.Movie),
        NavigationItem(Screen.MusicLibrary, "Music", Icons.Default.MusicNote),
        NavigationItem(Screen.Tools, "Tools", Icons.Default.Build),
        NavigationItem(Screen.Me, "Me", Icons.Default.Person)
    )

    val currentColors = com.example.ui.theme.NovaTheme.colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = currentColors.background.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, currentColors.cardBorder)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 4.dp
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (currentColors.isDark) Color.Black else Color.White,
                        selectedTextColor = currentColors.accent,
                        indicatorColor = currentColors.accent,
                        unselectedIconColor = currentColors.secondaryText,
                        unselectedTextColor = currentColors.secondaryText
                    )
                )
            }
        }
    }
}

data class NavigationItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MiniPlayerBar(
    currentMedia: MediaItemData?,
    isPlaying: Boolean,
    currentPosMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentMedia == null) return

    val colors = com.example.ui.theme.NovaTheme.colors
    val progress = if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .clickable { onExpand() }
                .testTag("mini_player_bar"),
            color = colors.surfaceElevated.copy(alpha = 0.98f),
            tonalElevation = 6.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(NovaPrimaryViolet, colors.accent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentMedia.isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentMedia.title,
                            color = colors.primaryText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (currentMedia.isVideo) "Video • ${currentMedia.resolution}" else currentMedia.artist,
                            color = colors.secondaryText,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = colors.accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = colors.accent,
                    trackColor = colors.surface
                )
            }
        }
    }
}

@Composable
fun MediaPermissionBanner(
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = requiredPermissions.all { perm -> result[perm] == true }
        if (allGranted) {
            hasPermission = true
            onPermissionsGranted()
        }
    }

    if (!hasPermission) {
        Surface(
            color = NovaSurfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaElectricCyan.copy(alpha = 0.6f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NovaPrimaryViolet, NovaElectricCyan))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NovaDarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Automatic Media Discovery",
                            color = NovaPrimaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Native Android MediaStore Integration",
                            color = NovaElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Allow Nova Player to automatically discover and list all videos and music files stored on your device, just like VLC or MX Player.",
                    color = NovaSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { launcher.launch(requiredPermissions) },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Allow Media Access",
                        color = NovaDarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

