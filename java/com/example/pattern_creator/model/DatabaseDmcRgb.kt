package com.example.pattern_creator.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import androidx.annotation.ColorInt
import com.example.pattern_creator.DataBaseHelper
import com.example.pattern_creator.R

class DatabaseDmcRgb(val context: Context) {
    private val databaseHelper = DataBaseHelper(context)
    val database: SQLiteDatabase = databaseHelper.readableDatabase

    fun checkForDmcId(id: String): Colour? {
        //Log.d("DatabaseDmcRgb", database.)

        val projection = arrayOf(
            DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_DMC_ID,
            DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_NAME,
            DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_RGB
        )

        //filter results where "DMCid" = id
        val selection = "${DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_DMC_ID} = ?"
        val selectionArgs = arrayOf(id)
        //sorting of results
        val sortOrder = "${DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_NAME} DESC"

        val cursor = database.query(
            DatabaseReaderContract.DatabaseEntry.TABLE_NAME, //table to query
            projection, //array of columns being returned
            selection, //columns for WHERE
            selectionArgs, //values for WHERE ( selection WHERE selectionArgs )
            null, //don't group rows
            null, //don't filter by row groups
            sortOrder
        )

        var name: String
        var rgb: String
        with(cursor){
            moveToFirst()
            try {
                name = getString(getColumnIndexOrThrow(DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_NAME))
                rgb = getString(getColumnIndexOrThrow(DatabaseReaderContract.DatabaseEntry.COLUMN_NAME_RGB))
            } catch (e: Exception) {
                return null
            }

        }
        cursor.close()
        name = context.resources.getString(R.string.dmc_colour_template, name, id)

        val newColour = Colour(databaseRgbToColorInt(rgb))
            newColour.setName(name)
        return newColour
    }

    @ColorInt
    private fun databaseRgbToColorInt(rgb: String): Int {
        return Color.rgb(Integer.parseInt(rgb.substring(2, 4), 16),
            Integer.parseInt(rgb.substring(4, 6), 16),
            Integer.parseInt(rgb.substring(6), 16))
    }

    object DatabaseReaderContract {
        object DatabaseEntry {
            const val TABLE_NAME = "DMC_Name_RGB"
            const val COLUMN_NAME_DMC_ID = "DMCid"
            const val COLUMN_NAME_NAME = "Name"
            const val COLUMN_NAME_RGB = "RGB"
        }
    }
}