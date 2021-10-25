package com.example.uberrider.presentation.registerscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uberrider.domain.usecases.Register
import com.example.uberrider.presentation.util.PreferencesManager
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val register: Register,
    private val preferencesManager: PreferencesManager
): ViewModel() {

    private val _registerResult = MutableLiveData<Result<Boolean>>()
    val registerResult: LiveData<Result<Boolean>> = _registerResult

    fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) = viewModelScope.launch {
        _registerResult.value = Result.Loading()

        val result = register.invoke(fullName, email, phoneNumber, password, confirmPassword)

        _registerResult.value = result
    }

    suspend fun validateRegisterStatus() {
        preferencesManager.validateUserRegisterStatus()
    }
    suspend fun getRegisterStatus(): Boolean {
        return preferencesManager.userRegisterStatus.first()
    }
}