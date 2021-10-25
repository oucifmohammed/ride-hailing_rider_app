package com.example.uberrider.data.util

import androidx.compose.runtime.key
import com.example.uberrider.data.model.DriverDto
import com.example.uberrider.domain.model.Driver
import com.example.uberrider.domain.util.Mapper

class DriverMapper: Mapper<DriverDto, Driver>{

    override fun mapToDomainModel(model: DriverDto): Driver {
        return Driver(
            key = model.key,
            fullName = model.fullName,
            phoneNumber = model.phoneNumber,
            latitude = model.latitude,
            longitude = model.longitude
        )
    }

    override fun mapFromDomainModel(model: Driver): DriverDto {
        return DriverDto(
            key = model.key,
            fullName = model.fullName,
            phoneNumber = model.phoneNumber,
            latitude = model.latitude,
            longitude = model.longitude
        )
    }
}