package com.example.data.solutions

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/**
 * Separate database for the 30 solutions so adding tables here never triggers the main
 * database's destructive migration (which would wipe the user's profile and complaints).
 */
@Database(
    entities = [
        FaultReportEntity::class,
        SupplyEventEntity::class,
        RefundClaimEntity::class,
        VoiceNoteEntity::class,
        ForumPostEntity::class,
        WhistleblowEntity::class,
        MeterWaitlistEntity::class,
        InventoryRequestEntity::class,
        TokenQueueEntity::class,
        TransformerFaultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SolutionsDatabase : RoomDatabase() {
    abstract fun dao(): SolutionsDao

    companion object {
        private const val NAME = "bright_solutions_db"

        @Volatile
        private var instance: SolutionsDatabase? = null

        fun get(context: Context): SolutionsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SolutionsDatabase::class.java,
                    NAME
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }

        /** Used by "Delete account": clears every table and the voice/photo evidence files. */
        fun wipe(context: Context) {
            get(context).clearAllTables()
            listOf("voice_notes", "evidence").forEach { dir ->
                File(context.filesDir, dir).deleteRecursively()
            }
            context.getSharedPreferences(SolutionsPrefs.FILE, Context.MODE_PRIVATE).edit().clear().apply()
        }
    }
}
