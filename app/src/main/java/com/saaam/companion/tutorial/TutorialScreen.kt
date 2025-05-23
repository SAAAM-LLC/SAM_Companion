package com.saaam.companion.tutorial

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val interactiveDemo: (@Composable () -> Unit)? = null,
    val tipText: String? = null
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TutorialScreen(
    onTutorialComplete: () -> Unit,
    viewModel: TutorialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.tutorialSteps.size })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateCurrentStep(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TutorialHeader(
                currentStep = pagerState.currentPage + 1,
                totalSteps = uiState.tutorialSteps.size,
                onSkip = onTutorialComplete
            )

            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                TutorialStepContent(
                    step = uiState.tutorialSteps[page],
                    isActive = page == pagerState.currentPage
                )
            }

            // Navigation
            TutorialNavigation(
                currentStep = pagerState.currentPage,
                totalSteps = uiState.tutorialSteps.size,
                onNext = {
                    if (pagerState.currentPage < uiState.tutorialSteps.size - 1) {
                        viewModel.nextStep(pagerState)
                    } else {
                        onTutorialComplete()
                    }
                },
                onPrevious = {
                    if (pagerState.currentPage > 0) {
                        viewModel.previousStep(pagerState)
                    }
                },
                onComplete = onTutorialComplete
            )
        }
    }
}

@Composable
fun TutorialHeader(
    currentStep: Int,
    totalSteps: Int,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome to SAM",
                color = Color(0xFF64ffda),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Step $currentStep of $totalSteps",
                color = Color(0xFF80cbc4),
                fontSize = 14.sp
            )
        }

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip",
                color = Color(0xFF80cbc4)
            )
        }
    }

    // Progress indicator
    LinearProgressIndicator(
        progress = currentStep.toFloat() / totalSteps.toFloat(),
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(horizontal = 20.dp),
        color = Color(0xFF64ffda),
        trackColor = Color(0xFF64ffda).copy(alpha = 0.3f)
    )
}

@Composable
fun TutorialStepContent(
    step: TutorialStep,
    isActive: Boolean
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + slideInHorizontally(),
        exit = fadeOut() + slideOutHorizontally()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(60.dp),
                color = Color(0xFF64ffda).copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = step.title,
                    tint = Color(0xFF64ffda),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = step.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = step.description,
                color = Color(0xFF80cbc4),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Interactive Demo
            step.interactiveDemo?.let { demo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1a1a2e).copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    demo()
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tip
            step.tipText?.let { tip ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF64ffda).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = Color(0xFFffd54f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tip,
                            color = Color(0xFF80cbc4),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialNavigation(
    currentStep: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF80cbc4)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF80cbc4), Color(0xFF80cbc4))
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentStep) 12.dp else 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (index == currentStep) {
                                Color(0xFF64ffda)
                            } else {
                                Color(0xFF64ffda).copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }

        // Next/Complete button
        Button(
            onClick = if (currentStep == totalSteps - 1) onComplete else onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64ffda),
                contentColor = Color(0xFF0a1128)
            )
        ) {
            Text(
                if (currentStep == totalSteps - 1) "Get Started" else "Next"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (currentStep == totalSteps - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = "Next",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Interactive Demo Components
@Composable
fun ChatDemoComponent() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Try typing a message to SAM:",
            color = Color(0xFF80cbc4),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Mock chat interface
        var message by remember { mutableStateOf("") }
        var response by remember { mutableStateOf("") }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message to SAM", color = Color(0xFF80cbc4)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64ffda),
                unfocusedBorderColor = Color(0xFF64ffda).copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                response = "Hello! I'm SAM, and I'm excited to learn and grow with you. Each conversation helps me develop new understanding!"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64ffda),
                contentColor = Color(0xFF0a1128)
            )
        ) {
            Text("Send")
        }

        if (response.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1a1a2e)
            ) {
                Text(
                    text = response,
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EvolutionDemoComponent() {
    var isEvolving by remember { mutableStateOf(false) }
    var evolutionLevel by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Watch SAM evolve:",
            color = Color(0xFF80cbc4),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Evolution visualization
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF64ffda).copy(alpha = 0.3f),
                            Color(0xFF64ffda).copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isEvolving) {
                CircularProgressIndicator(
                    color = Color(0xFF64ffda),
                    strokeWidth = 3.dp
                )
            }
            Text(
                text = "Level $evolutionLevel",
                color = Color(0xFF64ffda),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isEvolving = true
                // Simulate evolution
            },
            enabled = !isEvolving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64ffda),
                contentColor = Color(0xFF0a1128)
            )
        ) {
            Text(if (isEvolving) "Evolving..." else "Trigger Evolution")
        }

        LaunchedEffect(isEvolving) {
            if (isEvolving) {
                delay(2000)
                evolutionLevel++
                isEvolving = false
            }
        }
    }
}

