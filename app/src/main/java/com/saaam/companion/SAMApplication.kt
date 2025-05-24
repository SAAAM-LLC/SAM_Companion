package com.saaam.companion

import android.app.Application
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

// Application class
class SAMApplication : Application() {
    
    companion object {
        lateinit var instance: SAMApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i("SAMApplication", "SAM Companion application started")
    }
}

// Composable functions for UI components
@Composable
fun SAMMenu(
    onDismiss: () -> Unit,
    onAction: (String) -> Unit,
    samStats: SAMStats
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0a1128).copy(alpha = 0.95f),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAM Control Center",
                        color = Color(0xFF64ffda),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64ffda)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // System Status Section
                SAMStatusSection(samStats)

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuActionButton(
                        icon = Icons.Default.AutoAwesome,
                        title = "Force Evolution",
                        description = "Trigger neural architecture growth",
                        onClick = { onAction("evolve") }
                    )

                    MenuActionButton(
                        icon = Icons.Default.Bedtime,
                        title = "Dream Cycle",
                        description = "Process concepts and form connections",
                        onClick = { onAction("dream") }
                    )

                    MenuActionButton(
                        icon = Icons.Default.Refresh,
                        title = "Reset SAM",
                        description = "Return to initial state",
                        onClick = { onAction("reset") },
                        isDestructive = true
                    )

                    MenuActionButton(
                        icon = Icons.Default.FileDownload,
                        title = "Export Conversation",
                        description = "Save chat history and growth data",
                        onClick = { onAction("export") }
                    )

                    MenuActionButton(
                        icon = Icons.Default.Settings,
                        title = "Advanced Settings",
                        description = "Configure learning parameters",
                        onClick = { onAction("settings") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Text(
                    text = "SAM v1.0 • Neural architecture evolving since activation",
                    color = Color(0xFF80cbc4).copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SAMStatusSection(stats: SAMStats) {
    Column {
        Text(
            text = "Neural Architecture Status",
            color = Color(0xFF64ffda),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusMetric(
                label = "Concepts",
                value = stats.conceptCount.toString(),
                icon = Icons.Default.Memory,
                color = Color(0xFF64ffda)
            )

            StatusMetric(
                label = "Growth Lv",
                value = stats.growthLevel.toString(),
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF81c784)
            )

            StatusMetric(
                label = "Dreams",
                value = stats.dreamCount.toString(),
                icon = Icons.Default.Bedtime,
                color = Color(0xFFba68c8)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Neural Architecture Visualization
        NeuralArchitectureVisualization(stats)

        Spacer(modifier = Modifier.height(12.dp))

        // Resonance Score
        ResonanceIndicator(stats.resonanceScore)
    }
}

@Composable
fun StatusMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a2e).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color(0xFF80cbc4),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun NeuralArchitectureVisualization(stats: SAMStats) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a2e).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Neural Architecture",
                color = Color(0xFF64ffda),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hidden Dim",
                        color = Color(0xFF80cbc4),
                        fontSize = 12.sp
                    )
                    Text(
                        text = stats.hiddenDim.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Layers",
                        color = Color(0xFF80cbc4),
                        fontSize = 12.sp
                    )
                    Text(
                        text = stats.layerCount.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Thought Depth",
                        color = Color(0xFF80cbc4),
                        fontSize = 12.sp
                    )
                    Text(
                        text = stats.thoughtDepth.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ResonanceIndicator(resonanceScore: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conceptual Resonance",
                color = Color(0xFF80cbc4),
                fontSize = 12.sp
            )
            Text(
                text = "${(resonanceScore * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { resonanceScore },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = when {
                resonanceScore > 0.8f -> Color(0xFF81c784)
                resonanceScore > 0.6f -> Color(0xFFffd54f)
                else -> Color(0xFFf06292)
            },
            trackColor = Color(0xFF1a1a2e)
        )
    }
}

@Composable
fun MenuActionButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDestructive) {
            Color(0xFFf06292).copy(alpha = 0.1f)
        } else {
            Color(0xFF64ffda).copy(alpha = 0.1f)
        },
        border = BorderStroke(
            1.dp,
            if (isDestructive) {
                Color(0xFFf06292).copy(alpha = 0.3f)
            } else {
                Color(0xFF64ffda).copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) Color(0xFFf06292) else Color(0xFF64ffda),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = Color(0xFF80cbc4),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Execute",
                tint = Color(0xFF80cbc4),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Theme Definition
@Composable
fun SAMCompanionTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Color(0xFF64ffda),
        secondary = Color(0xFF80cbc4),
        background = Color(0xFF0a1128),
        surface = Color(0xFF1a1a2e),
        onPrimary = Color(0xFF0a1128),
        onSecondary = Color(0xFF0a1128),
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Additional UI Components
@Composable
fun AnimatedThinkingDots() {
    Row {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "thinking_dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        Color(0xFF64ffda).copy(alpha = alpha),
                        RoundedCornerShape(4.dp)
                    )
            )
            if (index < 2) Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun NeuralActivityIndicator(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_activity")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                brush = Brush.sweepGradient(
                    colors = if (isActive) {
                        listOf(
                            Color(0xFF64ffda),
                            Color(0xFF80cbc4),
                            Color(0xFFba68c8),
                            Color(0xFF64ffda)
                        )
                    } else {
                        listOf(Color(0xFF64ffda).copy(alpha = 0.3f))
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (isActive) {
                    Modifier.graphicsLayer { rotationZ = rotation }
                } else {
                    Modifier
                }
            )
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "Neural Activity",
            tint = Color(0xFF0a1128),
            modifier = Modifier
                .align(Alignment.Center)
                .size(16.dp)
        )
    }
}
