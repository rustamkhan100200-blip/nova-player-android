package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NovaNeonButton
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
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(400)
        )
        delay(800)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NovaPrimaryViolet, NovaElectricCyan)
                        )
                    )
                    .border(2.dp, NovaElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = NovaDarkBackground,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NOVA PLAYER",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NovaPrimaryText,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "BEYOND WATCHING",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaElectricCyan,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var page by remember { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlide(
            title = "Cinema Grade Playback",
            subtitle = "Experience crystal clear video and lossless audio powered by Google's native Media3 ExoPlayer engine with hardware acceleration.",
            icon = Icons.Default.Movie,
            color = NovaPrimaryViolet
        ),
        OnboardingSlide(
            title = "Scene Notebook & Smart Timeline",
            subtitle = "Save exact video timestamps, annotate lectures, bookmark crucial cinematic moments, and jump back anytime with precision.",
            icon = Icons.Default.EditNote,
            color = NovaElectricCyan
        ),
        OnboardingSlide(
            title = "Subtitle Studio with Full RTL",
            subtitle = "Engineered for international audiences with complete Right-to-Left script shaping for Dari, Pashto, and Arabic, plus sync offset controls.",
            icon = Icons.Default.Subtitles,
            color = NovaAccentPink
        )
    )

    val currentSlide = slides[page]

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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    color = NovaSecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onComplete() }
                )
            }

            // Slide Illustration
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NovaSurfaceElevated, NovaSurfaceCard)
                            )
                        )
                        .border(2.dp, currentSlide.color.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentSlide.icon,
                        contentDescription = null,
                        tint = currentSlide.color,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = currentSlide.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaPrimaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentSlide.subtitle,
                    fontSize = 14.sp,
                    color = NovaSecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Bottom Indicators & Next Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    slides.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .width(if (index == page) 24.dp else 8.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index == page) NovaElectricCyan else NovaSurfaceElevated
                                )
                        )
                    }
                }

                NovaNeonButton(
                    text = if (page == slides.size - 1) "Enter Nova Player" else "Continue",
                    icon = Icons.Default.ChevronRight,
                    onClick = {
                        if (page < slides.size - 1) {
                            page++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)
