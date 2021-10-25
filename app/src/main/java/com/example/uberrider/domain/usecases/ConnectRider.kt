package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectRider @Inject constructor(
    private val repository: RiderRepository
){
    fun invoke(latitude: Double, longitude: Double): Flow<Result<Boolean>> {
        return repository.connectRider(latitude,longitude)
    }
}