package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisconnectRider @Inject constructor(
    private val repository: RiderRepository
){

    fun invoke() {
        repository.disconnectRider()
    }
}