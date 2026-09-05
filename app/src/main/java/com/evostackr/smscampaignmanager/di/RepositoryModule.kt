package com.evostackr.smscampaignmanager.di

import com.evostackr.smscampaignmanager.data.local.dao.*
import com.evostackr.smscampaignmanager.data.repository.*
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
    fun provideCampaignRepository(
        campaignDao: CampaignDao,
        recipientDao: RecipientDao
    ): CampaignRepository {
        return CampaignRepository(campaignDao, recipientDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: SettingsDao
    ): SettingsRepository {
        return SettingsRepository(settingsDao)
    }

    @Provides
    @Singleton
    fun provideSmsRepository(
        smsLogDao: SmsLogDao
    ): SmsRepository {
        return SmsRepository(smsLogDao)
    }

    @Provides
    @Singleton
    fun provideTemplateRepository(
        templateDao: TemplateDao
    ): TemplateRepository {
        return TemplateRepository(templateDao)
    }

    @Provides
    @Singleton
    fun provideContactRepository(
        contactDao: ContactDao
    ): ContactRepository {
        return ContactRepository(contactDao)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        scheduledSmsDao: ScheduledSmsDao,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): ScheduleRepository {
        return ScheduleRepository(scheduledSmsDao, context)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        settingsDao: SettingsDao,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): BackupRepository {
        return BackupRepository(settingsDao, context)
    }
}

