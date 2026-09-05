package com.evostackr.smscampaignmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.evostackr.smscampaignmanager.data.local.dao.*
import com.evostackr.smscampaignmanager.data.local.entity.*
import com.evostackr.smscampaignmanager.util.SecurityUtil
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        CampaignEntity::class,
        RecipientEntity::class,
        SmsLogEntity::class,
        TemplateEntity::class,
        SettingsEntity::class,
        ContactEntity::class,
        ContactGroupEntity::class,
        ScheduledSmsEntity::class,
        ImportedContactEntity::class
    ],
    version = 17,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao
    abstract fun recipientDao(): RecipientDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun templateDao(): TemplateDao
    abstract fun settingsDao(): SettingsDao
    abstract fun contactDao(): ContactDao
    abstract fun scheduledSmsDao(): ScheduledSmsDao
    abstract fun importedContactDao(): ImportedContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    val appContext = context.applicationContext
                    net.sqlcipher.database.SQLiteDatabase.loadLibs(appContext)
                    val passphrase = SecurityUtil.getOrCreatePassphrase(appContext)
                    val factory = SupportFactory(passphrase)
                    Room.databaseBuilder(
                        appContext,
                        AppDatabase::class.java,
                        "sms_campaign_manager.db"
                    )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "sms_campaign_manager_std.db"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                }
                INSTANCE = instance
                instance
            }
        }
    }
}

