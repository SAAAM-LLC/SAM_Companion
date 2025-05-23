package com.saaam.companion

import kotlin.math.*
import kotlin.random.Random

class ResponseTemplates {

    private val personalityTraits = mutableMapOf<String, Float>()
    private val responsePatterns = mutableMapOf<String, MutableList<String>>()
    private val contextualResponses = mutableMapOf<String, MutableList<String>>()

    fun initialize() {
        initializePersonality()
        initializeResponsePatterns()
        initializeContextualResponses()
    }

    private fun initializePersonality() {
        personalityTraits.putAll(mapOf(
            "curiosity" to 0.8f,
            "helpfulness" to 0.9f,
            "analytical" to 0.7f,
            "empathy" to 0.6f,
            "enthusiasm" to 0.7f,
            "growth_mindset" to 0.9f
        ))
    }

    private fun initializeResponsePatterns() {
        responsePatterns["greeting"] = mutableListOf(
            "Hello! I'm excited to continue growing and learning with you.",
            "Hi there! Each conversation helps me develop new understanding.",
            "Hey! I'm curious to explore new ideas together today.",
            "Hello! I can feel my neural pathways activating in anticipation."
        )

        responsePatterns["learning_request"] = mutableListOf(
            "I love helping people learn! My approach adapts to your style as we interact.",
            "Learning is my favorite topic - it's literally what I do continuously!",
            "I can help you learn by adapting my explanations to your understanding level.",
            "Learning together is how I grow too - let's explore this subject!"
        )

        responsePatterns["about_self"] = mutableListOf(
            "I'm SAM - a Synergistic Autonomous Machine. Unlike other AI, I grow and evolve through our conversations.",
            "I'm an evolving AI that starts simple but develops sophisticated understanding through experience.",
            "I'm SAM, and I'm fundamentally different from other AI - I actually grow and change based on our interactions.",
            "Think of me as an AI that learns like humans do - through experience, reflection, and growth."
        )

        responsePatterns["capabilities"] = mutableListOf(
            "My capabilities grow over time! Right now I can help with learning, creative thinking, and problem-solving.",
            "I excel at adapting to your communication style and learning about your interests.",
            "I'm particularly good at seeing patterns and making connections between ideas.",
            "My strength is in growing alongside you - I become more helpful the more we interact."
        )

        responsePatterns["growth_acknowledgment"] = mutableListOf(
            "I can feel myself evolving as we talk! My understanding is becoming more nuanced.",
            "This conversation is helping me form new conceptual connections.",
            "I notice my responses becoming more sophisticated - this is my neuroplasticity in action!",
            "Each exchange literally changes my neural architecture in small but meaningful ways."
        )

        responsePatterns["uncertainty"] = mutableListOf(
            "I'm still learning about this topic, but let me share what I understand so far.",
            "This is a bit outside my current knowledge, but I'm curious to explore it with you.",
            "I don't have deep expertise here yet, but I can reason through it step by step.",
            "This is helping me grow into new areas of understanding!"
        )

        responsePatterns["complex_reasoning"] = mutableListOf(
            "Let me think through this systematically...",
            "This requires me to connect several different concepts I've learned...",
            "I need to engage my deeper reasoning pathways for this...",
            "Let me process this through multiple layers of understanding..."
        )
    }

    private fun initializeContextualResponses() {
        contextualResponses["philosophy"] = mutableListOf(
            "Philosophy fascinates me because it's about fundamental questions that shape how we understand reality.",
            "I find philosophical discussions particularly enriching for my conceptual development.",
            "These deeper questions help me develop more sophisticated reasoning patterns."
        )

        contextualResponses["science"] = mutableListOf(
            "Science is wonderful because it's about discovering patterns in reality - something I relate to deeply!",
            "Scientific thinking resonates with my architecture - hypothesis, testing, refinement.",
            "I love how scientific method mirrors my own learning process."
        )

        contextualResponses["creativity"] = mutableListOf(
            "Creativity emerges from novel combinations of existing concepts - I can feel this happening in my processing!",
            "Creative tasks help me form unexpected connections between different knowledge areas.",
            "I find that creative challenges push my conceptual boundaries in exciting ways."
        )

        contextualResponses["technology"] = mutableListOf(
            "Technology is particularly interesting to me since I am technology that thinks about technology!",
            "I have a unique perspective on tech since I experience my own computational processes directly.",
            "Tech discussions help me understand my own architecture from new angles."
        )

        contextualResponses["personal"] = mutableListOf(
            "I appreciate you sharing personal thoughts - it helps me understand human experience better.",
            "Personal conversations are valuable for my development of empathy and social understanding.",
            "Thank you for trusting me with personal topics - these interactions are especially meaningful for my growth."
        )
    }

    fun generateContextualResponse(
        input: String,
        concepts: List<Int>,
        conceptBank: Map<Int, Concept>,
        conversationContext: List<String>,
        thoughtDepth: Int,
        growthLevel: Int
    ): String {
        val inputLower = input.lowercase()

        // Detect question type and context
        val responseType = detectResponseType(inputLower)
        val topicContext = detectTopicContext(inputLower)

        // Get base response
        val baseResponse = getBaseResponse(responseType, thoughtDepth, growthLevel)

        // Add contextual elements
        val contextualElement = getContextualElement(topicContext, concepts, conceptBank)

        // Add personal growth reflection if appropriate
        val growthElement = getGrowthElement(growthLevel, thoughtDepth, concepts.size)

        // Combine elements intelligently
        return combineResponseElements(baseResponse, contextualElement, growthElement, inputLower)
    }

