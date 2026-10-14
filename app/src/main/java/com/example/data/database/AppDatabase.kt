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
        StreetHazardEntity::class,
        OutageGridNodeEntity::class,
        MaintenanceAlertEntity::class,
        GridTelemetryEntity::class,
        OfflineSyncQueueEntity::class,
        CommunityStatsEntity::class,
        CrewSightingEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun complaintDao(): ComplaintDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun vandalismDao(): VandalismDao
    abstract fun billingDisputeDao(): BillingDisputeDao
    abstract fun applianceClaimDao(): ApplianceClaimDao
    abstract fun streetHazardDao(): StreetHazardDao
    abstract fun outageGridNodeDao(): OutageGridNodeDao
    abstract fun maintenanceAlertDao(): MaintenanceAlertDao
    abstract fun gridTelemetryDao(): GridTelemetryDao
    abstract fun offlineSyncQueueDao(): OfflineSyncQueueDao
    abstract fun communityStatsDao(): CommunityStatsDao
    abstract fun crewSightingDao(): CrewSightingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bright_electricity_db"
                )
                    // Real migrations only: an app update must never wipe complaint history.
                    .addMigrations(*AppDatabaseMigrations.ALL)
                    // Only pre-baseline versions (1-6, schemas never recorded) may be rebuilt.
                    .fallbackToDestructiveMigrationFrom(true, *AppDatabaseMigrations.LEGACY_DESTRUCTIVE_FROM)
                    // Installing an older APK over a newer one cannot be migrated; rebuild
                    // rather than crash (history comes back from the cloud backup when enabled).
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
