package com.example.uberrider.domain.util

interface Mapper<T, DomainModel> {

    fun mapToDomainModel(model: T): DomainModel

    fun mapFromDomainModel(model: DomainModel): T
}