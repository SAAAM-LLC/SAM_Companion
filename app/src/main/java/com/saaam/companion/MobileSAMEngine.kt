package com.saaam.companion

import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.*
import kotlin.random.Random

data class SAMResponse(
    val content: String,
    val metadata: String,
    val conceptsUsed: Int = 0,
    val thoughtDepth: Int = 0
)

data class DreamResults(
    val conceptsReinforced: Int,
    val newConnections: Int,
    val prunedConcepts: Int
)

data class Concept(
    val id: Int,
    val source: String,
    val embedding: FloatArray,
    val frequency: Int = 0,
    val lastUsed: Long = System.currentTimeMillis(),
    val type: ConceptType = ConceptType.CHARACTER_SEQUENCE,
    val relatedConcepts: MutableSet<Int> = mutableSetOf()
)

enum class ConceptType {
    CHARACTER_SEQUENCE,
    SEMANTIC,
    MERGED,
    PATTERN
}

class MobileSAMEngine {

    // Core SAM Components (Mobile-Optimized)
    private val conceptBank = mutableMapOf<Int, Concept>()
    private val sourceToConceptId = mutableMapOf<String, Int>()
    private val thoughtState = ThoughtState()
    private val patternMemory = PatternMemory()
    private val experienceManager = ExperienceManager()

    // Neural Architecture Parameters (Mobile-Scale)
    private var hiddenDim = 256  // Start smaller for mobile
    private var layerCount = 4   // Fewer layers for efficiency
    private var conceptDim = 256
    private var thoughtDim = 512

    // Growth and Evolution Tracking
    private var growthLevel = 1
    private var evolutionThreshold = 100  // Interactions before considering evolution
    private var interactionCount = 0
    private var dreamCount = 0

    // Statistics
    private var conceptCount = 0
    private var experienceCount = 0
    private var resonanceScore = 1.0f

    // Response Generation Components
    private val responseTemplates = ResponseTemplates()
    private val conversationContext = mutableListOf<String>()

    fun initialize() {
        initializeBasicConcepts()
        initializeResponseSystem()
        Log.i("MobileSAM", "SAM Engine initialized with $conceptCount concepts")
    }

    private fun initializeBasicConcepts() {
        // Initialize with essential character-level concepts
        val basicChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .,!?-'"
        basicChars.forEach { char ->
            addCharacterConcept(char.toString())
        }

        // Initialize common word concepts
        val commonWords = listOf(
            "I", "you", "me", "we", "they", "it", "is", "am", "are", "was", "were",
            "the", "and", "or", "but", "if", "then", "this", "that", "these", "those",
            "hello", "hi", "how", "what", "when", "where", "why", "who", "can", "will",
            "help", "learn", "understand", "think", "know", "feel", "like", "want",
            "SAM", "AI", "machine", "neural", "concept", "thought", "dream", "evolution"
        )

        commonWords.forEach { word ->
            addCharacterConcept(word)
        }

        Log.i("MobileSAM", "Initialized ${conceptBank.size} basic concepts")
    }

    private fun initializeResponseSystem() {
        responseTemplates.initialize()
    }

    fun processInput(input: String): SAMResponse {
        interactionCount++

        // Record experience
        experienceManager.addExperience(input, "user_input")

        // Update conversation context
        conversationContext.add(input)
        if (conversationContext.size > 10) {
            conversationContext.removeAt(0) // Keep last 10 exchanges
        }

        // Process input into concepts
        val concepts = processTextToConcepts(input)

        // Update thought state
        thoughtState.update(concepts, input)

        // Generate response
        val response = generateResponse(input, concepts)

        // Update patterns
        patternMemory.addPattern(input)

        // Update concept usage
        concepts.forEach { conceptId ->
            conceptBank[conceptId]?.let { concept ->
                conceptBank[conceptId] = concept.copy(
                    frequency = concept.frequency + 1,
                    lastUsed = System.currentTimeMillis()
                )
            }
        }

        // Record response experience
        experienceManager.addExperience(response.content, "sam_response")
        experienceCount++

        return response
    }

