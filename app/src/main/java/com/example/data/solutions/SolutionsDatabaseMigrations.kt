package com.example.data.solutions

import androidx.room.migration.Migration

/**
 * Schema migrations for [SolutionsDatabase] ("bright_solutions_db").
 *
 * Version 1 is the baseline schema. Every future change bumps the version and adds a
 * [Migration] here that alters tables in place, so the supply log, fault reports and claims are
 * never lost on update. There is deliberately no destructive fallback.
 */
object SolutionsDatabaseMigrations {
    val ALL: Array<Migration> = arrayOf()
}
