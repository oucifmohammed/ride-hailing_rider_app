package com.example.uberrider.data.util

import com.example.uberrider.data.model.RiderDto
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.domain.util.Mapper

class RiderMapper: Mapper<RiderDto, Rider> {

    override fun mapToDomainModel(model: RiderDto): Rider {
        return Rider(
            fullName = model.fullName,
            email = model.email,
            phoneNumber = model.phoneNumber,
            profilePhoto = model.profilePhoto
        )
    }

    override fun mapFromDomainModel(model: Rider): RiderDto {
        return RiderDto(
            fullName = model.fullName,
            email = model.email,
            phoneNumber = model.phoneNumber,
            profilePhoto = model.profilePhoto
        )
    }
}