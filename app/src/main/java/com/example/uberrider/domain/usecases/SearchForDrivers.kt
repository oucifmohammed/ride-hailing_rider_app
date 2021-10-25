package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.model.Driver
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchForDrivers @Inject constructor(
    private val repository: RiderRepository
) {

    suspend fun invoke(
        latitude: Double,
        longitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ): Result<Driver> {
        return repository.searchForDrivers(latitude, longitude, destinationLatitude, destinationLongitude)
    }
}