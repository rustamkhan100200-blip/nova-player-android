package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.Screen

data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isActive: Boolean,
    val targetScreen: Screen? = null
)

@Composable
fun ToolsScreen(
    viewModel: NovaPlayerViewModel,
    modifier: Modifier = Modifier
) {
    var comingSoonTitle by remember { mutableStateOf<String?>(null) }
    var comingSoonDesc by remember { mutableStateOf<String?>(null) }
    var showHdrDialog by remember { mutableStateOf(false) }

    // HDR / Enhancement state
    var hdrToneMapping by remember { mutableStateOf(true) }
    var nightModeBoost by remember { mutableStateOf(false) }
    var vocalClarity by remember { mutableStateOf(true) }

    BackHandler {
        viewModel.navigateTo(Screen.Home)
    }

    val toolsList = remember {
        listOf(
            ToolItem(
                id = "ai_studio",
                title = "AI Studio",
                description = "On-device multimodal assistant and video metadata insights engine.",
                icon = Icons.Default.Psychology,
                isActive = true,
                targetScreen = Screen.AiStudio
            ),
            ToolItem(
                id = "subtitle_studio",
                title = "Subtitle Studio",
                description = "Precision multi-language subtitle track manager with full RTL engine.",
                icon = Icons.Default.Subtitles,
                isActive = true,
                targetScreen = Screen.SubtitleStudio
            ),
            ToolItem(
                id = "ai_scene_finder",
                title = "AI Scene Finder",
                description = "Natural-language query index over timestamps, notes, and scene markers.",
                icon = Icons.Default.AutoAwesome,
                isActive = true,
                targetScreen = Screen.AiStudio
            ),
            ToolItem(
                id = "smart_timeline",
                title = "Smart Timeline",
                description = "Interactive chapter markers, bookmark navigation, and timeline scrub.",
                icon = Icons.Default.Timeline,
                isActive = true,
                targetScreen = Screen.SmartTimeline
            ),
            ToolItem(
                id = "hdr_enhancement",
                title = "Video Enhancement & HDR",
                description = "Hardware GPU decoding, 10-bit tone mapping, and audio speech clarity.",
                icon = Icons.Default.HighQuality,
                isActive = true,
                targetScreen = null // Handled via interactive dialog
            ),
            ToolItem(
                id = "music_recognition",
                title = "Music Recognition",
                description = "Acoustic fingerprinting to identify background songs playing in videos.",
                icon = Icons.Default.GraphicEq,
                isActive = false,
                targetScreen = null
            ),
            ToolItem(
                id = "learning_mode",
                title = "Learning Mode",
                description = "A-B infinite video looper with pitch-corrected playback speed control.",
                icon = Icons.Default.School,
                isActive = true,
                targetScreen = Screen.LearningMode
            ),
            ToolItem(
                id = "scene_notebook",
                title = "Scene Notebook",
                description = "Timestamped study annotations, bookmarks, and exportable lecture notes.",
                icon = Icons.Default.EditNote,
                isActive = true,
                targetScreen = Screen.SceneNotebook
            ),
            ToolItem(
                id = "video_summarizer",
                title = "Video Summarizer",
                description = "Executive chapter summaries and key takeaway generation from media.",
                icon = Icons.Default.Summarize,
                isActive = false,
                targetScreen = null
            ),
            ToolItem(
                id = "ask_your_video",
                title = "Ask Your Video",
                description = "Interactive Q&A dialog to interrogate facts and scenes in long-form lectures.",
                icon = Icons.Default.QuestionAnswer,
                isActive = false,
                targetScreen = null
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Nova Tools",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaPrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced playback utilities, AI diagnostics, and cinema tools",
                        fontSize = 12.sp,
                        color = NovaSecondaryText
                    )
                }
            }

            // 10 Tools Cards
            items(
                items = toolsList,
                key = { it.id }
            ) { tool ->
                Surface(
                    color = NovaSurfaceCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (tool.isActive) NovaCardBorder else NovaSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {
                            if (tool.isActive) {
                                if (tool.targetScreen != null) {
                                    viewModel.navigateTo(tool.targetScreen)
                                } else if (tool.id == "hdr_enhancement") {
                                    showHdrDialog = true
                                }
                            } else {
                                comingSoonTitle = tool.title
                                comingSoonDesc = tool.description
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (tool.isActive) Brush.linearGradient(
                                            listOf(NovaPrimaryViolet, NovaElectricCyan)
                                        ) else Brush.linearGradient(
                                            listOf(NovaSurfaceElevated, NovaSurface)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = null,
                                    tint = if (tool.isActive) NovaDarkBackground else NovaSecondaryText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Status Badge
                            Surface(
                                color = if (tool.isActive) NovaElectricCyan.copy(alpha = 0.15f) else NovaAccentAmber.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (tool.isActive) NovaElectricCyan else NovaAccentAmber
                                )
                            ) {
                                Text(
                                    text = if (tool.isActive) "ACTIVE" else "COMING SOON",
                                    color = if (tool.isActive) NovaElectricCyan else NovaAccentAmber,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = tool.title,
                                color = NovaPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tool.description,
                                color = NovaSecondaryText,
                                fontSize = 10.sp,
                                maxLines = 3,
                                lineHeight = 14.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = if (tool.isActive) "Launch" else "Info",
                                color = if (tool.isActive) NovaElectricCyan else NovaSecondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (tool.isActive) NovaElectricCyan else NovaSecondaryText,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // HDR & Video Enhancement Dialog
        if (showHdrDialog) {
            AlertDialog(
                onDismissRequest = { showHdrDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = NovaElectricCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video & HDR Enhancement",
                            color = NovaPrimaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Hardware video decoding & cinema post-processing pipeline:",
                            fontSize = 12.sp,
                            color = NovaSecondaryText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("HDR Tone Mapping", color = NovaPrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("10-bit color gamut compression for OLED/LCD screens", color = NovaSecondaryText, fontSize = 11.sp)
                            }
                            Switch(
                                checked = hdrToneMapping,
                                onCheckedChange = { hdrToneMapping = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NovaDarkBackground,
                                    checkedTrackColor = NovaElectricCyan
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Vocal Clarity Audio DSP", color = NovaPrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Boost dialogue frequencies and suppress rumbling background noise", color = NovaSecondaryText, fontSize = 11.sp)
                            }
                            Switch(
                                checked = vocalClarity,
                                onCheckedChange = { vocalClarity = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NovaDarkBackground,
                                    checkedTrackColor = NovaElectricCyan
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Night Cinema Mode", color = NovaPrimaryText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Reduce contrast fatigue and limit peak volume spikes", color = NovaSecondaryText, fontSize = 11.sp)
                            }
                            Switch(
                                checked = nightModeBoost,
                                onCheckedChange = { nightModeBoost = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NovaDarkBackground,
                                    checkedTrackColor = NovaElectricCyan
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showHdrDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                    ) {
                        Text("Apply", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = NovaSurfaceCard
            )
        }

        // Honest Coming Soon Dialog
        if (comingSoonTitle != null) {
            AlertDialog(
                onDismissRequest = { comingSoonTitle = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NovaAccentAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comingSoonTitle ?: "",
                            color = NovaPrimaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = comingSoonDesc ?: "",
                            color = NovaSecondaryText,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Status: In Development. This capability requires dedicated machine learning models that are scheduled for the next major release.",
                            color = NovaAccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { comingSoonTitle = null },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricCyan)
                    ) {
                        Text("Understood", color = NovaDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = NovaSurfaceCard
            )
        }
    }
}
