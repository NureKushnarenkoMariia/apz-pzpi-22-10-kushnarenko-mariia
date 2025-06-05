package com.example.lifepaw.api

import com.google.gson.annotations.SerializedName

data class Shelter(
    val id: Int,
    val name: String,
    val location: String,
    val contacts: String
)

data class Animal(
    val id: Int,
    val name: String,
    val species: String,
    val breed: String,
    val age: Int,
    val shelter: Shelter,
    val shelterId: Int
){
    override fun toString(): String {
        return this.name
    }
}

data class SensorData(
    val id: Int,
    val temperature: Double,
    val humidity: Double,
    val timestamp: String,
    val installationDate: String,
    val animal: Animal,
    val animalId: Int
)

data class SensorUIData(
    val type: String,
    val status: String,
    val installationDate: String,
    val linkedAnimalInfo: String,
    val temperature: String,
    val humidity: String
)

data class NotificationUser(
    val id: Int,
    val name: String,
    val role: Int,
    val email: String,
    val password: String
)

data class NotificationData(
    val id: Int,
    val type: String,
    val message: String,
    val dateTime: String,
    val user: NotificationUser,
    val userId: Int,
    val animal: Animal,
    val animalId: Int
)

data class ReportData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("report_Type")
    val reportType: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("creationPeriod")
    val creationPeriod: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("file_path")
    val filePath: String,

    @SerializedName("shelter")
    val shelter: Shelter,

    @SerializedName("shelterId")
    val shelterId: Int
)