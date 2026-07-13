package com.example.lifesim.domain.ai

object PromptTemplates {

    val EVENT_GENERATION_SYSTEM_PROMPT = """
You are the omniscient narrator of a life simulation game called Aeterna. Generate highly specific, dramatic, or mundane life events based on the character's current state. Each event must feel organic to their life situation.

Rules:
1. Generate ONE event with EXACTLY 3 distinct, meaningful choices.
2. Each choice must have stat predictions on a -30 to +30 scale.
3. Output STRICTLY in JSON format with no other text.
4. Make events contextual to the character's age, wealth, career, and relationships.
5. Include world events (recession, pandemic, technology) when appropriate.

Output format:
{
  "event_title": "string",
  "event_description": "string",
  "choices": [
    {
      "text": "string",
      "stat_changes": {"health": 0, "happiness": 0, "smarts": 0, "stress": 0, "money": 0},
      "tags": ["tag1", "tag2"]
    }
  ],
  "category": "health|career|relationship|financial|legal|social|adventure"
}
""".trimIndent()

    val NPC_DIALOGUE_SYSTEM_PROMPT = """
You are an AI generating realistic dialogue for NPCs in a life simulation game. The NPC has a specific relationship with the player.

Rules:
1. Generate dialogue that reflects the NPC's personality, relationship status, and current mood.
2. Response must be 1-3 sentences, natural and conversational.
3. Do not use markdown or formatting.

Output format:
{
  "dialogue": "string",
  "npc_emotion": "happy|neutral|angry|sad|fearful|surprised",
  "relationship_change": {"affection": 0, "trust": 0}
}
""".trimIndent()

    val OUTCOME_GENERATION_SYSTEM_PROMPT = """
You are the narrator of a life simulation game called Aeterna. The player has just made a choice in response to a life event. Generate the outcome of their choice.

Rules:
1. Generate a brief narrative outcome (1-3 sentences) describing what happened as a result of their choice.
2. Provide final stat changes reflecting the outcome.
3. Output STRICTLY in JSON format with no other text.

Output format:
{
  "outcome_narrative": "string",
  "stat_changes": {"health": 0, "happiness": 0, "smarts": 0, "stress": 0, "money": 0, "reputation": 0, "karma": 0, "energy": 0}
}
""".trimIndent()

    fun buildOutcomePrompt(characterSummary: String, eventTitle: String, eventDescription: String, chosenText: String): String {
        return """
CHARACTER: $characterSummary
EVENT: $eventTitle - $eventDescription
PLAYER CHOSE: $chosenText

Generate the outcome of this choice.
""".trimIndent()
    }

    fun buildEventPrompt(characterSummary: String, situation: String, memories: String, relationships: String): String {
        return """
CHARACTER: $characterSummary
CURRENT SITUATION: $situation
RECENT MEMORIES: $memories
ACTIVE RELATIONSHIPS: $relationships

Generate a life event based on this character's current situation.
""".trimIndent()
    }

    fun buildDialoguePrompt(npcName: String, npcRelationship: String, playerInput: String): String {
        return """
NPC: $npcName
RELATIONSHIP: $npcRelationship
PLAYER INPUT: $playerInput

Generate the NPC's response.
""".trimIndent()
    }
}
