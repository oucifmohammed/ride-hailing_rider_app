package com.example.uberrider.presentation.resetpasswordscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uberrider.domain.usecases.ResetPassword
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPassword: ResetPassword
): ViewModel(){

    private val _resetPasswordResult = MutableLiveData<Result<Boolean>>()
    val resetPasswordResult: LiveData<Result<Boolean>> = _resetPasswordResult

    fun resetPassword(email: String) = viewModelScope.launch {
        _resetPasswordResult.value = Result.Loading()

        val result = resetPassword.invoke(email)

        _resetPasswordResult.value = result
    }
}