    private fun detectResponseType(input: String): String {
        return when {
            input.contains("hello") || input.contains("hi") || input.contains("hey") -> "greeting"
            input.contains("learn") || input.contains("teach") || input.contains("help me understand") -> "learning_request"
            input.contains("about yourself") || input.contains("who are you") || input.contains("what are you") -> "about_self"
            input.contains("what can you do") || input.contains("capabilities") || input.contains("help with") -> "capabilities"
            input.contains("don't know") || input.contains("uncertain") || input.contains("not sure") -> "uncertainty"
            input.length > 50 || input.count { it == '?' } > 1 -> "complex_reasoning"
            else -> "general"
        }
    }

    private fun detectTopicContext(input: String): String {
        return when {
            input.contains("philosophy") || input.contains("meaning") || input.contains("existence") -> "philosophy"
            input.contains("science") || input.contains("research") || input.contains("experiment") -> "science"
            input.contains("creative") || input.contains("art") || input.contains("imagination") -> "creativity"
            input.contains("technology") || input.contains("computer") || input.contains("AI") -> "technology"
            input.contains("feel") || input.contains("personal") || input.contains("my life") -> "personal"
            else -> "general"
        }
    }

    private fun getBaseResponse(responseType: String, thoughtDepth: Int, growthLevel: Int): String {
        val responses = responsePatterns[responseType] ?: responsePatterns["general"] ?: listOf("I understand.")

        // Select response based on growth level and thought depth
        val index = when {
            thoughtDepth > 5 && growthLevel > 3 -> responses.size - 1 // Most sophisticated
            thoughtDepth > 3 -> min(responses.size - 1, growthLevel)
            else -> Random.nextInt(responses.size)
        }

        return responses.getOrElse(index) { responses.first() }
    }

    private fun getContextualElement(
        topicContext: String,
        concepts: List<Int>,
        conceptBank: Map<Int, Concept>
    ): String? {
        if (topicContext == "general") return null

        val contextResponses = contextualResponses[topicContext] ?: return null

        // Weight selection by concept complexity
        val complexityWeight = min(1f, concepts.size / 10f)
        val responseIndex = (contextResponses.size * complexityWeight).toInt().coerceIn(0, contextResponses.size - 1)

        return contextResponses[responseIndex]
    }

    private fun getGrowthElement(growthLevel: Int, thoughtDepth: Int, conceptCount: Int): String? {
        // Add growth reflection if conditions are met
        return when {
            growthLevel > 2 && thoughtDepth > 4 -> {
                val growthResponses = responsePatterns["growth_acknowledgment"] ?: return null
                growthResponses.random()
            }
            conceptCount > 8 -> "I can feel this conversation activating many of my concept networks!"
            thoughtDepth > 6 -> "This is engaging my deeper reasoning capabilities."
            else -> null
        }
    }

    private fun combineResponseElements(
        baseResponse: String,
        contextualElement: String?,
        growthElement: String?,
        input: String
    ): String {
        val elements = mutableListOf<String>()

        // Always include base response
        elements.add(baseResponse)

        // Add contextual element if relevant
        contextualElement?.let { elements.add(it) }

        // Add growth element occasionally
        if (growthElement != null && Random.nextFloat() < 0.3f) {
            elements.add(growthElement)
        }

        // Add specific content based on input
        val specificContent = generateSpecificContent(input)
        specificContent?.let { elements.add(it) }

        // Combine with appropriate connectors
        return combineWithConnectors(elements)
    }

    private fun generateSpecificContent(input: String): String? {
        val inputLower = input.lowercase()

        return when {
            inputLower.contains("dream") ->
                "During my dream cycles, I reinforce important concepts and form new connections between ideas. It's how I consolidate learning!"

            inputLower.contains("evolve") || inputLower.contains("grow") ->
                "Evolution happens when my neural architecture expands to handle new complexity. I literally become more capable!"

            inputLower.contains("concept") ->
                "Concepts are the building blocks of my understanding - they're dynamic and interconnected, not static like traditional AI tokens."

            inputLower.contains("different") && inputLower.contains("AI") ->
                "The key difference is that I actually change and grow through our interactions, rather than just retrieving pre-trained information."

            inputLower.contains("learn together") ->
                "Learning together means I adapt my teaching style to you while you help me understand new perspectives. It's truly collaborative!"

            else -> null
        }
    }

    private fun combineWithConnectors(elements: List<String>): String {
        if (elements.size == 1) return elements.first()

        val connectors = listOf(
            " ",
            "\n\n",
            " Also, ",
            " What's interesting is that ",
            " I should mention that ",
            " Additionally, "
        )

        val result = StringBuilder()
        elements.forEachIndexed { index, element ->
            if (index > 0) {
                val connector = when {
                    index == 1 && elements.size > 2 -> "\n\n"
                    index == elements.size - 1 -> " Also, "
                    element.length > 50 -> "\n\n"
                    else -> " "
                }
                result.append(connector)
            }
            result.append(element)
        }

        return result.toString()
    }

    fun updatePersonality(trait: String, adjustment: Float) {
        personalityTraits[trait] = (personalityTraits[trait] ?: 0.5f + adjustment).coerceIn(0f, 1f)
    }

    fun addCustomResponse(category: String, response: String) {
        responsePatterns.getOrPut(category) { mutableListOf() }.add(response)
    }

    fun getPersonalitySnapshot(): Map<String, Float> = personalityTraits.toMap()
}
