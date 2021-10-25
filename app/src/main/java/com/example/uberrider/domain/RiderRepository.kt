package com.example.uberrider.domain

import com.example.uberrider.domain.model.Driver
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

interface RiderRepository {

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<Boolean>

    suspend fun login(email: String, password: String): Result<Boolean>

    suspend fun getUserConnectionStatus(): Result<Boolean>

    suspend fun resetPassword(email: String): Result<Boolean>

    fun connectRider(latitude: Double, longitude: Double): Flow<Result<Boolean>>

    fun disconnectRider()

    suspend fun getRiderProfile(): Result<Rider>

    suspend fun editProfile(
        currentEmail: String,
        password: String,
        updatedEmail: String,
        phoneNumber: String,
        uri: String?
    ): Result<Boolean>

    fun logout()

    suspend fun updateTokenService(token: String)

    suspend fun updateTokenActivity()

    fun queryDrivers(cityName: String, latitude: Double, longitude: Double, loadDrivers: () -> Unit)

    fun getAvailableDrivers(): Flow<HashMap<String, Driver>>

    fun checkDriverConnection(key: String, city: String)

    fun listenToDriverDisconnection(): Flow<String>

    suspend fun searchForDrivers(
        latitude: Double,
        longitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ): Result<Driver>

    fun addToDeclineDrivers(driverId: String)
}