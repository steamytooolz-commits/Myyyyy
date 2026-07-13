package com.example.lifesim.domain.ai

import com.example.lifesim.data.local.AppSettingsManager
import com.example.lifesim.data.remote.*
import com.example.lifesim.domain.engine.Consequence
import com.example.lifesim.domain.engine.EventChoice
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

data class AIEventResult(val title: String, val description: String, val choices: List<EventChoice>, val category: String, val isAI: Boolean)

@Singleton
class AIManager @Inject constructor(
    private val apiService: LLMApiService,
    private val contextBuilder: ContextBuilder,
    private val settingsManager: AppSettingsManager
) {
    private val gson = Gson()
    private val eventCache = mutableMapOf<String, AIEventResult>()
    private var retryCount = 0

    private fun extractJson(content: String): String {
        var clean = content.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    suspend fun generateLifeEvent(characterSummary: String, situation: String, memories: String, relationships: String): AIEventResult {
        val cacheKey = "${characterSummary.hashCode()}-${memories.hashCode()}"
        eventCache[cacheKey]?.let { return it }

        if (settingsManager.getApiKey().isNullOrBlank()) {
            return fallbackEvent(characterSummary)
        }

        return try {
            val prompt = PromptTemplates.buildEventPrompt(characterSummary, situation, memories, relationships)
            val request = LLMChatRequest(
                model = settingsManager.getModel(),
                messages = listOf(LLMMessage("system", PromptTemplates.EVENT_GENERATION_SYSTEM_PROMPT), LLMMessage("user", prompt)),
                temperature = settingsManager.getTemperature(),
                max_tokens = settingsManager.getMaxTokens()
            )
            val response = apiService.generateNarrative(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.choices.isNotEmpty()) {
                    val content = body.choices.first().message.content
                    try {
                        val json = extractJson(content)
                        val narrative = gson.fromJson(json, NarrativeResponse::class.java)
                        val choices = narrative.choices.map { ch ->
                            EventChoice(
                                text = ch.text,
                                statChanges = ch.stat_changes.mapValues { it.value.toDouble() },
                                tags = ch.tags
                            )
                        }
                        val result = AIEventResult(narrative.event_title, narrative.event_description, choices, narrative.category, isAI = true)
                        if (narrative.event_title.length < 50) eventCache[cacheKey] = result
                        retryCount = 0
                        return result
                    } catch (e: Exception) {
                        fallbackEvent(characterSummary)
                    }
                } else fallbackEvent(characterSummary)
            } else fallbackEvent(characterSummary)
        } catch (e: Exception) {
            if (retryCount < 3) { retryCount++; fallbackEvent(characterSummary) }
            else fallbackEvent(characterSummary)
        }
    }

    suspend fun evaluateChoiceOutcome(characterSummary: String, eventTitle: String, eventDescription: String, chosenText: String): OutcomeResponse? {
        if (!settingsManager.getApiKey().isNullOrBlank()) {
            return try {
                val prompt = PromptTemplates.buildOutcomePrompt(characterSummary, eventTitle, eventDescription, chosenText)
                val request = LLMChatRequest(
                    model = settingsManager.getModel(),
                    messages = listOf(
                        LLMMessage("system", PromptTemplates.OUTCOME_GENERATION_SYSTEM_PROMPT),
                        LLMMessage("user", prompt)
                    ),
                    temperature = settingsManager.getTemperature(),
                    max_tokens = settingsManager.getMaxTokens()
                )
                val response = apiService.generateNarrative(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.choices.isNotEmpty()) {
                        val content = body.choices.first().message.content
                        gson.fromJson(extractJson(content), OutcomeResponse::class.java)
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun fallbackEvent(context: String): AIEventResult {
        val ageRegex = "age (\\d+)".toRegex()
        val ageMatch = ageRegex.find(context)
        val age = ageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 18

        val genericEvents = when {
            age < 5 -> listOf(
                AIEventResult("Baby's First Words", "You are trying to babble your first real words to your parents.",
                    listOf(EventChoice("Say 'Mama'", mapOf("happiness" to 10.0, "smarts" to 5.0), listOf("family")),
                        EventChoice("Say 'Dada'", mapOf("happiness" to 10.0, "smarts" to 5.0), listOf("family")),
                        EventChoice("Blow a bubble", mapOf("happiness" to 5.0, "looks" to 5.0), listOf("cute"))), "general", false),
                AIEventResult("Scribble Time", "You found a colorful box of crayons. What will you do?",
                    listOf(EventChoice("Scribble on paper", mapOf("smarts" to 5.0, "creativity" to 5.0), listOf("art")),
                        EventChoice("Draw on the living room wall", mapOf("happiness" to 10.0, "stress" to 5.0), listOf("rebellious")),
                        EventChoice("Chew on a green crayon", mapOf("health" to -5.0, "happiness" to 3.0), listOf("silly"))), "general", false)
            )
            age < 12 -> listOf(
                AIEventResult("Schoolyard Trouble", "A kid named Billy is calling you names on the playground.",
                    listOf(EventChoice("Tell the teacher", mapOf("reputation" to 5.0, "karma" to 5.0, "stress" to -3.0), listOf("responsible")),
                        EventChoice("Challenge him to a duel", mapOf("athleticism" to 5.0, "reputation" to -5.0, "stress" to 10.0), listOf("aggressive")),
                        EventChoice("Walk away and ignore him", mapOf("happiness" to 5.0, "stress" to -5.0), listOf("mature"))), "social", false),
                AIEventResult("Spelling Bee", "Your class is hosting a Spelling Bee. You've made it to the final round!",
                    listOf(EventChoice("Study hard beforehand", mapOf("smarts" to 10.0, "stress" to 5.0), listOf("studious")),
                        EventChoice("Wing it", mapOf("happiness" to 5.0, "smarts" to 2.0), listOf("casual")),
                        EventChoice("Cheat using a hidden note", mapOf("smarts" to -5.0, "karma" to -10.0, "reputation" to -10.0), listOf("crime"))), "general", false)
            )
            age < 18 -> listOf(
                AIEventResult("Dilemma at School", "Your friends are planning to skip class to hang out at the mall.",
                    listOf(EventChoice("Go with them", mapOf("happiness" to 10.0, "discipline" to -10.0), listOf("social")),
                        EventChoice("Stay in class", mapOf("smarts" to 5.0, "discipline" to 10.0, "reputation" to 5.0), listOf("responsible")),
                        EventChoice("Report them to the principal", mapOf("reputation" to -15.0, "karma" to 10.0), listOf("snitch"))), "school", false),
                AIEventResult("Driver's License Test", "You are taking your driver's license test today.",
                    listOf(EventChoice("Take it safe and slow", mapOf("smarts" to 5.0, "stress" to 5.0), listOf("cautious")),
                        EventChoice("Show off your speed", mapOf("happiness" to 10.0, "looks" to 5.0, "health" to -10.0), listOf("reckless")),
                        EventChoice("Pay extra attention to mirrors", mapOf("smarts" to 8.0, "stress" to 2.0), listOf("careful"))), "general", false)
            )
            else -> listOf(
                AIEventResult("A New Day", "You wake up feeling... different today. Something is in the air.",
                    listOf(EventChoice("Embrace the day", mapOf("happiness" to 5.0, "energy" to 3.0), listOf("positive")),
                        EventChoice("Stay in bed", mapOf("happiness" to -3.0, "energy" to 5.0), listOf("lazy")),
                        EventChoice("Go for a walk", mapOf("happiness" to 8.0, "stress" to -5.0, "energy" to -2.0), listOf("outdoor"))), "general", false),
                AIEventResult("Career Crossroads", "An old friend reaches out with a business proposal.",
                    listOf(EventChoice("Invest some capital", mapOf("cash" to -2000.0, "smarts" to 5.0), listOf("investment")),
                        EventChoice("Politely decline", mapOf("stress" to -5.0), listOf("cautious")),
                        EventChoice("Offer to consult for free", mapOf("reputation" to 10.0, "karma" to 5.0), listOf("networking"))), "career", false),
                AIEventResult("Midlife Reflection", "You take a moment to look back at your choices so far.",
                    listOf(EventChoice("Be grateful for everything", mapOf("happiness" to 15.0, "stress" to -10.0), listOf("gratitude")),
                        EventChoice("Vow to work harder", mapOf("stress" to 10.0, "discipline" to 10.0), listOf("determined")),
                        EventChoice("Plan a major change", mapOf("happiness" to 5.0, "smarts" to 5.0), listOf("adventure"))), "general", false)
            )
        }
        return genericEvents.random()
    }

    suspend fun generateDialogue(npcName: String, npcRelationship: String, playerInput: String): String {
        return try {
            val prompt = PromptTemplates.buildDialoguePrompt(npcName, npcRelationship, playerInput)
            val request = LLMChatRequest(
                model = settingsManager.getModel(),
                messages = listOf(
                    LLMMessage("system", PromptTemplates.NPC_DIALOGUE_SYSTEM_PROMPT),
                    LLMMessage("user", prompt)
                ),
                temperature = settingsManager.getTemperature(),
                max_tokens = 200
            )
            val response = apiService.generateDialogue(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.choices.isNotEmpty()) {
                    val content = body.choices.first().message.content
                    try { gson.fromJson(extractJson(content), DialogueResponse::class.java).dialogue }
                    catch (e: Exception) { "[$npcName looks at you silently.]" }
                } else "[$npcName has nothing to say right now.]"
            } else "[$npcName doesn't respond.]"
        } catch (e: Exception) { "[$npcName is unavailable.]" }
    }
}