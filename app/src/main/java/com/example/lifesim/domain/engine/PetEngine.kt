// =========================================
// File: domain/engine/PetEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

enum class PetType(val label: String, val baseCost: Double, val lifespanYears: Int, val careFrequency: Int) {
    DOG("Dog", 500.0, 13, 2),      // needs care every 2 years
    CAT("Cat", 300.0, 15, 3),
    BIRD("Bird", 100.0, 10, 4),
    FISH("Fish", 50.0, 5, 5),
    HAMSTER("Hamster", 80.0, 3, 4),
    RABBIT("Rabbit", 150.0, 8, 3),
    REPTILE("Reptile", 400.0, 20, 3),
    HORSE("Horse", 3000.0, 25, 1),
    EXOTIC("Exotic Pet", 5000.0, 15, 1)
}

enum class PetBreed(val type: PetType, val displayName: String, val size: String, val personalityTraits: List<String>) {
    // Dogs
    GOLDEN_RETRIEVER(PetType.DOG, "Golden Retriever", "Large", listOf("friendly", "loyal", "energetic")),
    LABRADOR(PetType.DOG, "Labrador", "Large", listOf("playful", "gentle", "smart")),
    BULLDOG(PetType.DOG, "Bulldog", "Medium", listOf("calm", "brave", "stubborn")),
    POODLE(PetType.DOG, "Poodle", "Medium", listOf("smart", "elegant", "active")),
    CHIHUAHUA(PetType.DOG, "Chihuahua", "Small", listOf("feisty", "alert", "devoted")),
    GERMAN_SHEPHERD(PetType.DOG, "German Shepherd", "Large", listOf("loyal", "courageous", "disciplined")),
    // Cats
    PERSIAN(PetType.CAT, "Persian Cat", "Medium", listOf("calm", "gentle", "quiet")),
    SIAMESE(PetType.CAT, "Siamese Cat", "Medium", listOf("vocal", "social", "smart")),
    MAINE_COON(PetType.CAT, "Maine Coon", "Large", listOf("gentle", "playful", "dog-like")),
    BENGAL(PetType.CAT, "Bengal Cat", "Medium", listOf("active", "curious", "wild")),
    // Others
    BUDGIE(PetType.BIRD, "Budgie", "Small", listOf("chatty", "colorful", "social")),
    MACAW(PetType.BIRD, "Macaw", "Large", listOf("intelligent", "loud", "long-lived")),
    GOLDFISH(PetType.FISH, "Goldfish", "Small", listOf("easy", "peaceful")),
    BETTA(PetType.FISH, "Betta Fish", "Small", listOf("beautiful", "solitary")),
    SYRIAN(PetType.HAMSTER, "Syrian Hamster", "Small", listOf("nocturnal", "curious", "solitary")),
    DWARF_HAMSTER(PetType.HAMSTER, "Dwarf Hamster", "Small", listOf("social", "fast", "tiny")),
    LOP(PetType.RABBIT, "Lop Rabbit", "Medium", listOf("gentle", "floppy", "calm")),
    REX(PetType.RABBIT, "Rex Rabbit", "Medium", listOf("curious", "playful", "soft")),
    BEARDED_DRAGON(PetType.REPTILE, "Bearded Dragon", "Medium", listOf("docile", "curious", "sun-loving")),
    BALL_PYTHON(PetType.REPTILE, "Ball Python", "Medium", listOf("calm", "shy", "low-maintenance")),
    THOROUGHBRED(PetType.HORSE, "Thoroughbred", "Large", listOf("fast", "spirited", "elegant")),
    ARABIAN(PetType.HORSE, "Arabian Horse", "Large", listOf("intelligent", "enduring", "proud")),
    FENNEC_FOX(PetType.EXOTIC, "Fennec Fox", "Small", listOf("exotic", "playful", "nocturnal")),
    KINKAJOU(PetType.EXOTIC, "Kinkajou", "Small", listOf("exotic", "curious", "nocturnal"))
}

data class Pet(
    val id: String = java.util.UUID.randomUUID().toString(),
    val breed: PetBreed,
    val name: String,
    val age: Int = 0,             // in years
    val health: Double = 100.0,    // 0-100
    val happiness: Double = 80.0,  // 0-100
    val hunger: Double = 80.0,     // 0-100
    val energy: Double = 80.0,     // 0-100
    val cleanliness: Double = 80.0,// 0-100
    val obedience: Double = 30.0,  // 0-100 (trainable)
    val isAlive: Boolean = true,
    val lastCaredYear: Int = 2024, // last year they received care
    val acquisitionYear: Int = 2024
)

@Singleton
class PetEngine @Inject constructor() {

    /** Serialize pets to string for storage */
    fun serializePets(pets: List<Pet>): String =
        pets.joinToString("|") { pet ->
            "${pet.id},${pet.breed.name},${pet.name},${pet.age},${pet.health.toInt()}," +
            "${pet.happiness.toInt()},${pet.hunger.toInt()},${pet.energy.toInt()}," +
            "${pet.cleanliness.toInt()},${pet.obedience.toInt()},${pet.isAlive}," +
            "${pet.lastCaredYear},${pet.acquisitionYear}"
        }

