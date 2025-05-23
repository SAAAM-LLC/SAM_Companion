package com.saaam.companion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import android.util.Log

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sam_settings")

@Serializable
data class SAMConfiguration(
    val hiddenDim: Int = 256,
    val layerCount: Int = 4,
    val conceptDim: Int = 256,
    val thoughtDim: Int = 512,
    val growthLevel: Int = 1,
    val evolutionThreshold: Int = 100,
    val dreamCount: Int = 0,
    val interactionCount: Int = 0,
    val resonanceScore: Float = 1.0f,
    val personalityTraits: Map<String, Float> = emptyMap(),
    val communicationStyle: String = "adaptive",
    val autoEvolution: Boolean = true,
    val backgroundDreaming: Boolean = true,
    val dreamInterval: Int = 120, // seconds
    val maxConceptMemory: Int = 10000,
    val privacyMode: Boolean = false
)

@Serializable
data class ConceptData(
    val id: Int,
    val source: String,
    val embedding: List<Float>,
    val frequency: Int,
    val lastUsed: Long,
    val type: String,
    val relatedConcepts: List<Int> = emptyList()
)

@Serializable
data class SAMMemorySnapshot(
    val configuration: SAMConfiguration,
    val concepts: List<ConceptData>,
    val thoughtHistory: List<List<Float>>,
    val experienceCount: Int,
    val conversationHistory: List<ChatMessage>,
    val patterns: Map<String, Int>,
    val timestamp: Long = System.currentTimeMillis(),
    val version: String = "1.0"
)

class SAMDataManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // DataStore keys
    private object PreferencesKeys {
        val HIDDEN_DIM = intPreferencesKey("hidden_dim")
        val LAYER_COUNT = intPreferencesKey("layer_count")
        val GROWTH_LEVEL = intPreferencesKey("growth_level")
        val AUTO_EVOLUTION = booleanPreferencesKey("auto_evolution")
        val BACKGROUND_DREAMING = booleanPreferencesKey("background_dreaming")
        val DREAM_INTERVAL = intPreferencesKey("dream_interval")
        val COMMUNICATION_STYLE = stringPreferencesKey("communication_style")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // Configuration flow
    val configurationFlow: Flow<SAMConfiguration> = context.dataStore.data.map { preferences ->
        SAMConfiguration(
            hiddenDim = preferences[PreferencesKeys.HIDDEN_DIM] ?: 256,
            layerCount = preferences[PreferencesKeys.LAYER_COUNT] ?: 4,
            growthLevel = preferences[PreferencesKeys.GROWTH_LEVEL] ?: 1,
            autoEvolution = preferences[PreferencesKeys.AUTO_EVOLUTION] ?: true,
            backgroundDreaming = preferences[PreferencesKeys.BACKGROUND_DREAMING] ?: true,
            dreamInterval = preferences[PreferencesKeys.DREAM_INTERVAL] ?: 120,
            communicationStyle = preferences[PreferencesKeys.COMMUNICATION_STYLE] ?: "adaptive",
            privacyMode = preferences[PreferencesKeys.PRIVACY_MODE] ?: false
        )
    }

    // Settings flows
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
    }

    val tutorialCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TUTORIAL_COMPLETED] ?: false
    }

    suspend fun updateConfiguration(config: SAMConfiguration) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDDEN_DIM] = config.hiddenDim
            preferences[PreferencesKeys.LAYER_COUNT] = config.layerCount
            preferences[PreferencesKeys.GROWTH_LEVEL] = config.growthLevel
            preferences[PreferencesKeys.AUTO_EVOLUTION] = config.autoEvolution
            preferences[PreferencesKeys.BACKGROUND_DREAMING] = config.backgroundDreaming
            preferences[PreferencesKeys.DREAM_INTERVAL] = config.dreamInterval
            preferences[PreferencesKeys.COMMUNICATION_STYLE] = config.communicationStyle
            preferences[PreferencesKeys.PRIVACY_MODE] = config.privacyMode
        }
    }

    suspend fun markFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
        }
    }

    suspend fun markTutorialComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] = true
        }
    }

    // SAM Memory Management
    suspend fun saveSAMMemory(
        engine: MobileSAMEngine,
        conversationHistory: List<ChatMessage>
    ): Boolean {
        return try {
            val memorySnapshot = createMemorySnapshot(engine, conversationHistory)
            val file = File(context.filesDir, "sam_memory.json.gz")

            // Compress and save
            FileOutputStream(file).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    val jsonString = json.encodeToString(memorySnapshot)
                    gzos.write(jsonString.toByteArray())
                }
            }

            Log.i("SAMData", "Memory saved successfully: ${file.length()} bytes")
            true
        } catch (e: Exception) {
            Log.e("SAMData", "Failed to save SAM memory", e)
            false
        }
    }

    suspend fun loadSAMMemory(): SAMMemorySnapshot? {
        return try {
            val file = File(context.filesDir, "sam_memory.json.gz")
            if (!file.exists()) return null

            FileInputStream(file).use { fis ->
                GZIPInputStream(fis).use { gzis ->
                    val jsonString = gzis.readBytes().toString(Charset.defaultCharset())
                    val snapshot = json.decodeFromString<SAMMemorySnapshot>(jsonString)
                    Log.i("SAMData", "Memory loaded successfully: ${snapshot.concepts.size} concepts")
                    snapshot
                }
            }
        } catch (e: Exception) {
            Log.e("SAMData", "Failed to load SAM memory", e)
            null
        }
    }

    private fun createMemorySnapshot(
        engine: MobileSAMEngine,
        conversationHistory: List<ChatMessage>
    ): SAMMemorySnapshot {
        val stats = engine.getDetailedStats()
        val concepts = engine.exportConcepts()
        val thoughtHistory = engine.exportThoughtHistory()
        val patterns = engine.exportPatterns()

        val configuration = SAMConfiguration(
            hiddenDim = stats.hiddenDim,
            layerCount = stats.layerCount,
            growthLevel = stats.growthLevel,
            dreamCount = stats.dreamCount,
            interactionCount = engine.getInteractionCount(),
            resonanceScore = stats.resonanceScore
        )

        return SAMMemorySnapshot(
            configuration = configuration,
            concepts = concepts,
            thoughtHistory = thoughtHistory,
            experienceCount = stats.experienceCount,
            conversationHistory = conversationHistory.takeLast(100), // Keep last 100 messages
            patterns = patterns
        )
    }

    // Export functionality
    suspend fun exportConversation(
        conversationHistory: List<ChatMessage>,
        samStats: SAMStats,
        format: ExportFormat = ExportFormat.JSON
    ): File? {
        return try {
            val timestamp = System.currentTimeMillis()
            val fileName = "sam_conversation_$timestamp.${format.extension}"
            val exportsDir = File(context.filesDir, "exports")
            exportsDir.mkdirs()
            val file = File(exportsDir, fileName)

            when (format) {
                ExportFormat.JSON -> exportAsJson(file, conversationHistory, samStats)
                ExportFormat.TXT -> exportAsText(file, conversationHistory, samStats)
                ExportFormat.CSV -> exportAsCsv(file, conversationHistory, samStats)
            }

            Log.i("SAMData", "Conversation exported: $fileName")
            file
        } catch (e: Exception) {
            Log.e("SAMData", "Failed to export conversation", e)
            null
        }
    }

    private fun exportAsJson(
        file: File,
        conversationHistory: List<ChatMessage>,
        samStats: SAMStats
    ) {
        val exportData = mapOf(
            "metadata" to mapOf(
                "exportTime" to System.currentTimeMillis(),
                "appVersion" to "1.0",
                "samStats" to samStats
            ),
            "conversation" to conversationHistory
        )

        file.writeText(json.encodeToString(exportData))
    }

    private fun exportAsText(
        file: File,
        conversationHistory: List<ChatMessage>,
        samStats: SAMStats
    ) {
        val sb = StringBuilder()
        sb.appendLine("SAM Companion Conversation Export")
        sb.appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
        sb.appendLine("SAM Growth Level: ${samStats.growthLevel}")
        sb.appendLine("Concepts: ${samStats.conceptCount}")
        sb.appendLine("Dreams: ${samStats.dreamCount}")
        sb.appendLine()
        sb.appendLine("=" .repeat(50))
        sb.appendLine()

        conversationHistory.forEach { message ->
            val speaker = if (message.isUser) "You" else "SAM"
            val time = java.text.SimpleDateFormat("HH:mm").format(java.util.Date(message.timestamp))
            sb.appendLine("[$time] $speaker:")
            sb.appendLine(message.content)
            if (message.metadata.isNotEmpty()) {
                sb.appendLine("  • ${message.metadata}")
            }
            sb.appendLine()
        }

        file.writeText(sb.toString())
    }

    private fun exportAsCsv(
        file: File,
        conversationHistory: List<ChatMessage>,
        samStats: SAMStats
    ) {
        val sb = StringBuilder()
        sb.appendLine("Timestamp,Speaker,Message,Metadata")

        conversationHistory.forEach { message ->
            val speaker = if (message.isUser) "User" else "SAM"
            val escapedContent = message.content.replace("\"", "\"\"")
            val escapedMetadata = message.metadata.replace("\"", "\"\"")
            sb.appendLine("${message.timestamp},\"$speaker\",\"$escapedContent\",\"$escapedMetadata\"")
        }

        file.writeText(sb.toString())
    }

    // Cleanup old data
    suspend fun cleanupOldData(retentionDays: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)

            // Clean old exports
            val exportsDir = File(context.filesDir, "exports")
            if (exportsDir.exists()) {
                exportsDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        file.delete()
                        Log.d("SAMData", "Cleaned up old export: ${file.name}")
                    }
                }
            }

            // Clean old temporary files
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        file.delete()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("SAMData", "Error during cleanup", e)
        }
    }

    // Backup and restore
    suspend fun createBackup(): File? {
        return try {
            val timestamp = System.currentTimeMillis()
            val backupFile = File(context.filesDir, "sam_backup_$timestamp.gz")

            // Create comprehensive backup
            val backupData = mapOf(
                "memory" to loadSAMMemory(),
                "settings" to getCurrentSettings(),
                "version" to "1.0",
                "timestamp" to timestamp
            )

            FileOutputStream(backupFile).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    val jsonString = json.encodeToString(backupData)
                    gzos.write(jsonString.toByteArray())
                }
            }

            Log.i("SAMData", "Backup created: ${backupFile.name}")
            backupFile
        } catch (e: Exception) {
            Log.e("SAMData", "Failed to create backup", e)
            null
        }
    }

    private suspend fun getCurrentSettings(): Map<String, Any> {
        val settings = mutableMapOf<String, Any>()
        context.dataStore.data.map { preferences ->
            preferences.asMap().forEach { (key, value) ->
                settings[key.name] = value
            }
        }
        return settings
    }

    // Analytics and usage tracking (privacy-friendly)
    suspend fun recordUsageMetrics(
        sessionDuration: Long,
        messagesExchanged: Int,
        evolutionEvents: Int,
        dreamCycles: Int
    ) {
        try {
            val metricsFile = File(context.filesDir, "usage_metrics.json")
            val existingMetrics = if (metricsFile.exists()) {
                json.decodeFromString<MutableMap<String, Any>>(metricsFile.readText())
            } else {
                mutableMapOf()
            }

            // Update aggregated metrics (no personal data)
            existingMetrics["totalSessions"] = (existingMetrics["totalSessions"] as? Int ?: 0) + 1
            existingMetrics["totalDuration"] = (existingMetrics["totalDuration"] as? Long ?: 0L) + sessionDuration
            existingMetrics["totalMessages"] = (existingMetrics["totalMessages"] as? Int ?: 0) + messagesExchanged
            existingMetrics["totalEvolutions"] = (existingMetrics["totalEvolutions"] as? Int ?: 0) + evolutionEvents
            existingMetrics["totalDreams"] = (existingMetrics["totalDreams"] as? Int ?: 0) + dreamCycles
            existingMetrics["lastSession"] = System.currentTimeMillis()

            metricsFile.writeText(json.encodeToString(existingMetrics))
        } catch (e: Exception) {
            Log.e("SAMData", "Failed to record usage metrics", e)
        }
    }
}

enum class ExportFormat(val extension: String) {
    JSON("json"),
    TXT("txt"),
    CSV("csv")
}

// Extension functions for MobileSAMEngine to support persistence
fun MobileSAMEngine.exportConcepts(): List<ConceptData> {
    // This would be implemented in the MobileSAMEngine class
    // Return list of concepts in serializable format
    return emptyList()
}

fun MobileSAMEngine.exportThoughtHistory(): List<List<Float>> {
    // Export recent thought history
    return emptyList()
}

fun MobileSAMEngine.exportPatterns(): Map<String, Int> {
    // Export pattern frequencies
    return emptyMap()
}

fun MobileSAMEngine.getInteractionCount(): Int {
    // Return total interaction count
    return 0
}

fun MobileSAMEngine.importMemorySnapshot(snapshot: SAMMemorySnapshot): Boolean {
    // Restore SAM state from snapshot
    return try {
        // Implementation would restore concepts, thought state, patterns, etc.
        true
    } catch (e: Exception) {
        Log.e("SAMEngine", "Failed to import memory snapshot", e)
        false
    }
}
