package com.ganadero.control.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animales")
    suspend fun getAllAnimals(): List<AnimalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity): Long

    @Query("DELETE FROM animales WHERE id = :id")
    suspend fun deleteAnimal(id: Int)
}

@Dao
interface ProduccionDao {
    @Query("SELECT * FROM producciones")
    suspend fun getAllProducciones(): List<ProduccionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduccion(produccion: ProduccionEntity): Long

    @Query("DELETE FROM producciones WHERE id = :id")
    suspend fun deleteProduccion(id: Int)

    @Query("DELETE FROM producciones WHERE animalId = :animalId")
    suspend fun deleteByAnimalId(animalId: Int)
}

@Dao
interface VacunaDao {
    @Query("SELECT * FROM vacunas")
    suspend fun getAllVacunas(): List<VacunaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacuna(vacuna: VacunaEntity): Long

    @Query("DELETE FROM vacunas WHERE id = :id")
    suspend fun deleteVacuna(id: Int)

    @Query("DELETE FROM vacunas WHERE animalId = :animalId")
    suspend fun deleteByAnimalId(animalId: Int)
}

@Dao
interface FinanzaDao {
    @Query("SELECT * FROM finanzas")
    suspend fun getAllFinanzas(): List<FinanzaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanza(finanza: FinanzaEntity): Long

    @Query("DELETE FROM finanzas WHERE id = :id")
    suspend fun deleteFinanza(id: Int)
}
