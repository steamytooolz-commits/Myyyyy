package com.example.lifesim.util

object Constants {
    // Game Configuration
    const val STARTING_MONEY = 1000.0
    const val STARTING_AGE = 0
    const val MAX_AGE = 120
    const val YEARS_PER_TURN = 1
    
    // Stat Bounds
    const val MIN_STAT = 0
    const val MAX_STAT = 100
    const val CRITICAL_STAT_THRESHOLD = 20
    const val DANGER_STAT_THRESHOLD = 10
    
    // Event Generation
    const val AI_EVENT_PROBABILITY = 0.3f
    const val PROCEDURAL_EVENT_PROBABILITY = 0.7f
    const val MAX_ACTIVE_MEMORIES = 50
    const val MEMORY_DECAY_THRESHOLD = 10
    
    // Relationship Constants
    const val MAX_RELATIONSHIPS = 50
    const val CLOSE_RELATIONSHIP_THRESHOLD = 70
    const val CONFLICT_RELATIONSHIP_THRESHOLD = 60
    
    // Career Constants
    const val BASE_SALARY = 30000.0
    const val PROMOTION_PERFORMANCE_THRESHOLD = 85
    const val DEMOTION_PERFORMANCE_THRESHOLD = 30
    const val BURNOUT_STRESS_THRESHOLD = 80
    
    // Dynasty Constants
    const val MIN_DYNASTY_WEALTH_FOR_SUCCESS = 100000.0
    const val DYNASTY_REPUTATION_DECAY_RATE = 0.05f
    const val INHERITANCE_TAX_RATE = 0.15
    
    // Health & Death
    const val DEATH_HEALTH_THRESHOLD = 0
    const val HEALTH_RECOVERY_RATE = 0.1f
    const val STRESS_DAMAGE_RATE = 0.5f
    
    // Activity Costs
    const val GYM_COST = 50.0
    const val MOVIE_COST = 20.0
    const val DINNER_COST = 80.0
    const val VACATION_COST = 2000.0
    const val EDUCATION_COST = 10000.0
    
    // AI Configuration
    const val LLM_API_BASE_URL = "https://api.openai.com/v1/"
    const val LLM_MODEL = "gpt-3.5-turbo"
    const val MAX_TOKENS = 500
    const val AI_TIMEOUT_SECONDS = 30L
    
    // UI Constants
    const val ANIMATION_DURATION = 300
    const val STAT_BAR_HEIGHT = 12
    const val CARD_CORNER_RADIUS = 16
    
    // Tags for Memory System
    val TRAUMA_TAGS = listOf("betrayal", "death", "accident", "abuse", "loss", "failure", "humiliation")
    val TRIUMPH_TAGS = listOf("success", "love", "achievement", "discovery", "promotion", "victory", "wealth")
    val RELATIONSHIP_TAGS = listOf("love", "friendship", "conflict", "betrayal", "support", "rivalry")
    val CAREER_TAGS = listOf("promotion", "fired", "success", "failure", "rivalry", "mentor", "achievement")
    
    // Event Categories
    val EVENT_CATEGORIES = listOf(
        "childhood", "education", "career", "relationship", "family",
        "health", "financial", "social", "legal", "travel", "hobby"
    )
    
    // Life Stage Age Ranges
    val LIFE_STAGE_AGES = mapOf(
        "child" to (0..12),
        "teenager" to (13..17),
        "young_adult" to (18..25),
        "adult" to (26..45),
        "middle_aged" to (46..60),
        "senior" to (61..75),
        "elderly" to (76..120)
    )
}