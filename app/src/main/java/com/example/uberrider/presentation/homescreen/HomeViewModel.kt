package com.example.uberrider.presentation.homescreen

import androidx.lifecycle.*
import com.example.uberrider.domain.model.Driver
import com.example.uberrider.domain.usecases.*
import com.example.uberrider.presentation.util.Event
import com.example.uberrider.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectRider: ConnectRider,
    private val disconnectRider: DisconnectRider,
    private val updateToken: UpdateTokenActivity,
    private val queryDrivers: QueryDrivers,
    private val getAvailableDrivers: GetAvailableDrivers,
    private val listenToDriverDisconnection: ListenToDriverDisconnection,
    private val searchForDrivers: SearchForDrivers,
    private val addToDeclineDrivers: AddToDeclineDrivers
) : ViewModel() {

    private val _connectionState = MutableLiveData<Event<Result<Boolean>>>()
    val connectionState: LiveData<Event<Result<Boolean>>> = _connectionState

    private val _availableDrivers = MutableLiveData<HashMap<String, Driver>>()
    val availableDrivers: LiveData<HashMap<String, Driver>> = _availableDrivers

    private val _searchForDriversResult = MutableLiveData<Result<Driver>>()
    val searchForDriversResult: LiveData<Result<Driver>> = _searchForDriversResult

    fun connectRider(latitude: Double, longitude: Double) = viewModelScope.launch {
        connectRider.invoke(latitude, longitude).collect {
            _connectionState.value = Event(it)
        }
    }

    fun queryDrivers(city: String, latitude: Double, longitude: Double, loadDrivers: () -> Unit) {
        queryDrivers.invoke(city, latitude, longitude, loadDrivers)
    }

    fun getAvailableDrivers() = viewModelScope.launch {
        getAvailableDrivers.invoke().collect {
            _availableDrivers.value = it
        }
    }

    fun getDisconnectDriver() = liveData {
        listenToDriverDisconnection.invoke().collect {
            emit(it)
        }
    }

    fun disconnectRider() {
        disconnectRider.invoke()
    }

    suspend fun updateToken() {
        updateToken.invoke()
    }

    fun searchForDrivers(
        latitude: Double,
        longitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ) = viewModelScope.launch {
        val result = searchForDrivers.invoke(latitude, longitude, destinationLatitude, destinationLongitude)

        _searchForDriversResult.value = result
    }

    fun addToDeclineDrivers(driverId: String) {
        addToDeclineDrivers.invoke(driverId)
    }
}