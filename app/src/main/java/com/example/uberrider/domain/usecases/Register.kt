package com.example.uberrider.domain.usecases

import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.util.EMAIL_ADDRESS_PATTERN
import com.example.uberrider.util.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Register @Inject constructor(
    private val repository: RiderRepository
){

    suspend fun invoke(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Result<Boolean> {
        if (fullName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()
            || confirmPassword.isEmpty()) {
            return Result.Error(data = false, errorMessage = "Please fill all the fields.")
        }

        if (password != confirmPassword) {
            return Result.Error(
                data = false,
                errorMessage = "the values in password and confirm password must be equal"
            )
        }

        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            return Result.Error(data = false, "The format of the email is wrong.")
        }

        return repository.register(fullName, email, phoneNumber, password)
    }
}