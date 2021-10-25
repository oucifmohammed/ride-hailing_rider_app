package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueryDrivers @Inject constructor(
    private val repository: RiderRepository
){

    fun invoke(city: String, latitude: Double, longitude: Double, loadDrivers: () -> Unit) {
        repository.queryDrivers(city, latitude, longitude, loadDrivers)
    }
}