package com.example.lifepaw.ui.sensor

data class SensorInfo(
    val type: String,
    val status: String,
    val installationDate: String,
    val linkedAnimalName: String,
    val linkedAnimalCage: Int,
    val temperature: Double,
    val humidity: Double
)