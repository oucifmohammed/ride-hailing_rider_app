package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateTokenService @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(token: String) {
        repository.updateTokenService(token)
    }
}