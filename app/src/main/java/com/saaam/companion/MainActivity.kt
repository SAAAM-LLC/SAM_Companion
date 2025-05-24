package com.saaam.companion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saaam.companion.data.SAMDataManager
import com.saaam.companion.service.SAMBackgroundService
import com.saaam.companion.tutorial.TutorialScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var dataManager: SAMDataManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize data manager
        dataManager = SAMDataManager(this)
        
        setContent {
            SAMCompanionTheme {
                var showTutorial by remember { mutableStateOf(false) }
                
                // Check if this is first launch
                LaunchedEffect(Unit) {
                    dataManager.isFirstLaunch.collect { isFirstLaunch ->
                        showTutorial = isFirstLaunch
                    }
                }
                
                if (showTutorial) {
                    TutorialScreen(
                        onTutorialComplete = {
                            lifecycleScope.launch {
                                dataManager.markFirstLaunchComplete()
                                dataManager.markTutorialComplete()
                                showTutorial = false
                                
                                // Start background service after tutorial
                                SAMBackgroundService.startService(this@MainActivity)
                            }
                        }
                    )
                } else {
                    SAMCompanionApp()
                }
            }
        }
        
        // Handle intent actions (from shortcuts or notifications)
        handleIntentAction(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntentAction(it) }
    }
    
    private fun handleIntentAction(intent: Intent) {
        when (intent.action) {
            "com.saaam.companion.EVOLVE" -> {
                // Trigger evolution from shortcut
                SAMBackgroundService.triggerEvolution(this)
            }
            "com.saaam.companion.QUICK_CHAT" -> {
                // Quick chat action - focus on input
                // This would be handled by the ViewModel
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Record session metrics
        lifecycleScope.launch {
            // Calculate session duration, messages, etc.
            dataManager.recordUsageMetrics(
                sessionDuration = System.currentTimeMillis(), // Would calculate actual duration
                messagesExchanged = 0, // Would get from ViewModel
                evolutionEvents = 0,
                dreamCycles = 0
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SAMCompanionApp(viewModel: SAMViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
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
        // Top App Bar with SAM Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0a1128).copy(alpha = 0.9f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SAM Companion",
                            color = Color(0xFF64ffda),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.samStatus,
                            color = Color(0xFF80cbc4),
                            fontSize = 12.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Evolution indicator
                        Icon(
                            imageVector = if (uiState.isEvolving) Icons.Default.AutoAwesome else Icons.Default.Psychology,
                            contentDescription = "SAM Status",
                            tint = if (uiState.isEvolving) Color(0xFFffd54f) else Color(0xFF64ffda),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Menu button
                        IconButton(onClick = { viewModel.toggleMenu() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFF64ffda)
                            )
                        }
                    }
                }
                
                // SAM Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        label = "Concepts",
                        value = uiState.conceptCount.toString(),
                        icon = Icons.Default.Memory
                    )
                    StatCard(
                        label = "Growth",
                        value = uiState.growthLevel.toString(),
                        icon = Icons.Default.TrendingUp
                    )
                    StatCard(
                        label = "Dreams",
                        value = uiState.dreamCount.toString(),
                        icon = Icons.Default.Bedtime
                    )
                }
            }
        }

        // Chat Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.messages) { message ->
                MessageCard(message = message)
            }
            
            if (uiState.isThinking) {
                item {
                    ThinkingIndicator()
                }
            }
        }

        // Input Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0a1128).copy(alpha = 0.95f),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Quick Actions
                if (uiState.showQuickActions) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(uiState.quickActions) { action ->
                            QuickActionChip(
                                text = action,
                                onClick = { viewModel.sendQuickAction(action) }
                            )
                        }
                    }
                }
                
                // Text Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInputText,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Share your thoughts with SAM...",
                                color = Color(0xFF64ffda).copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64ffda),
                            unfocusedBorderColor = Color(0xFF64ffda).copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF64ffda)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.sendMessage()
                                keyboardController?.hide()
                            }
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            viewModel.sendMessage()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color(0xFF64ffda),
                        contentColor = Color(0xFF0a1128)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
    
    // Side Menu
    if (uiState.showMenu) {
        SAMMenu(
            onDismiss = { viewModel.toggleMenu() },
            onAction = viewModel::handleMenuAction,
            samStats = uiState.samStats
        )
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a2e).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF64ffda),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
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
fun MessageCard(message: ChatMessage) {
    val isUser = message.isUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF64ffda)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "SAM",
                    tint = Color(0xFF0a1128),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    Color(0xFF64ffda).copy(alpha = 0.9f)
                } else {
                    Color(0xFF1a1a2e).copy(alpha = 0.9f)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = if (isUser) Color(0xFF0a1128) else Color.White,
                    fontSize = 14.sp
                )
                
                if (message.metadata.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.metadata,
                        color = if (isUser) {
                            Color(0xFF0a1128).copy(alpha = 0.7f)
                        } else {
                            Color(0xFF80cbc4)
                        },
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF80cbc4)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "You",
                    tint = Color(0xFF0a1128),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF64ffda)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "SAM",
                tint = Color(0xFF0a1128),
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1a1a2e).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAM is thinking...",
                    color = Color(0xFF80cbc4),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF64ffda).copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF64ffda).copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color(0xFF64ffda),
                fontSize = 12.sp
            )
        }
    }
}
