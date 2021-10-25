package com.example.uberrider.di

import com.example.uberrider.data.NotificationApi
import com.example.uberrider.data.RiderRepositoryImpl
import com.example.uberrider.data.util.DriverMapper
import com.example.uberrider.data.util.RiderMapper
import com.example.uberrider.domain.RiderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providesRiderRepository(riderMapper: RiderMapper, driverMapper: DriverMapper, api: NotificationApi): RiderRepository = RiderRepositoryImpl(riderMapper, driverMapper, api)
}