package com.example.lifesim.data.repository

import com.example.lifesim.data.local.dao.CharacterDao
import com.example.lifesim.data.local.dao.MemoryDao
import com.example.lifesim.data.local.dao.RelationshipDao
import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.RelationshipEntity
import com.example.lifesim.util.generateId
import com.example.lifesim.util.generateRandomName
import javax.inject.Inject
import javax.inject.Singleton

interface CharacterRepository {
    suspend fun getCharacter(id: String): CharacterEntity?
    suspend fun createCharacter(firstName: String? = null, lastName: String? = null, birthYear: Int): CharacterEntity
    suspend fun updateCharacter(character: CharacterEntity)
    suspend fun deleteCharacter(id: String)
    suspend fun getMemories(characterId: String): List<MemoryEntity>
    suspend fun addMemory(characterId: String, memory: MemoryEntity)
    suspend fun getRelationships(characterId: String): List<RelationshipEntity>
}

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao,
    private val memoryDao: MemoryDao,
    private val relationshipDao: RelationshipDao
) : CharacterRepository {

    override suspend fun getCharacter(id: String): CharacterEntity? = characterDao.getCharacterById(id)

    override suspend fun createCharacter(firstName: String?, lastName: String?, birthYear: Int): CharacterEntity {
        val (fName, lName) = if (firstName == null) generateRandomName() else Pair(firstName, lastName ?: "")
        val character = CharacterEntity(
            characterId = generateId(),
            firstName = fName,
            lastName = lName,
            dateOfBirth = (birthYear * 31557600000L),
            gender = if (kotlin.random.Random.nextFloat() < 0.5f) com.example.lifesim.data.local.entity.Gender.MALE
                     else com.example.lifesim.data.local.entity.Gender.FEMALE,
            cash = 5000.0,
            totalNetWorth = 5000.0
        )
        characterDao.insertCharacter(character)
        return character
    }

    override suspend fun updateCharacter(character: CharacterEntity) = characterDao.updateCharacter(character)

    override suspend fun deleteCharacter(id: String) {
        val c = characterDao.getCharacterById(id) ?: return
        characterDao.markAsDeceased(id)
    }

    override suspend fun getMemories(characterId: String): List<MemoryEntity> =
        memoryDao.getMostIntenseMemories(characterId, 100)

    override suspend fun addMemory(characterId: String, memory: MemoryEntity) =
        memoryDao.insertMemory(memory)

    override suspend fun getRelationships(characterId: String): List<RelationshipEntity> =
        relationshipDao.getActiveRelationships(characterId)
}
