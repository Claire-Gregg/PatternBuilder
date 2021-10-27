package com.example.pattern_creator

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


const val DATABASE_NAME = "dmc_thread_rgb.db"
const val DATABASE_VERSION = 1

class DataBaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var myDataBase: SQLiteDatabase? = null

    init {
        // Check if the database already copied to the device.
        val dbExist = checkDatabase()
        if (dbExist) {
            // if already copied then don't do anything.
            Log.e("-----", "Database exists")
        } else {
            // else copy the database to the device.
            Log.e("-----", "Database doesn't exist")
            createDatabase()
        }
    }
    fun createDatabase(){
        copyDataBase()
    }

    private fun checkDatabase(): Boolean {
        val dbFile = File(context.getDatabasePath(DATABASE_NAME).path)
        return dbFile.exists()
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val inputStream = context.assets.open(DATABASE_NAME)

        val outputFile = File(context.getDatabasePath(DATABASE_NAME).path)
        val outputStream = FileOutputStream(outputFile)

        val bytesCopied = inputStream.copyTo(outputStream)
        Log.e("bytesCopied", "$bytesCopied")
        inputStream.close()

        outputStream.flush()
        outputStream.close()
    }

    fun deleteDatabase() {
        val file = File(context.getDatabasePath(DATABASE_NAME).path)
        if (file.exists()) {
            file.delete()
            println("delete database file.")
        }
    }

    fun openDatabase() {
        myDataBase = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).path, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun closeDataBase() {
       myDataBase?.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion)
        {
            Log.v("Database Upgrade", "Database version higher than old.")
            deleteDatabase()
        }
    }

}