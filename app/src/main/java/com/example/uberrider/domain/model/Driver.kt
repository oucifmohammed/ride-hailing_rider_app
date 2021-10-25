package com.example.uberrider.domain.model

data class Driver(
    val key: String,
    val fullName: String,
    val phoneNumber: String,
    var latitude: Double,
    var longitude: Double
)
