package me.camsteffen.polite.db

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.camsteffen.polite.util.SqlStatementReader
import java.io.BufferedReader
import java.io.InputStreamReader

val MIGRATION_5_6 = EmptyMigration(5, 6)

fun allMigrations(context: Context): Array<Migration> {
    return arrayOf(
        AssetMigration(context, 1, 2),
        AssetMigration(context, 2, 3),
        AssetMigration(context, 3, 4),
        AssetMigration(context, 4, 5),
        MIGRATION_5_6,
        AssetMigration(context, 6, 7),
        AssetMigration(context, 7, 8),
        AssetMigration(context, 8, 9)
    )
}

fun migrationAssetName(startVersion: Int, endVersion: Int): String {
    return "migration/migration_${startVersion}_$endVersion.sql"
}

class AssetMigration(val context: Context, startVersion: Int, endVersion: Int) :
    Migration(startVersion, endVersion) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val name = migrationAssetName(startVersion, endVersion)
        context.assets.open(name).use { sqlIn ->
            SqlStatementReader(BufferedReader(InputStreamReader(sqlIn))).forEach(database::execSQL)
        }
    }
}

class EmptyMigration(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // do nothing
    }
}