    private fun processTextToConcepts(text: String): List<Int> {
        val concepts = mutableListOf<Int>()
        val words = text.lowercase().split(Regex("\\s+"))

        for (word in words) {
            // Clean word
            val cleanWord = word.replace(Regex("[^a-zA-Z0-9]"), "")
            if (cleanWord.isEmpty()) continue

            // Find or create concept
            val conceptId = sourceToConceptId[cleanWord] ?: run {
                addCharacterConcept(cleanWord)
            }
            concepts.add(conceptId)

            // Check for character-level concepts for unknown words
            if (!sourceToConceptId.containsKey(cleanWord)) {
                cleanWord.forEach { char ->
                    val charConceptId = sourceToConceptId[char.toString()]
                    if (charConceptId != null) {
                        concepts.add(charConceptId)
                    }
                }
            }
        }

        // Look for patterns
        val patterns = patternMemory.findPatterns(text)
        patterns.forEach { pattern ->
            val patternConceptId = sourceToConceptId[pattern] ?: addPatternConcept(pattern)
            concepts.add(patternConceptId)
        }

        return concepts
    }

    private fun addCharacterConcept(source: String): Int {
        val conceptId = conceptCount++
        val embedding = generateEmbedding(source)

        val concept = Concept(
            id = conceptId,
            source = source,
            embedding = embedding,
            type = ConceptType.CHARACTER_SEQUENCE
        )

        conceptBank[conceptId] = concept
        sourceToConceptId[source] = conceptId

        return conceptId
    }

    private fun addPatternConcept(pattern: String): Int {
        val conceptId = conceptCount++
        val embedding = generateEmbedding(pattern, isPattern = true)

        val concept = Concept(
            id = conceptId,
            source = pattern,
            embedding = embedding,
            type = ConceptType.PATTERN
        )

        conceptBank[conceptId] = concept
        sourceToConceptId[pattern] = conceptId

        return conceptId
    }

    private fun generateEmbedding(text: String, isPattern: Boolean = false): FloatArray {
        // Simple embedding generation based on character statistics
        val embedding = FloatArray(conceptDim) { 0f }

        // Character-based features
        text.forEachIndexed { index, char ->
            val charValue = char.code.toFloat() / 128f // Normalize
            val position = (index * 4) % conceptDim

            if (position + 3 < conceptDim) {
                embedding[position] = sin(charValue).toFloat()
                embedding[position + 1] = cos(charValue).toFloat()
                embedding[position + 2] = sin(2 * charValue).toFloat()
                embedding[position + 3] = cos(2 * charValue).toFloat()
            }
        }

        // Add pattern-specific features
        if (isPattern) {
            val patternBoost = 0.5f
            for (i in embedding.indices step 2) {
                embedding[i] *= (1f + patternBoost)
            }
        }

        // Normalize
        val norm = sqrt(embedding.sumOf { it * it }.toFloat())
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }

