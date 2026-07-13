package com.example.lifesim.domain.usecase

import com.example.lifesim.data.local.dao.CharacterDao
import com.example.lifesim.data.local.dao.MemoryDao
import com.example.lifesim.data.local.dao.RelationshipDao
import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.RelationType
import com.example.lifesim.domain.engine.DynastyAndGeneticsEngine
import javax.inject.Inject

class GenerateHeirUseCase @Inject constructor(
    private val characterDao: CharacterDao,
    private val memoryDao: MemoryDao,
    private val relationshipDao: RelationshipDao,
    private val geneticsEngine: DynastyAndGeneticsEngine
) {
    suspend operator fun invoke(deceasedCharacterId: String, currentYear: Int): CharacterEntity? {
        val deceased = characterDao.getCharacterById(deceasedCharacterId) ?: return null
        val relationships = relationshipDao.getActiveRelationships(deceasedCharacterId)
        val spouseRel = relationships.find { it.relationType == RelationType.SPOUSE || it.relationType == RelationType.PARTNER }
        val spouse = spouseRel?.let { characterDao.getCharacterById(it.targetCharacterId) }

        val children = deceased.childIds.mapNotNull { characterDao.getCharacterById(it) }
        val oldestChild = if (children.isNotEmpty()) children.minByOrNull { it.dateOfBirth } else null

        val heir = if (oldestChild != null) {
            val memories = memoryDao.getMostIntenseMemories(deceasedCharacterId, 20)
            val trait = geneticsEngine.determineGenerationalTrait(memories, deceased.totalNetWorth)
            val withTraits = if (trait != null) geneticsEngine.applyGenerationalTraitsToChild(oldestChild, trait) else oldestChild
            geneticsEngine.applyGenerationalTrauma(withTraits, deceased)
        } else if (spouse != null) {
            val memories = memoryDao.getMostIntenseMemories(deceasedCharacterId, 20)
            val trait = geneticsEngine.determineGenerationalTrait(memories, deceased.totalNetWorth)
            val withTraits = if (trait != null) geneticsEngine.applyGenerationalTraitsToChild(spouse, trait) else spouse
            geneticsEngine.applyGenerationalTrauma(withTraits, deceased)
        } else null

        if (heir != null && heir.characterId != deceasedCharacterId) {
            val heirsList = listOfNotNull(heir, spouse).distinctBy { it.characterId }
            val inheritance = geneticsEngine.distributeInheritance(deceased, heirsList, null)
            val inheritedHeir = inheritance.firstOrNull { it.first.characterId == heir.characterId }?.let {
                var h = heir.copy(cash = heir.cash + it.second)
                characterDao.updateCharacter(h)
                h
            } ?: heir
            return inheritedHeir
        }
        return heir
    }
}
