package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditProfile @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(
        currentEmail: String,
        password: String,
        updatedEmail: String,
        phoneNumber: String,
        uri: String?
    ): Result<Boolean> {
        return repository.editProfile(currentEmail, password, updatedEmail, phoneNumber, uri)
    }
}