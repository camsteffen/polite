package me.camsteffen.polite.db

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Scanner

val MIGRATION_5_6 = EmptyMigration(5, 6)

fun allMigrations(context: Context): Array<Migration> {
    return arrayOf(
        AssetMigration(context, 1, 2),
        AssetMigration(context, 2, 3),
        AssetMigration(context, 3, 4),
        AssetMigration(context, 4, 5),
        MIGRATION_5_6
    )
}

fun migrationAssetName(startVersion: Int, endVersion: Int): String {
    return "migration/migration_${startVersion}_$endVersion.sql"
}

class AssetMigration(val context: Context, startVersion: Int, endVersion: Int) :
    Migration(startVersion, endVersion) {

    override fun migrate(database: SupportSQLiteDatabase) {
        val name = migrationAssetName(startVersion, endVersion)
        val sqlIn = context.assets.open(name)
        Scanner(sqlIn).useDelimiter(";\\s*").use { scanner ->
            scanner.forEach { database.execSQL(it) }
        }
    }
}

class EmptyMigration(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // do nothing
    }
}
