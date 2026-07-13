package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(
    tableName = "crime_records",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["characterId"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId")]
)
@TypeConverters(Converters::class)
data class CrimeRecordEntity(
    @PrimaryKey val recordId: String = java.util.UUID.randomUUID().toString(),
    val characterId: String,
    val crimeType: CrimeType,
    val severity: Int, // 1-10
    val yearCommitted: Int,
    val tickCommitted: Long,
    val bailAmount: Double = 0.0,
    val isConvicted: Boolean = false,
    val prisonSentenceYears: Int = 0,
    val yearsServed: Int = 0,
    val paroleEligibilityTick: Long? = null,
    val isParoled: Boolean = false,
    val finesPaid: Double = 0.0,
    val isExpunged: Boolean = false,
    val victimCharacterId: String? = null,
    val locationId: String? = null,
    val witnessIds: List<String> = emptyList(),
    val accompliceIds: List<String> = emptyList(),
    val evidenceCollected: List<String> = emptyList(),
    val isColdCase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CrimeType {
    MURDER, MANSLAUGHTER, ASSAULT, THEFT, BURGLARY, ROBBERY,
    FRAUD, TAX_EVASION, DRUG_POSSESSION, DRUG_TRAFFICKING,
    HACKING, IDENTITY_THEFT, VANDALISM, TRESPASSING, DUI,
    BRIBERY, PERJURY, KIDNAPPING, ARSON, EXTORTION,
    MONEY_LAUNDERING, INSIDER_TRADING, CYBER_CRIME, WAR_CRIMES,
    PUBLIC_INDECENCY, DISORDERLY_CONDUCT, CONTEMPT, PAROLE_VIOLATION
}
