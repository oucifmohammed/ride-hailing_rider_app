package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.util.EMAIL_ADDRESS_PATTERN
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResetPassword @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(email: String): Result<Boolean> {

        if(email.isEmpty()) {
            return Result.Error(data = null, errorMessage = "Please fill the email field.")
        }

        if(!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            return Result.Error(data = null, "The email format is wrong.")
        }

        return repository.resetPassword(email)
    }
}