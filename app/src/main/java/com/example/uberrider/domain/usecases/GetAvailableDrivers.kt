package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.model.Driver
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

@Singleton
class GetAvailableDrivers @Inject constructor(
    val repository: RiderRepository
){

    fun invoke(): Flow<HashMap<String, Driver>> {
        return repository.getAvailableDrivers()
    }
}