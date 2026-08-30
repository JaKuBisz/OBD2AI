package com.jakubisz.obd2ai.di

import android.content.Context
import androidx.room.Room
import com.jakubisz.obd2ai.data.ai.OpenAIService
import com.jakubisz.obd2ai.data.local.AppDatabase
import com.jakubisz.obd2ai.data.local.DtcRecordDao
import com.jakubisz.obd2ai.data.local.TripSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOpenAiService(): OpenAIService = OpenAIService()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "obd2ai.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDtcRecordDao(db: AppDatabase): DtcRecordDao = db.dtcRecordDao()

    @Provides
    fun provideTripSessionDao(db: AppDatabase): TripSessionDao = db.tripSessionDao()
}
