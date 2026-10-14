package com.example.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations for [AppDatabase] ("bright_electricity_db").
 *
 * Version 7 is the baseline: it is the schema every installed copy has today. From now on every
 * schema change bumps the version AND adds a [Migration] here that alters the tables in place
 * (ALTER TABLE / CREATE TABLE), so the user's complaint history is never wiped by an update.
 *
 * Versions 1-6 predate this file and their exact schemas were never recorded, so only those may
 * still be rebuilt from scratch (see AppDatabase.getDatabase).
 *
 * How to add one:
 *   val MIGRATION_7_8 = object : Migration(7, 8) {
 *       override fun migrate(db: SupportSQLiteDatabase) {
 *           db.execSQL("ALTER TABLE complaints ADD COLUMN source TEXT NOT NULL DEFAULT ''")
 *       }
 *   }
 * then list it in [ALL] and raise `version` in @Database.
 */
object AppDatabaseMigrations {
    /** Versions with no recorded schema; the only ones Room may still drop and recreate. */
    val LEGACY_DESTRUCTIVE_FROM: IntArray = intArrayOf(1, 2, 3, 4, 5, 6)

    /**
     * 7 -> 8: real data feeds.
     * - grid_telemetry_cache now holds NISO's published daily figures. It only ever held an
     *   invented "live" reading, so it is rebuilt (no user data lives there).
     * - New caches: community_stats_cache, crew_sightings_cache.
     * Complaints and every other table are untouched.
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `grid_telemetry_cache`")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `grid_telemetry_cache` (" +
                    "`id` TEXT NOT NULL, `reportDate` TEXT NOT NULL, `peakGenerationMw` REAL, " +
                    "`peakTime` TEXT NOT NULL, `peakFrequencyHz` REAL, `offPeakGenerationMw` REAL, " +
                    "`offPeakTime` TEXT NOT NULL, `offPeakFrequencyHz` REAL, `energyGeneratedMwh` REAL, " +
                    "`energySentOutMwh` REAL, `source` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `community_stats_cache` (" +
                    "`scope` TEXT NOT NULL, `reportCount` INTEGER NOT NULL, `neighbourCount` INTEGER NOT NULL, " +
                    "`avgRestoreHours` REAL, `avgSupplyHours` REAL, `reportsPerTransformer` REAL, " +
                    "`fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`scope`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `crew_sightings_cache` (" +
                    "`id` TEXT NOT NULL, `transformerId` TEXT NOT NULL, `kind` TEXT NOT NULL, " +
                    "`note` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `reporterKey` TEXT NOT NULL, " +
                    "`isMine` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_7_8)
}
