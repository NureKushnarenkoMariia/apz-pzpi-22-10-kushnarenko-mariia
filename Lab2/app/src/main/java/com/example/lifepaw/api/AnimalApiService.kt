package com.example.lifepaw.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AnimalApiService {
    @GET("api/Animal/{id}")
    suspend fun getAnimalById(@Path("id") id: Int): Response<Animal>

    @GET("api/Animal")
    suspend fun getAnimals(): Response<List<Animal>>

    @GET("api/Sensors/animal/{id}")
    suspend fun getSensorForAnimal(@Path("id") id: Int): Response<List<SensorData>>

    @GET("/api/Notifications/animal/{id}")
    suspend fun getNotificationForAnimal(@Path("id") id: Int): Response<List<NotificationData>>

    @GET("/api/Reports/shelter/{id}")
    suspend fun getMedicalReports(@Path("id") id: Int): Response<List<ReportData>>
}