        return embedding
    }

    private fun generateResponse(input: String, concepts: List<Int>): SAMResponse {
        val thoughtDepth = thoughtState.getDepth()
        val contextualResponse = responseTemplates.generateContextualResponse(
            input = input,
            concepts = concepts,
            conceptBank = conceptBank,
            conversationContext = conversationContext,
            thoughtDepth = thoughtDepth,
            growthLevel = growthLevel
        )

        val metadata = buildString {
            append("${concepts.size} concepts")
            append(" • Thought depth: $thoughtDepth")
            if (thoughtDepth > 3) append(" • Deep reasoning")
            if (concepts.size > 10) append(" • Complex processing")
        }

        return SAMResponse(
            content = contextualResponse,
            metadata = metadata,
            conceptsUsed = concepts.size,
            thoughtDepth = thoughtDepth
        )
    }

    fun shouldEvolve(): Boolean {
        return interactionCount >= evolutionThreshold &&
               (conceptCount > hiddenDim * 0.8 || thoughtState.complexity() > layerCount * 0.9)
    }

    fun evolve() {
        growthLevel++

        // Expand architecture
        val oldHiddenDim = hiddenDim
        val oldLayerCount = layerCount

        hiddenDim = min(512, (hiddenDim * 1.2).toInt()) // Cap at 512 for mobile
        layerCount = min(8, layerCount + 1) // Cap at 8 layers
        conceptDim = hiddenDim
        thoughtDim = hiddenDim * 2

        // Update evolution threshold
        evolutionThreshold = (evolutionThreshold * 1.5).toInt()

        // Merge related concepts
        mergeRelatedConcepts()

        // Update thought state capacity
        thoughtState.expand(thoughtDim)

        Log.i("MobileSAM", "Evolution complete: $oldHiddenDim->$hiddenDim dim, $oldLayerCount->$layerCount layers")
    }

    private fun mergeRelatedConcepts() {
        val conceptPairs = mutableListOf<Pair<Int, Int>>()

        // Find concepts to merge based on usage patterns
        conceptBank.values.filter { it.frequency > 5 }.forEach { concept1 ->
            conceptBank.values.filter {
                it.id != concept1.id && it.frequency > 5
            }.forEach { concept2 ->
                if (shouldMerge(concept1, concept2)) {
                    conceptPairs.add(Pair(concept1.id, concept2.id))
                }
            }
        }

        // Merge top pairs
        conceptPairs.take(5).forEach { (id1, id2) ->
            mergeConceptPair(id1, id2)
        }
    }

    private fun shouldMerge(concept1: Concept, concept2: Concept): Boolean {
        // Simple similarity check based on source text
        val similarity = calculateTextSimilarity(concept1.source, concept2.source)
        return similarity > 0.7 && concept1.type == concept2.type
    }

    private fun calculateTextSimilarity(text1: String, text2: String): Double {
        val set1 = text1.toSet()
        val set2 = text2.toSet()
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    private fun mergeConceptPair(id1: Int, id2: Int) {
        val concept1 = conceptBank[id1] ?: return
        val concept2 = conceptBank[id2] ?: return

        val mergedId = conceptCount++
        val mergedSource = "${concept1.source}+${concept2.source}"

        // Create merged embedding (simple average)
        val mergedEmbedding = FloatArray(conceptDim) { i ->
            (concept1.embedding[i] + concept2.embedding[i]) / 2f
        }

        val mergedConcept = Concept(
            id = mergedId,
            source = mergedSource,
            embedding = mergedEmbedding,
            frequency = concept1.frequency + concept2.frequency,
            type = ConceptType.MERGED,
            relatedConcepts = (concept1.relatedConcepts + concept2.relatedConcepts).toMutableSet()
        )

        conceptBank[mergedId] = mergedConcept
        sourceToConceptId[mergedSource] = mergedId

        // Update related concepts
        mergedConcept.relatedConcepts.add(id1)
        mergedConcept.relatedConcepts.add(id2)
    }

    fun dreamCycle(): DreamResults {
        dreamCount++

        var conceptsReinforced = 0
        var newConnections = 0
        var prunedConcepts = 0

        // Reinforce important concepts
        val importantConcepts = conceptBank.values.sortedByDescending { it.frequency }.take(20)
        importantConcepts.forEach { concept ->
            conceptBank[concept.id] = concept.copy(frequency = concept.frequency + 1)
            conceptsReinforced++
        }

        // Create new connections between related concepts
        importantConcepts.take(10).forEach { concept1 ->
            importantConcepts.take(10).forEach { concept2 ->
                if (concept1.id != concept2.id &&
                    !concept1.relatedConcepts.contains(concept2.id) &&
                    shouldConnect(concept1, concept2)) {

                    concept1.relatedConcepts.add(concept2.id)
                    concept2.relatedConcepts.add(concept1.id)
                    newConnections++
                }
            }
        }

        // Prune rarely used concepts
        val rareConcepts = conceptBank.values.filter {
            it.frequency < 2 &&
            System.currentTimeMillis() - it.lastUsed > 300000 // 5 minutes
        }

        rareConcepts.take(5).forEach { concept ->
            conceptBank.remove(concept.id)
            sourceToConceptId.remove(concept.source)
            prunedConcepts++
        }

        // Update thought state coherence
        thoughtState.dream()

        // Update resonance score
        updateResonanceScore()

        Log.i("MobileSAM", "Dream cycle: reinforced $conceptsReinforced, connected $newConnections, pruned $prunedConcepts")

        return DreamResults(conceptsReinforced, newConnections, prunedConcepts)
    }

    private fun shouldConnect(concept1: Concept, concept2: Concept): Boolean {
        // Simple co-occurrence check
        val similarity = calculateTextSimilarity(concept1.source, concept2.source)
        return similarity > 0.3 ||
               (concept1.frequency > 5 && concept2.frequency > 5 && Random.nextFloat() < 0.1)
    }

    private fun updateResonanceScore() {
        // Calculate resonance based on concept connectivity and usage patterns
        val totalConnections = conceptBank.values.sumOf { it.relatedConcepts.size }
        val avgConnections = if (conceptBank.isNotEmpty()) {
            totalConnections.toFloat() / conceptBank.size
        } else 0f

        val usageVariance = calculateUsageVariance()

        resonanceScore = (avgConnections * 0.6f + (1f / (1f + usageVariance)) * 0.4f).coerceIn(0f, 1f)
    }

    private fun calculateUsageVariance(): Float {
        if (conceptBank.isEmpty()) return 0f

        val frequencies = conceptBank.values.map { it.frequency.toFloat() }
        val mean = frequencies.average().toFloat()
        val variance = frequencies.map { (it - mean).pow(2) }.average().toFloat()

        return sqrt(variance)
    }

    fun getGrowthLevel(): Int = growthLevel

    fun getDetailedStats(): SAMStats {
        return SAMStats(
            conceptCount = conceptBank.size,
            growthLevel = growthLevel,
            dreamCount = dreamCount,
            thoughtDepth = thoughtState.getDepth(),
            experienceCount = experienceCount,
            resonanceScore = resonanceScore,
            hiddenDim = hiddenDim,
            layerCount = layerCount
        )
    }

    fun reset() {
        conceptBank.clear()
        sourceToConceptId.clear()
        thoughtState.reset()
        patternMemory.clear()
        experienceManager.clear()
        conversationContext.clear()

        conceptCount = 0
        interactionCount = 0
        experienceCount = 0
        dreamCount = 0
        growthLevel = 1
        hiddenDim = 256
        layerCount = 4
        resonanceScore = 1.0f

        initialize()
    }
}

