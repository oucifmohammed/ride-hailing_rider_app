package com.example.uberrider.presentation.profilescreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.domain.usecases.EditProfile
import com.example.uberrider.domain.usecases.GetRiderProfile
import com.example.uberrider.domain.usecases.SignOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.uberrider.util.Result;

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getRiderProfile: GetRiderProfile,
    private val editProfile: EditProfile,
    private val signOut: SignOut
): ViewModel(){

    private val _profileData = MutableLiveData<Result<Rider>>()
    val profileData: LiveData<Result<Rider>> = _profileData

    private val _editProfileResult = MutableLiveData<Result<Boolean>>()
    val editProfileResult: LiveData<Result<Boolean>> = _editProfileResult

    fun getRiderProfile() = viewModelScope.launch {

        _profileData.value = Result.Loading()

        val result = getRiderProfile.invoke()
        _profileData.value = result
    }

    fun editProfile(currentEmail: String, password: String, updatedEmail: String, phoneNumber: String, uri: String?) = viewModelScope.launch {

        _editProfileResult.value = Result.Loading()
        val result = editProfile.invoke(currentEmail, password, updatedEmail, phoneNumber, uri)

        _editProfileResult.value = result
    }

    fun signOut() {
        signOut.invoke()
    }
}