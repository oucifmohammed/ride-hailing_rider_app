package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRiderProfile @Inject constructor(
    val repository: RiderRepository
){

    suspend fun invoke(): Result<Rider> {
        return repository.getRiderProfile()
    }
}