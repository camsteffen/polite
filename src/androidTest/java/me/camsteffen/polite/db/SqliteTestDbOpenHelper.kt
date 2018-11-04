/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.camsteffen.polite.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Scanner

class SqliteTestDbOpenHelper(
    private val context: Context,
    private val targetContext: Context,
    name: String,
    private val version: Int
) :
    SQLiteOpenHelper(targetContext, name, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        val schemaFileName = schemaFileName(version)
        val sqlIn = context.assets.open(schemaFileName)
        Scanner(sqlIn).useDelimiter(";\\s*").use { scanner ->
            scanner.forEach(db::execSQL)
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
