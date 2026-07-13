package com.example.lifesim.data.remote

import retrofit2.Response
import retrofit2.http.*

data class LLMChatRequest(val model: String, val messages: List<LLMMessage>, val temperature: Float = 0.8f, val max_tokens: Int = 500)
data class LLMMessage(val role: String, val content: String)
data class LLMChatResponse(val id: String, val choices: List<LLMChoice>, val usage: LLMUsage? = null, val model: String, val created: Long)
data class LLMChoice(val index: Int, val message: LLMMessage, val finish_reason: String)
data class LLMUsage(val prompt_tokens: Int, val completion_tokens: Int, val total_tokens: Int)
data class NarrativeResponse(val event_title: String = "", val event_description: String = "", val choices: List<NarrativeChoice> = emptyList(), val category: String = "general")
data class NarrativeChoice(val text: String, val stat_changes: Map<String, Int> = emptyMap(), val tags: List<String> = emptyList())
data class DialogueResponse(val dialogue: String = "", val npc_emotion: String = "neutral", val relationship_change: Map<String, Int> = emptyMap())
data class CharacterProfileResponse(val first_name: String, val last_name: String, val gender: String, val personality_traits: List<String>, val background_story: String, val starting_stats: Map<String, Int>)
data class OutcomeResponse(val outcome_narrative: String = "", val stat_changes: Map<String, Int> = emptyMap())

interface LLMApiService {
    @POST("chat/completions")
    suspend fun generateNarrative(@Body request: LLMChatRequest): Response<LLMChatResponse>

    @POST("chat/completions")
    suspend fun generateDialogue(@Body request: LLMChatRequest): Response<LLMChatResponse>

    @POST("chat/completions")
    suspend fun generateCharacterProfile(@Body request: LLMChatRequest): Response<LLMChatResponse>
}
