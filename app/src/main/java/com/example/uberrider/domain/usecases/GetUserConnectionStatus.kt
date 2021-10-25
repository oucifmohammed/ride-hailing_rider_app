package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUserConnectionStatus @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(): Result<Boolean> {
        return repository.getUserConnectionStatus()
    }
}