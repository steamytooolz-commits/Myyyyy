package com.example.lifesim.data.local

import androidx.room.TypeConverter
import com.example.lifesim.data.local.entity.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromGender(value: Gender): String = value.name
    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)

    @TypeConverter
    fun fromSexualOrientation(value: SexualOrientation): String = value.name
    @TypeConverter
    fun toSexualOrientation(value: String): SexualOrientation = SexualOrientation.valueOf(value)

    @TypeConverter
    fun fromZodiacSign(value: ZodiacSign): String = value.name
    @TypeConverter
    fun toZodiacSign(value: String): ZodiacSign = ZodiacSign.valueOf(value)

    @TypeConverter
    fun fromMemoryEventType(value: MemoryEventType): String = value.name
    @TypeConverter
    fun toMemoryEventType(value: String): MemoryEventType = MemoryEventType.valueOf(value)

    @TypeConverter
    fun fromRelationType(value: RelationType): String = value.name
    @TypeConverter
    fun toRelationType(value: String): RelationType = RelationType.valueOf(value)

    @TypeConverter
    fun fromRelationshipStatus(value: RelationshipStatus): String = value.name
    @TypeConverter
    fun toRelationshipStatus(value: String): RelationshipStatus = RelationshipStatus.valueOf(value)

    @TypeConverter
    fun fromAssetType(value: AssetType): String = value.name
    @TypeConverter
    fun toAssetType(value: String): AssetType = AssetType.valueOf(value)

    @TypeConverter
    fun fromCrimeType(value: CrimeType): String = value.name
    @TypeConverter
    fun toCrimeType(value: String): CrimeType = CrimeType.valueOf(value)

    @TypeConverter
    fun fromOfficePoliticsAlignment(value: OfficePoliticsAlignment): String = value.name
    @TypeConverter
    fun toOfficePoliticsAlignment(value: String): OfficePoliticsAlignment = OfficePoliticsAlignment.valueOf(value)
}
