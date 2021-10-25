package com.example.uberrider.di

import com.example.uberrider.data.util.DriverMapper
import com.example.uberrider.data.util.RiderMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideRiderMapper(): RiderMapper = RiderMapper()

    @Singleton
    @Provides
    fun provideDriverMapper(): DriverMapper = DriverMapper()
}