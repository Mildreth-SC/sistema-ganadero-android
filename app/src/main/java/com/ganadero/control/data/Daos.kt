package com.ganadero.control.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * CAPA DE DATOS Y FLUJOS ASÍNCRONOS (DAOs DE ROOM):
 * 1. Lecturas reactivas mediante Flow<T> para reflejar cambios automáticamente en la UI.
 * 2. Escrituras/actualizaciones/eliminaciones como funciones de suspensión (suspend fun).
 * 3. El borrado en cascada de producciones/vacunas lo aplica SQLite vía @ForeignKey(ON DELETE CASCADE).
 */

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animales")
    fun getAllAnimales(): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animales WHERE id = :id")
    fun getById(id: Int): Flow<AnimalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity): Long

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Query("DELETE FROM animales WHERE id = :id")
    suspend fun deleteAnimalById(id: Int)

    @Query("SELECT COUNT(*) FROM animales")
    suspend fun count(): Int
}

@Dao
interface VacunaDao {
    @Query("SELECT * FROM vacunas")
    fun getAllVacunas(): Flow<List<VacunaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacuna(vacuna: VacunaEntity): Long

    @Query("DELETE FROM vacunas WHERE id = :id")
    suspend fun deleteVacunaById(id: Int)

    @Update
    suspend fun updateVacuna(vacuna: VacunaEntity)

    @Query("SELECT COUNT(*) FROM vacunas")
    suspend fun count(): Int
}

@Dao
interface FinanzaDao {
    @Query("SELECT * FROM finanzas")
    fun getAllFinanzas(): Flow<List<FinanzaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanza(finanza: FinanzaEntity): Long

    @Query("DELETE FROM finanzas WHERE id = :id")
    suspend fun deleteFinanzaById(id: Int)

    @Update
    suspend fun updateFinanza(finanza: FinanzaEntity)

    @Query("SELECT COUNT(*) FROM finanzas")
    suspend fun count(): Int
}

@Dao
interface ProduccionDao {
    @Query("SELECT * FROM producciones")
    fun getAllProducciones(): Flow<List<ProduccionEntity>>

    @Query("SELECT * FROM producciones WHERE animalId = :animalId ORDER BY fecha DESC")
    fun getByAnimalId(animalId: Int): Flow<List<ProduccionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduccion(produccion: ProduccionEntity): Long

    @Query("DELETE FROM producciones WHERE id = :id")
    suspend fun deleteProduccionById(id: Int)

    @Update
    suspend fun updateProduccion(produccion: ProduccionEntity)

    @Query("SELECT COUNT(*) FROM producciones")
    suspend fun count(): Int
}
