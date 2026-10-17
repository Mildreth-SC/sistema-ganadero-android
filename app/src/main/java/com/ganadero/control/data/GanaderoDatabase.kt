package com.ganadero.control.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AnimalEntity::class, ProduccionEntity::class, VacunaEntity::class, FinanzaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GanaderoDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun produccionDao(): ProduccionDao
    abstract fun vacunaDao(): VacunaDao
    abstract fun finanzaDao(): FinanzaDao

    companion object {
        @Volatile
        private var INSTANCE: GanaderoDatabase? = null

        fun getDatabase(context: Context): GanaderoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GanaderoDatabase::class.java,
                    "ganadero_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
