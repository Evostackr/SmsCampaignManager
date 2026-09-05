package com.evostackr.smscampaignmanager.di

import android.content.Context
import com.evostackr.smscampaignmanager.data.local.AppDatabase
import com.evostackr.smscampaignmanager.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideCampaignDao(database: AppDatabase): CampaignDao = database.campaignDao()

    @Provides
    fun provideRecipientDao(database: AppDatabase): RecipientDao = database.recipientDao()

    @Provides
    fun provideSmsLogDao(database: AppDatabase): SmsLogDao = database.smsLogDao()

    @Provides
    fun provideTemplateDao(database: AppDatabase): TemplateDao = database.templateDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideScheduledSmsDao(database: AppDatabase): ScheduledSmsDao = database.scheduledSmsDao()

    @Provides
    fun provideImportedContactDao(database: AppDatabase): ImportedContactDao = database.importedContactDao()
}

