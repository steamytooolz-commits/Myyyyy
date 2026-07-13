// =========================================
// File: data/repository/EventRepository.kt
// =========================================
package com.example.lifesim.data.repository

import com.example.lifesim.domain.ai.AIEventResult
import com.example.lifesim.domain.ai.AIManager
import com.example.lifesim.domain.ai.ContextBuilder
import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.RelationshipEntity
import com.example.lifesim.data.local.entity.CareerEntity
import javax.inject.Inject
import javax.inject.Singleton

interface EventRepository {
    suspend fun getLifeEvent(character: CharacterEntity, memories: List<MemoryEntity>, relationships: List<RelationshipEntity>, career: CareerEntity?): AIEventResult
}

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val aiManager: AIManager,
    private val contextBuilder: ContextBuilder
) : EventRepository {

    override suspend fun getLifeEvent(
        character: CharacterEntity,
        memories: List<MemoryEntity>,
        relationships: List<RelationshipEntity>,
        career: CareerEntity?
    ): AIEventResult {
        val summary = contextBuilder.buildCharacterSummary(character)
        val situation = contextBuilder.buildCurrentSituation(character, career)
        val memSummary = contextBuilder.buildRecentMemorySummary(memories)
        val relSummary = contextBuilder.buildSocialGraphSummary(relationships)
        return aiManager.generateLifeEvent(summary, situation, memSummary, relSummary)
    }
}
