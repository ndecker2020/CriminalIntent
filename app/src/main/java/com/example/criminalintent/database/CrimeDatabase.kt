package com.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminalintent.Crime

@Database(entities = [Crime::class],version =1,exportSchema = false)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase:RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

