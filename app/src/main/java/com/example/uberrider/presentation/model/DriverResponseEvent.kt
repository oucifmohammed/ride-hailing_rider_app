package com.example.uberrider.presentation.model

data class DriverResponseEvent(
    val driverId: String = "",
    var driverName: String = "",
    var phoneNumber: String = "",
    val response: String,
    var driverLatitude: Double = 0.0,
    var driverLongitude: Double = 0.0,
    var profilePhoto: String = ""
)