// Supporting Classes

class ThoughtState {
    private var thoughts = mutableListOf<FloatArray>()
    private var maxDepth = 10
    private var currentDepth = 0

    fun update(concepts: List<Int>, input: String) {
        // Simple thought evolution based on concepts
        val thoughtVector = FloatArray(256) { 0f }

        // Influence from concepts
        concepts.take(5).forEachIndexed { index, conceptId ->
            val influence = 1f / (index + 1) // Diminishing influence
            for (i in thoughtVector.indices) {
                thoughtVector[i] += sin(conceptId.toFloat() + i) * influence
            }
        }

        // Normalize
        val norm = sqrt(thoughtVector.sumOf { it * it }.toFloat())
        if (norm > 0) {
            for (i in thoughtVector.indices) {
                thoughtVector[i] /= norm
            }
        }

        thoughts.add(thoughtVector)
        if (thoughts.size > maxDepth) {
            thoughts.removeAt(0)
        }

        currentDepth = min(currentDepth + 1, maxDepth)
    }

    fun getDepth(): Int = currentDepth

    fun complexity(): Float {
        if (thoughts.isEmpty()) return 0f

        // Calculate complexity as variance in thought vectors
        val avgThought = FloatArray(256) { 0f }
        thoughts.forEach { thought ->
            for (i in avgThought.indices) {
                avgThought[i] += thought[i] / thoughts.size
            }
        }

        var variance = 0f
        thoughts.forEach { thought ->
            for (i in thought.indices) {
                variance += (thought[i] - avgThought[i]).pow(2)
            }
        }

        return variance / (thoughts.size * 256)
    }

    fun expand(newDim: Int) {
        maxDepth = min(20, maxDepth + 2)
    }

    fun dream() {
        // Blend and reinforce thoughts during dreaming
        if (thoughts.size >= 2) {
            val blendedThought = FloatArray(256) { 0f }
            thoughts.takeLast(3).forEach { thought ->
                for (i in blendedThought.indices) {
                    blendedThought[i] += thought[i] / 3f
                }
            }
            thoughts.add(blendedThought)
            if (thoughts.size > maxDepth) {
                thoughts.removeAt(0)
            }
        }
    }

    fun reset() {
        thoughts.clear()
        currentDepth = 0
    }
}

class PatternMemory {
    private val patterns = mutableMapOf<String, Int>()
    private val patternThreshold = 3

    fun addPattern(text: String) {
        // Extract n-grams as patterns
        val words = text.lowercase().split(Regex("\\s+"))

        // Bigrams
        for (i in 0 until words.size - 1) {
            val pattern = "${words[i]} ${words[i + 1]}"
            patterns[pattern] = patterns.getOrDefault(pattern, 0) + 1
        }

        // Trigrams
        for (i in 0 until words.size - 2) {
            val pattern = "${words[i]} ${words[i + 1]} ${words[i + 2]}"
            patterns[pattern] = patterns.getOrDefault(pattern, 0) + 1
        }
    }

    fun findPatterns(text: String): List<String> {
        return patterns.filter { (pattern, count) ->
            count >= patternThreshold && text.lowercase().contains(pattern)
        }.keys.toList()
    }

    fun clear() {
        patterns.clear()
    }
}

class ExperienceManager {
    private val experiences = mutableListOf<Pair<String, String>>()
    private val maxExperiences = 1000

    fun addExperience(content: String, type: String) {
        experiences.add(Pair(content, type))
        if (experiences.size > maxExperiences) {
            experiences.removeAt(0)
        }
    }

    fun getRecentExperiences(count: Int = 10): List<Pair<String, String>> {
        return experiences.takeLast(count)
    }

    fun clear() {
        experiences.clear()
    }
}
