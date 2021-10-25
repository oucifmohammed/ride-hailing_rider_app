package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListenToDriverDisconnection @Inject constructor(
    private val repository: RiderRepository
){

    fun invoke(): Flow<String> {
        return repository.listenToDriverDisconnection()
    }
}