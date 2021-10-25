package com.example.uberrider.data.model

import com.example.uberrider.data.util.Constants.DEFAULT_USER_IMAGE

data class RiderDto(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePhoto: String = DEFAULT_USER_IMAGE
)