package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhysioTealLight
import com.example.ui.theme.PhysioTealPrimary

@Composable
fun YouTubePlayerEmbed(
    youtubeUrl: String,
    exerciseTitle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionSec by remember { mutableFloatStateOf(14f) }
    val totalDurationSec = 145f // 2:25 min video

    // Extract video ID if possible
    val videoId = remember(youtubeUrl) {
        when {
            youtubeUrl.contains("v=") -> youtubeUrl.substringAfter("v=").substringBefore("&")
            youtubeUrl.contains("youtu.be/") -> youtubeUrl.substringAfter("youtu.be/").substringBefore("?")
            else -> "physio_guideline"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
    ) {
        // Video Preview Mock Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF0000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("YouTube HD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = exerciseTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl.ifEmpty { "https://www.youtube.com" }))
                        try {
                            context.startActivity(webIntent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Center Play/Pause button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(54.dp)
                        .background(if (isPlaying) PhysioTealPrimary else Color(0xCCFF0000), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Bottom Controls & Scrub Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(currentPositionSec / 60).toInt()}:${(currentPositionSec % 60).toInt().toString().padStart(2, '0')} / ${(totalDurationSec / 60).toInt()}:${(totalDurationSec % 60).toInt().toString().padStart(2, '0')}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "1080p 60fps",
                        color = PhysioTealLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Slider(
                    value = currentPositionSec,
                    onValueChange = { currentPositionSec = it },
                    valueRange = 0f..totalDurationSec,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF0000),
                        activeTrackColor = Color(0xFFFF0000),
                        inactiveTrackColor = Color(0x55FFFFFF)
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}
