package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.util.EMAIL_ADDRESS_PATTERN
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Login @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(email: String, password: String): Result<Boolean>{

        if(email.isEmpty() || password.isEmpty()) {
            return Result.Error(data = null, errorMessage = "You must fill all the fields.")
        }

        if(!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            return Result.Error(data = false, "The format of the email is wrong.")
        }

        return repository.login(email, password)
    }
}