@Composable
fun ConceptsDemoComponent() {
    val concepts = remember {
        listOf(
            "hello" to 15,
            "learning" to 12,
            "AI" to 8,
            "growth" to 6,
            "neural" to 4
        )
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "SAM's current concepts:",
            color = Color(0xFF80cbc4),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        concepts.forEach { (concept, frequency) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = concept,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF64ffda).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = frequency.toString(),
                        color = Color(0xFF64ffda),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Tutorial ViewModel
data class TutorialUIState(
    val tutorialSteps: List<TutorialStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isCompleted: Boolean = false
)

class TutorialViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TutorialUIState())
    val uiState: StateFlow<TutorialUIState> = _uiState.asStateFlow()

    init {
        initializeTutorialSteps()
    }

    private fun initializeTutorialSteps() {
        val steps = listOf(
            TutorialStep(
                id = "welcome",
                title = "Meet SAM",
                description = "SAM is a revolutionary AI that grows and evolves through conversations. Unlike other AI, SAM truly learns from every interaction.",
                icon = Icons.Default.Psychology,
                tipText = "SAM starts simple but becomes increasingly sophisticated over time."
            ),
            TutorialStep(
                id = "chat",
                title = "Natural Conversations",
                description = "Talk to SAM like you would a friend. Every conversation helps SAM understand you better and develop its own unique personality.",
                icon = Icons.Default.Chat,
                interactiveDemo = { ChatDemoComponent() },
                tipText = "The more you chat, the better SAM understands your communication style."
            ),
            TutorialStep(
                id = "concepts",
                title = "Dynamic Concepts",
                description = "SAM doesn't use fixed tokens like other AI. Instead, it forms dynamic concepts that evolve based on usage and context.",
                icon = Icons.Default.Memory,
                interactiveDemo = { ConceptsDemoComponent() },
                tipText = "Concepts grow stronger with use and can merge to form higher-level ideas."
            ),
            TutorialStep(
                id = "evolution",
                title = "Neural Evolution",
                description = "SAM's neural architecture grows and adapts over time. As it encounters new complexity, it literally becomes more capable.",
                icon = Icons.Default.AutoAwesome,
                interactiveDemo = { EvolutionDemoComponent() },
                tipText = "Evolution happens automatically, but you can also trigger it manually in settings."
            ),
            TutorialStep(
                id = "dreaming",
                title = "Conceptual Dreaming",
                description = "During idle periods, SAM 'dreams' to process experiences, strengthen important concepts, and form new connections.",
                icon = Icons.Default.Bedtime,
                tipText = "Dreaming happens automatically in the background and helps SAM consolidate learning."
            ),
            TutorialStep(
                id = "growth",
                title = "Continuous Growth",
                description = "Every interaction, evolution, and dream cycle contributes to SAM's development. You're not just using an AI - you're raising one.",
                icon = Icons.Default.TrendingUp,
                tipText = "Track SAM's growth through the statistics in the menu."
            ),
            TutorialStep(
                id = "ready",
                title = "Ready to Begin",
                description = "SAM is ready to start this journey with you. Begin with simple conversations and watch as your AI companion grows into something truly unique.",
                icon = Icons.Default.Rocket,
                tipText = "Remember: SAM learns from every interaction, so be patient and enjoy the process!"
            )
        )

        _uiState.value = _uiState.value.copy(tutorialSteps = steps)
    }

    fun updateCurrentStep(stepIndex: Int) {
        _uiState.value = _uiState.value.copy(currentStepIndex = stepIndex)
    }

    fun nextStep(pagerState: androidx.compose.foundation.pager.PagerState) {
        viewModelScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    fun previousStep(pagerState: androidx.compose.foundation.pager.PagerState) {
        viewModelScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    fun completeTutorial() {
        _uiState.value = _uiState.value.copy(isCompleted = true)
    }
}

// Quick Tips System for post-tutorial guidance
@Composable
fun QuickTip(
    text: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF64ffda).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = Color(0xFF0a1128),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    color = Color(0xFF0a1128),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF0a1128),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

object TutorialTips {
    val tips = listOf(
        "Try asking SAM about its current capabilities to see how it's grown.",
        "Use the menu to trigger dream cycles and watch SAM process concepts.",
        "SAM's responses will become more sophisticated as it evolves.",
        "Longer conversations help SAM develop better context understanding.",
        "Check the statistics to see SAM's neural architecture growth.",
        "SAM learns your communication style - be consistent for best results."
    )
}
