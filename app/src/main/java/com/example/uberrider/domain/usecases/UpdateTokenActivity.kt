package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateTokenActivity @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke() {
        repository.updateTokenActivity()
    }
}