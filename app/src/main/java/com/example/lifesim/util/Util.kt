package com.example.lifesim.util

import kotlin.random.Random

fun generateId(): String = java.util.UUID.randomUUID().toString()

fun generateRandomName(): Pair<String, String> {
    val firstNames = listOf("James","Mary","John","Patricia","Robert","Jennifer","Michael","Linda",
        "William","Elizabeth","David","Barbara","Richard","Susan","Joseph","Jessica","Thomas","Sarah",
        "Charles","Karen","Christopher","Lisa","Daniel","Nancy","Matthew","Betty","Anthony","Margaret",
        "Mark","Sandra","Donald","Ashley","Steven","Dorothy","Andrew","Kimberly","Paul","Emily")
    val lastNames = listOf("Smith","Johnson","Williams","Brown","Jones","Garcia","Miller","Davis",
        "Rodriguez","Martinez","Hernandez","Lopez","Gonzalez","Wilson","Anderson","Thomas","Taylor",
        "Moore","Jackson","Martin","Lee","Perez","Thompson","White","Harris","Sanchez","Clark")
    return firstNames.random() to lastNames.random()
}

fun <T> List<Pair<T, Float>>.weightedRandomSelect(): T {
    val total = sumOf { it.second.toDouble() }
    if (total <= 0.0) return first().first
    var r = Random.nextDouble() * total
    for ((item, weight) in this) { r -= weight; if (r <= 0.0) return item }
    return last().first
}
