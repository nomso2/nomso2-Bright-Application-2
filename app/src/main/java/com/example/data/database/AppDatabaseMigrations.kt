package com.example.data.database

import androidx.room.migration.Migration

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

    val ALL: Array<Migration> = arrayOf()
}
