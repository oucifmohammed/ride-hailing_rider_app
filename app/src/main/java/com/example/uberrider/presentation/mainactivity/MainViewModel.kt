package com.example.uberrider.presentation.mainactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.domain.usecases.DisconnectRider
import com.example.uberrider.domain.usecases.GetRiderProfile
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRiderProfile: GetRiderProfile,
): ViewModel(){

    private val _navHeaderAccount = MutableLiveData<Result<Rider>>()
    val navHeaderAccount: LiveData<Result<Rider>> = _navHeaderAccount

    fun showNavHeaderData() = viewModelScope.launch {

        _navHeaderAccount.value = Result.Loading()
        val result = getRiderProfile.invoke()

        _navHeaderAccount.value = result
    }
}