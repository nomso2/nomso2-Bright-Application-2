package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ComplaintEntity::class,
        UserProfileEntity::class,
        VandalismEntity::class,
        BillingDisputeEntity::class,
        ApplianceClaimEntity::class,
        StreetHazardEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun complaintDao(): ComplaintDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun vandalismDao(): VandalismDao
    abstract fun billingDisputeDao(): BillingDisputeDao
    abstract fun applianceClaimDao(): ApplianceClaimDao
    abstract fun streetHazardDao(): StreetHazardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bright_electricity_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
