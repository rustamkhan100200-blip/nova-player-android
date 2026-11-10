package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItemData
import com.example.ui.theme.NovaElectricCyan
import com.example.ui.theme.NovaPrimaryViolet
import com.example.ui.theme.NovaTheme

@Composable
fun AudioOnlyVisualizer(
    media: MediaItemData?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = NovaTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isPlaying) 0.95f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 1.0f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, delayMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isPlaying) 0.85f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isPlaying) 0.92f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(410, delayMillis = 50, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isPlaying) 0.78f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, delayMillis = 110, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b5"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060911)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Glowing circular headphone badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                colors.accent.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, colors.accent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = "Audio Only Mode",
                    tint = colors.accent,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated EQ spectrum bars
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(bar1, bar2, bar3, bar4, bar5, bar2, bar4, bar1, bar3)
                heights.forEach { factor ->
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height((60.dp * factor).coerceAtLeast(8.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.accent, NovaPrimaryViolet)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title and artist
            Text(
                text = media?.title ?: "Nova Player Audio",
                color = colors.primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = media?.artist ?: "Audio-Only Background Stream",
                color = colors.secondaryText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Status chip
            Surface(
                color = colors.surfaceCard.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AUDIO-ONLY MODE • Display Off Savings",
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
