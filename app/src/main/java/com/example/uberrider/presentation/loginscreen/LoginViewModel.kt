package com.example.uberrider.presentation.loginscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uberrider.domain.usecases.Login
import com.example.uberrider.presentation.util.PreferencesManager
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: Login,
    private val preferencesManager: PreferencesManager
): ViewModel(){

    private val _loginResult = MutableLiveData<Result<Boolean>>()
    val loginResult: LiveData<Result<Boolean>> = _loginResult

    fun login(email: String, password: String)  = viewModelScope.launch {

        _loginResult.value = Result.Loading()

        val result = login.invoke(email, password)

        _loginResult.value = result
    }

    suspend fun validateRegisterStatus() {
        preferencesManager.validateUserRegisterStatus()
    }
    suspend fun getRegisterStatus(): Boolean {
        return preferencesManager.userRegisterStatus.first()
    }
}