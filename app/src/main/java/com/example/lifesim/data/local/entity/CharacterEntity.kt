package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(tableName = "characters")
@TypeConverters(Converters::class)
data class CharacterEntity(
    @PrimaryKey val characterId: String = java.util.UUID.randomUUID().toString(),

    // Core Demographics
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,
    val gender: Gender,
    val sexualOrientation: SexualOrientation = SexualOrientation.STRAIGHT,
    val zodiacSign: ZodiacSign = ZodiacSign.ARIES,

    // Base Stats (0-100)
    val health: Double = 70.0,
    val happiness: Double = 60.0,
    val smarts: Double = 50.0,
    val looks: Double = 50.0,
    val athleticism: Double = 50.0,
    val charisma: Double = 50.0,
    val empathy: Double = 50.0,
    val aggression: Double = 30.0,
    val paranoia: Double = 10.0,
    val discipline: Double = 50.0,
    val creativity: Double = 50.0,
    val stress: Double = 30.0,
    val energy: Double = 80.0,
    val hunger: Double = 70.0,
    val hygiene: Double = 70.0,

    // Hidden Stats
    val karma: Int = 50,
    val reputation: Int = 50,
    val notoriety: Int = 0,
    val sanity: Double = 100.0,

    // State Flags
    val isAlive: Boolean = true,
    val isInPrison: Boolean = false,
    val isInHospital: Boolean = false,
    val isAddicted: Boolean = false,
    val addictionType: String? = null,
    val addictionSeverity: Int = 0,
    val addictionTicksWithoutUse: Int = 0,
    val addictionRecoveryProgress: Int = 0,
    val isInWithdrawal: Boolean = false,
    val withdrawalDuration: Int = 0,
    val currentLocationId: String? = null,
    val currentCareerId: String? = null,
    val currentEducationId: String? = null,

    // Genetics & Dynasty
    val dnaSequence: String = "",
    val generationNumber: Int = 1,
    val ancestorIds: List<String> = emptyList(),
    val dynastyId: String? = null,
    val parentsIds: List<String> = emptyList(),
    val spouseId: String? = null,
    val childIds: List<String> = emptyList(),

    // Prison System
    val prisonGang: String? = null,
    val prisonJob: String? = null,
    val prisonEscapeProgress: Int = 0,
    val prisonContraband: List<String> = emptyList(),
    val prisonYearsServed: Int = 0,
    val prisonDisciplinaryRecord: Int = 0,
    val hasPrisonAllies: Boolean = false,
    val hasPrisonEnemies: Boolean = false,
    val prisonEducationLevel: Int = 0,

    // Military System
    val isInMilitary: Boolean = false,
    val militaryRankTitle: String? = null,
    val militaryPayGrade: Int = 0,
    val militaryYearsServed: Int = 0,
    val militaryCombatDeployments: Int = 0,
    val militaryMedals: List<String> = emptyList(),

    // Political System
    val politicalOfficeTitle: String? = null,
    val politicalOfficeTier: Int = 0,
    val politicalTermYearsRemaining: Int = 0,
    val approvalRating: Int = 50,
    val royalTitle: String? = null,

    // Education System
    val gpa: Double = 0.0,
    val degrees: List<String> = emptyList(),
    val studentLoan: Double = 0.0,
    val educationYearsCompleted: Int = 0,

    // Investment System
    val retirementSavings: Double = 0.0,
    val stockPortfolioValue: Double = 0.0,

    // Hobby System
    val hobbies: List<String> = emptyList(),
    val hobbyData: String = "",

    // Pet System
    val petsData: String = "",

    // Medical System
    val medicalConditions: String = "",
    val insurancePlan: String = "NONE",

    // Money & Assets
    val cash: Double = 5000.0,
    val totalNetWorth: Double = 5000.0,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

enum class Gender { MALE, FEMALE, NON_BINARY }
enum class SexualOrientation { STRAIGHT, GAY, BISEXUAL, ASEXUAL, PANSEXUAL }
enum class ZodiacSign { ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES }
enum class LifeStage { CHILD, TEENAGER, YOUNG_ADULT, ADULT, MIDDLE_AGED, SENIOR, ELDERLY }
