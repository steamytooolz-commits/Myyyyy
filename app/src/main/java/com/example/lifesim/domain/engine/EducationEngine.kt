// =========================================
// File: domain/engine/EducationEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class Degree(val name: String, val field: String, val level: Int, val yearsRequired: Int)
data class School(val name: String, val type: SchoolType, val tuition: Double, val acceptanceRate: Double, val prestige: Int)

enum class SchoolType { PRESCHOOL, ELEMENTARY, MIDDLE_SCHOOL, HIGH_SCHOOL, COMMUNITY_COLLEGE, UNIVERSITY, GRADUATE_SCHOOL, ONLINE }

@Singleton
class EducationEngine @Inject constructor() {

    private val majors = listOf("Computer Science", "Business Administration", "Psychology", "Engineering", "Biology",
        "Political Science", "Economics", "English Literature", "Art History", "Mathematics", "Physics",
        "Chemistry", "Sociology", "Philosophy", "Journalism", "Architecture", "Law", "Medicine", "Education")

    val universities = listOf(
        School("Harvard University", SchoolType.UNIVERSITY, 55000.0, 0.04, 100),
        School("Stanford University", SchoolType.UNIVERSITY, 52000.0, 0.05, 98),
        School("MIT", SchoolType.UNIVERSITY, 53000.0, 0.06, 97),
        School("Yale University", SchoolType.UNIVERSITY, 51000.0, 0.07, 95),
        School("State University", SchoolType.UNIVERSITY, 18000.0, 0.40, 50),
        School("Community College", SchoolType.COMMUNITY_COLLEGE, 5000.0, 0.80, 20),
        School("Online University", SchoolType.ONLINE, 8000.0, 0.90, 15)
    )

    fun applyToSchool(character: CharacterEntity, school: School, major: String): SchoolResult {
        val gpa = calculateGPA(character)
        val acceptance = (school.acceptanceRate * 100.0 + gpa * 5.0 + character.smarts * 0.2).coerceIn(0.0, 99.0)
        val accepted = Random.nextDouble() * 100 < acceptance

        return if (accepted) {
            val loan = if (character.cash < school.tuition) school.tuition * 0.8 else 0.0
            SchoolResult(true, "Accepted to ${school.name}! Major: $major", 
                character.copy(currentEducationId = school.name + "_" + major, cash = character.cash - school.tuition + loan,
                    studentLoan = loan + character.studentLoan, smarts = (character.smarts + 5.0).coerceIn(0.0, 100.0)))
        } else {
            SchoolResult(false, "Rejected from ${school.name}. Your GPA/scores weren't competitive.", character)
        }
    }

    fun studyYear(character: CharacterEntity): StudyResult {
        if (character.currentEducationId == null) return StudyResult("Not enrolled in any school.", character, emptyList())
        val gpaChange = (character.smarts * 0.05 + character.discipline * 0.03 - character.stress * 0.02 + java.util.Random().nextGaussian() * 0.3).coerceIn(-1.0, 1.0)
        var c = character.copy(gpa = (character.gpa + gpaChange).coerceIn(0.0, 4.0),
            smarts = (character.smarts + 3.0).coerceIn(0.0, 100.0),
            discipline = (character.discipline + 2.0).coerceIn(0.0, 100.0),
            stress = (character.stress + 8.0).coerceIn(0.0, 100.0),
            energy = (character.energy - 5.0).coerceIn(0.0, 100.0))

        val events = mutableListOf<String>()
        if (c.gpa < 2.0) events.add("Academic probation! You need to improve your grades.")
        if (c.gpa > 3.7) events.add("Dean's list! Your academic performance is exceptional.")
        if (Random.nextFloat() < 0.05f) {
            c = c.copy(smarts = (c.smarts + 10.0).coerceIn(0.0, 100.0))
            events.add("You published a research paper! Your intelligence grows.")
        }
        if (c.stress > 80) {
            c = c.copy(health = (c.health - 5.0).coerceIn(0.0, 100.0))
            events.add("The stress of studying is affecting your health.")
        }

        return StudyResult("Year of study complete. GPA: ${"%.2f".format(c.gpa)}", c, events)
    }

    fun graduate(character: CharacterEntity): SchoolResult {
        if (character.currentEducationId == null) return SchoolResult(false, "Not enrolled.", character)
        if (character.gpa < 2.0) return SchoolResult(false, "Failed to graduate. GPA too low.", character)

        val degreeName = character.currentEducationId!!.substringAfter("_")
        val schoolName = character.currentEducationId!!.substringBefore("_")
        val newDegrees = character.degrees + "$degreeName Degree from $schoolName"

        return SchoolResult(true, "Congratulations! You earned your $degreeName degree!",
            character.copy(currentEducationId = null, degrees = newDegrees, gpa = 0.0,
                smarts = (character.smarts + 15.0).coerceIn(0.0, 100.0),
                reputation = (character.reputation + 20).coerceIn(0, 100)))
    }

    private fun calculateGPA(character: CharacterEntity): Double {
        if (character.gpa > 0) return character.gpa
        return (character.smarts * 0.04 + character.discipline * 0.03 + Random.nextDouble() * 1.0).coerceIn(0.0, 4.0)
    }
}

data class SchoolResult(val success: Boolean, val message: String, val character: CharacterEntity)
data class StudyResult(val message: String, val character: CharacterEntity, val events: List<String>)
