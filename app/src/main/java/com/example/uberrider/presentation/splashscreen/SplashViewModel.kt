package com.example.uberrider.presentation.splashscreen

import androidx.lifecycle.ViewModel
import com.example.uberrider.domain.usecases.GetUserConnectionStatus
import com.example.uberrider.domain.usecases.UpdateTokenActivity
import com.example.uberrider.presentation.util.PreferencesManager
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserConnectionStatus: GetUserConnectionStatus,
    private val preferencesManager: PreferencesManager,
): ViewModel(){

    suspend fun getUserConnectionStatus(): Result<Boolean> {
        return getUserConnectionStatus.invoke()
    }

    suspend fun getRegisterStatus(): Boolean {
        return preferencesManager.userRegisterStatus.first()
    }
}