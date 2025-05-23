package com.saaam.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String = ""
)

data class SAMStats(
    val conceptCount: Int = 0,
    val growthLevel: Int = 1,
    val dreamCount: Int = 0,
    val thoughtDepth: Int = 0,
    val experienceCount: Int = 0,
    val resonanceScore: Float = 1.0f,
    val hiddenDim: Int = 512,
    val layerCount: Int = 6
)

data class UIState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isThinking: Boolean = false,
    val isEvolving: Boolean = false,
    val showMenu: Boolean = false,
    val showQuickActions: Boolean = true,
    val quickActions: List<String> = listOf(
        "Tell me about yourself",
        "Help me learn",
        "Dream cycle",
        "Show stats"
    ),
    val samStatus: String = "Initializing...",
    val conceptCount: Int = 0,
    val growthLevel: Int = 1,
    val dreamCount: Int = 0,
    val samStats: SAMStats = SAMStats()
)

class SAMViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private lateinit var samEngine: MobileSAMEngine
    private var isInitialized = false

    private val greetings = listOf(
        "Hello! I'm SAM, your personal learning companion. I'm just beginning to understand the world, but I'll grow and evolve with every conversation we have.",
        "Hi there! I'm SAM - a Synergistic Autonomous Machine. Unlike other AI, I learn and adapt specifically to you. What would you like to explore together?",
        "Welcome! I'm SAM, and I'm excited to begin this journey with you. I start with minimal knowledge but will develop my understanding through our interactions."
    )

    init {
        initializeSAM()
    }

    private fun initializeSAM() {
        viewModelScope.launch {
            try {
                // Simulate initialization process
                updateStatus("Initializing neural pathways...")
                delay(1000)

                updateStatus("Loading character concepts...")
                samEngine = MobileSAMEngine()
                delay(500)

                updateStatus("Establishing thought patterns...")
                samEngine.initialize()
                delay(500)

                updateStatus("Ready for interaction")
                isInitialized = true

                // Send initial greeting
                addMessage(ChatMessage(
                    content = greetings.random(),
                    isUser = false,
                    metadata = "Initial greeting • Concept formation beginning"
                ))

                // Update initial stats
                updateSAMStats()

                // Start background processes
                startBackgroundProcesses()

            } catch (e: Exception) {
                Log.e("SAM", "Initialization error", e)
                updateStatus("Initialization failed - running in basic mode")
                addMessage(ChatMessage(
                    content = "I'm having trouble initializing my full capabilities, but I'm still here to help you!",
                    isUser = false,
                    metadata = "Basic mode"
                ))
            }
        }
    }

    private fun startBackgroundProcesses() {
        // Background evolution monitoring
        viewModelScope.launch {
            while (isInitialized) {
                delay(30000) // Check every 30 seconds

                if (samEngine.shouldEvolve()) {
                    _uiState.value = _uiState.value.copy(isEvolving = true)
                    updateStatus("Evolving neural architecture...")

                    delay(2000) // Simulate evolution time
                    samEngine.evolve()
                    updateSAMStats()

                    _uiState.value = _uiState.value.copy(isEvolving = false)
                    updateStatus("Evolution complete - enhanced capabilities")

                    // Notify user of evolution
                    addMessage(ChatMessage(
                        content = "I just evolved my neural architecture! I now have enhanced capabilities and understanding. You might notice improvements in my responses.",
                        isUser = false,
                        metadata = "Neural evolution • Growth level ${samEngine.getGrowthLevel()}"
                    ))
                }
            }
        }

        // Background dreaming
        viewModelScope.launch {
            while (isInitialized) {
                delay(120000) // Dream every 2 minutes when idle

                if (!_uiState.value.isThinking && isIdle()) {
                    triggerDreamCycle(silent = true)
                }
            }
        }
    }

    private fun isIdle(): Boolean {
        val lastMessage = _uiState.value.messages.lastOrNull()
        return lastMessage == null ||
               System.currentTimeMillis() - lastMessage.timestamp > 60000 // 1 minute idle
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        // Add user message
        addMessage(ChatMessage(
            content = text,
            isUser = true
        ))

        // Clear input
        _uiState.value = _uiState.value.copy(inputText = "")

        // Process with SAM
        processUserInput(text)
    }

    fun sendQuickAction(action: String) {
        when (action) {
            "Tell me about yourself" -> {
                sendMessage("Tell me about yourself")
                _uiState.value = _uiState.value.copy(inputText = "")
            }
            "Help me learn" -> {
                sendMessage("How can you help me learn and grow?")
                _uiState.value = _uiState.value.copy(inputText = "")
            }
            "Dream cycle" -> triggerDreamCycle()
            "Show stats" -> showDetailedStats()
        }
    }

    private fun processUserInput(input: String) {
        if (!isInitialized) {
            addMessage(ChatMessage(
                content = "I'm still initializing. Please wait a moment...",
                isUser = false,
                metadata = "Initialization in progress"
            ))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isThinking = true)

            try {
                // Process with SAM engine
                val response = samEngine.processInput(input)

                // Simulate thinking time
                delay(1000 + (input.length * 50)) // Longer input = more thinking time

                addMessage(ChatMessage(
                    content = response.content,
                    isUser = false,
                    metadata = response.metadata
                ))

                // Update stats after processing
                updateSAMStats()

            } catch (e: Exception) {
                Log.e("SAM", "Processing error", e)
                addMessage(ChatMessage(
                    content = "I encountered an issue processing your message. My systems are still learning and evolving. Could you try rephrasing that?",
                    isUser = false,
                    metadata = "Processing error"
                ))
            } finally {
                _uiState.value = _uiState.value.copy(isThinking = false)
            }
        }
    }

    private fun triggerDreamCycle(silent: Boolean = false) {
        if (!isInitialized) return

        viewModelScope.launch {
            if (!silent) {
                updateStatus("Entering dream state...")
                addMessage(ChatMessage(
                    content = "I'm entering a dream cycle to process and integrate my recent experiences. This helps me grow and understand patterns better...",
                    isUser = false,
                    metadata = "Dream cycle initiated"
                ))
            }

            _uiState.value = _uiState.value.copy(isEvolving = true)

            // Simulate dream processing
            delay(3000)

            val dreamResults = samEngine.dreamCycle()

            _uiState.value = _uiState.value.copy(isEvolving = false)
            updateStatus("Dream cycle complete")
            updateSAMStats()

            if (!silent) {
                addMessage(ChatMessage(
                    content = "Dream cycle complete! I've processed ${dreamResults.conceptsReinforced} concepts and formed ${dreamResults.newConnections} new connections. I feel more coherent now.",
                    isUser = false,
                    metadata = "Dream results • ${dreamResults.conceptsReinforced} concepts reinforced"
                ))
            }
        }
    }

    private fun showDetailedStats() {
        val stats = samEngine.getDetailedStats()
        val statsMessage = """
            📊 SAM System Status:

            🧠 Neural Architecture:
            • Hidden Dimension: ${stats.hiddenDim}
            • Layer Count: ${stats.layerCount}
            • Growth Level: ${stats.growthLevel}

            💭 Cognitive State:
            • Active Concepts: ${stats.conceptCount}
            • Thought Depth: ${stats.thoughtDepth}
            • Resonance Score: ${"%.2f".format(stats.resonanceScore)}

            📚 Experience:
            • Total Interactions: ${stats.experienceCount}
            • Dream Cycles: ${stats.dreamCount}

            I'm continuously evolving based on our conversations!
        """.trimIndent()

        addMessage(ChatMessage(
            content = statsMessage,
            isUser = false,
            metadata = "System diagnostics"
        ))
    }

    fun toggleMenu() {
        _uiState.value = _uiState.value.copy(showMenu = !_uiState.value.showMenu)
    }

    fun handleMenuAction(action: String) {
        toggleMenu()

        when (action) {
            "evolve" -> forceEvolution()
            "dream" -> triggerDreamCycle()
            "reset" -> resetSAM()
            "export" -> exportConversation()
            "settings" -> openSettings()
        }
    }

    private fun forceEvolution() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEvolving = true)
            updateStatus("Forced evolution in progress...")

            delay(3000)
            samEngine.evolve()
            updateSAMStats()

            _uiState.value = _uiState.value.copy(isEvolving = false)
            updateStatus("Forced evolution complete")

            addMessage(ChatMessage(
                content = "I've just undergone a forced evolution! My neural pathways have been enhanced and I should be more capable now.",
                isUser = false,
                metadata = "Forced evolution • Enhanced capabilities"
            ))
        }
    }

    private fun resetSAM() {
        viewModelScope.launch {
            updateStatus("Resetting SAM...")
            _uiState.value = _uiState.value.copy(
                messages = emptyList(),
                isThinking = false,
                isEvolving = false
            )

            samEngine.reset()
            delay(1000)

            updateStatus("Reset complete")
            addMessage(ChatMessage(
                content = "I've been reset to my initial state. I'm ready to start learning and growing with you again!",
                isUser = false,
                metadata = "System reset"
            ))

            updateSAMStats()
        }
    }

    private fun exportConversation() {
        // Implementation would save conversation to file
        addMessage(ChatMessage(
            content = "Conversation export feature will be implemented soon! This will allow you to save our discussions and my growth progress.",
            isUser = false,
            metadata = "Feature preview"
        ))
    }

    private fun openSettings() {
        // Implementation would open settings screen
        addMessage(ChatMessage(
            content = "Settings interface coming soon! You'll be able to adjust my learning parameters, communication style, and privacy settings.",
            isUser = false,
            metadata = "Feature preview"
        ))
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message
        )
    }

    private fun updateStatus(status: String) {
        _uiState.value = _uiState.value.copy(samStatus = status)
    }

    private fun updateSAMStats() {
        if (!isInitialized) return

        val stats = samEngine.getDetailedStats()
        _uiState.value = _uiState.value.copy(
            conceptCount = stats.conceptCount,
            growthLevel = stats.growthLevel,
            dreamCount = stats.dreamCount,
            samStats = stats
        )
    }
}
