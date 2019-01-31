package me.camsteffen.polite.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Scanner

class SqliteTestDbOpenHelper(
    private val context: Context,
    targetContext: Context,
    name: String,
    private val version: Int
) :
    SQLiteOpenHelper(targetContext, name, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        val schemaFileName = schemaFileName(version)
        context.assets.open(schemaFileName).use { sqlIn ->
            Scanner(sqlIn).useDelimiter(";\\s*")
                .forEach { db.execSQL(it) }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}

private fun schemaFileName(version: Int): String {
    return "schema_$version.sql"
}