    /** Deserialize pets from string */
    fun deserializePets(data: String): List<Pet> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size < 13) return@mapNotNull null
            val breed = PetBreed.entries.find { it.name == parts[1] } ?: return@mapNotNull null
            Pet(
                id = parts[0], breed = breed, name = parts[2],
                age = parts[3].toIntOrNull() ?: 0,
                health = (parts[4].toIntOrNull() ?: 100).toDouble(),
                happiness = (parts[5].toIntOrNull() ?: 80).toDouble(),
                hunger = (parts[6].toIntOrNull() ?: 80).toDouble(),
                energy = (parts[7].toIntOrNull() ?: 80).toDouble(),
                cleanliness = (parts[8].toIntOrNull() ?: 80).toDouble(),
                obedience = (parts[9].toIntOrNull() ?: 30).toDouble(),
                isAlive = parts[10].toBooleanStrictOrNull() ?: true,
                lastCaredYear = parts[11].toIntOrNull() ?: 2024,
                acquisitionYear = parts[12].toIntOrNull() ?: 2024
            )
        }
    }

    /** Adopt a pet — costs money, adds to character */
    fun adoptPet(character: CharacterEntity, breed: PetBreed, name: String, year: Int): CharacterEntity? {
        if (character.cash < breed.type.baseCost) return null
        val existing = deserializePets(character.petsData)
        if (existing.size >= 4) return null // max 4 pets

        val pet = Pet(breed = breed, name = name, acquisitionYear = year, lastCaredYear = year)
        val newList = existing + pet
        return character.copy(
            petsData = serializePets(newList),
            cash = character.cash - breed.type.baseCost,
            happiness = (character.happiness + 5.0).coerceIn(0.0, 100.0),
            stress = (character.stress - 3.0).coerceIn(0.0, 100.0)
        )
    }

    /** Feed a pet — costs money, increases hunger and happiness */
    fun feedPet(character: CharacterEntity, petId: String, year: Int): Pair<CharacterEntity, String> {
        val pets = deserializePets(character.petsData).toMutableList()
        val idx = pets.indexOfFirst { it.id == petId && it.isAlive }
        if (idx < 0) return character to "Pet not found."
        val pet = pets[idx]
        if (character.cash < pet.breed.type.baseCost * 0.1) return character to "Can't afford pet food."

        val cost = (pet.breed.type.baseCost * 0.1).coerceAtLeast(5.0)
        val updated = pet.copy(
            hunger = (pet.hunger + 25.0).coerceIn(0.0, 100.0),
            happiness = (pet.happiness + 5.0).coerceIn(0.0, 100.0),
            lastCaredYear = year
        )
        pets[idx] = updated
        return character.copy(petsData = serializePets(pets), cash = character.cash - cost) to
            "Fed ${pet.name}! Hunger +25%"
    }

    /** Play with a pet — increases happiness, costs energy */
    fun playWithPet(character: CharacterEntity, petId: String): Pair<CharacterEntity, String> {
        val pets = deserializePets(character.petsData).toMutableList()
        val idx = pets.indexOfFirst { it.id == petId && it.isAlive }
        if (idx < 0) return character to "Pet not found."
        val pet = pets[idx]
        val updated = pet.copy(
            happiness = (pet.happiness + 15.0).coerceIn(0.0, 100.0),
            energy = (pet.energy - 10.0).coerceIn(0.0, 100.0),
            obedience = (pet.obedience + 2.0).coerceIn(0.0, 100.0)
        )
        pets[idx] = updated
        return character.copy(
            petsData = serializePets(pets),
            happiness = (character.happiness + 8.0).coerceIn(0.0, 100.0),
            stress = (character.stress - 5.0).coerceIn(0.0, 100.0),
            energy = (character.energy - 5.0).coerceIn(0.0, 100.0)
        ) to "Played with ${pet.name}! Happiness +15%"
    }

    /** Take pet to vet — heals health, costs money */
    fun vetVisit(character: CharacterEntity, petId: String): Pair<CharacterEntity, String> {
        val pets = deserializePets(character.petsData).toMutableList()
        val idx = pets.indexOfFirst { it.id == petId && it.isAlive }
        if (idx < 0) return character to "Pet not found."
        val pet = pets[idx]
        val cost = pet.breed.type.baseCost * 0.5
        if (character.cash < cost) return character to "Vet visit costs ${cost.toInt()}, can't afford it."

        val updated = pet.copy(
            health = (pet.health + 30.0).coerceIn(0.0, 100.0),
            cleanliness = (pet.cleanliness + 10.0).coerceIn(0.0, 100.0)
        )
        pets[idx] = updated
        return character.copy(petsData = serializePets(pets), cash = character.cash - cost) to
            "Vet visit for ${pet.name}! Health +30%, cost $${cost.toInt()}"
    }

    /** Train a pet — increases obedience, costs energy */
    fun trainPet(character: CharacterEntity, petId: String): Pair<CharacterEntity, String> {
        val pets = deserializePets(character.petsData).toMutableList()
        val idx = pets.indexOfFirst { it.id == petId && it.isAlive }
        if (idx < 0) return character to "Pet not found."
        val pet = pets[idx]
        if (pet.obedience >= 95.0) return character to "${pet.name} is fully trained!"

        val gain = 5.0 + Random.nextDouble() * 5.0
        val updated = pet.copy(
            obedience = (pet.obedience + gain).coerceIn(0.0, 100.0),
            energy = (pet.energy - 10.0).coerceIn(0.0, 100.0)
        )
        pets[idx] = updated
        return character.copy(
            petsData = serializePets(pets),
            discipline = (character.discipline + 2.0).coerceIn(0.0, 100.0),
            energy = (character.energy - 4.0).coerceIn(0.0, 100.0)
        ) to "Trained ${pet.name}! Obedience +${gain.toInt()}%"
    }

    /** Walk a dog — for dogs only */
    fun walkPet(character: CharacterEntity, petId: String): Pair<CharacterEntity, String> {
        val pets = deserializePets(character.petsData).toMutableList()
        val idx = pets.indexOfFirst { it.id == petId && it.isAlive }
        if (idx < 0) return character to "Pet not found."
        val pet = pets[idx]
        if (pet.breed.type != PetType.DOG) return character to "Only dogs need walks."

        val updated = pet.copy(
            happiness = (pet.happiness + 12.0).coerceIn(0.0, 100.0),
            energy = (pet.energy - 15.0).coerceIn(0.0, 100.0),
            cleanliness = (pet.cleanliness - 5.0).coerceIn(0.0, 100.0)
        )
        pets[idx] = updated
        return character.copy(
            petsData = serializePets(pets),
            happiness = (character.happiness + 6.0).coerceIn(0.0, 100.0),
            athleticism = (character.athleticism + 2.0).coerceIn(0.0, 100.0),
            energy = (character.energy - 6.0).coerceIn(0.0, 100.0)
        ) to "Walked ${pet.name}! Great exercise for both of you."
    }

    /** Age pets each year — decay stats, may die of old age */
    fun agePets(character: CharacterEntity): CharacterEntity {
        val pets = deserializePets(character.petsData).toMutableList()
        if (pets.isEmpty()) return character
        var c = character
        var anyChanged = false

        pets.forEachIndexed { idx, pet ->
            if (!pet.isAlive) return@forEachIndexed

            var p = pet.copy(age = pet.age + 1)
            val maxAge = p.breed.type.lifespanYears

            // Natural stat decay
            p = p.copy(
                health = (p.health - 2.0 - Random.nextDouble() * 5.0).coerceIn(0.0, 100.0),
                happiness = (p.happiness - 3.0).coerceIn(0.0, 100.0),
                hunger = (p.hunger - 8.0).coerceIn(0.0, 100.0),
                energy = (p.energy - 3.0).coerceIn(0.0, 100.0),
                cleanliness = (p.cleanliness - 5.0).coerceIn(0.0, 100.0)
            )

            // Age-related death probability
            val ageRatio = p.age.toDouble() / maxAge
            if (ageRatio > 0.7 && Random.nextDouble() < (ageRatio - 0.7) * 0.3) {
                p = p.copy(isAlive = false)
                c = c.copy(happiness = (c.happiness - 15.0).coerceIn(0.0, 100.0),
                    stress = (c.stress + 8.0).coerceIn(0.0, 100.0))
            }

            // Neglect death if stats drop too low
            if (p.health <= 0 || p.hunger <= 0) {
                p = p.copy(isAlive = false)
                c = c.copy(happiness = (c.happiness - 20.0).coerceIn(0.0, 100.0),
                    karma = (c.karma - 10).coerceIn(0, 100))
            }

            pets[idx] = p
            anyChanged = true
        }

        return if (anyChanged) c.copy(petsData = serializePets(pets)) else c
    }

    /** Get character stats boost from pets (happiness, stress reduction) */
    fun applyPetBoosts(character: CharacterEntity): CharacterEntity {
        val pets = deserializePets(character.petsData).filter { it.isAlive }
        if (pets.isEmpty()) return character
        var c = character
        val totalBoost = pets.size * 2.0
        c = c.copy(
            happiness = (c.happiness + totalBoost).coerceIn(0.0, 100.0),
            stress = (c.stress - totalBoost).coerceIn(0.0, 100.0)
        )
        // Cash cost for upkeep
        val upkeep = pets.sumOf { it.breed.type.baseCost * 0.05 }
        c = c.copy(cash = (c.cash - upkeep).coerceAtLeast(0.0))
        return c
    }

    /** Get all living pets */
    fun getLivingPets(character: CharacterEntity): List<Pet> =
        deserializePets(character.petsData).filter { it.isAlive }

    /** Get all pets */
    fun getAllPets(character: CharacterEntity): List<Pet> =
        deserializePets(character.petsData)
}
