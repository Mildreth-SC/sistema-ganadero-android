package com.ganadero.control.data.repository

import com.ganadero.control.data.local.VacunaDao
import com.ganadero.control.data.local.toDomain
import com.ganadero.control.data.local.toEntity
import com.ganadero.control.model.Vacuna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * REPOSITORIO DE VACUNAS:
 * Abstrae el acceso a la tabla `vacunas`.
 */
interface VacunaRepository {
    val vacunas: Flow<List<Vacuna>>
    suspend fun addVacuna(v: Vacuna)
    suspend fun updateVacuna(v: Vacuna)
    suspend fun deleteVacuna(id: Int)
}

class VacunaRepositoryImpl @Inject constructor(
    private val vacunaDao: VacunaDao
) : VacunaRepository {

    override val vacunas: Flow<List<Vacuna>> =
        vacunaDao.getAllVacunas().map { list -> list.map { it.toDomain() } }

    override suspend fun addVacuna(v: Vacuna) = withContext(Dispatchers.IO) {
        vacunaDao.insertVacuna(v.toEntity())
        Unit
    }

    override suspend fun updateVacuna(v: Vacuna) = withContext(Dispatchers.IO) {
        vacunaDao.updateVacuna(v.toEntity())
    }

    override suspend fun deleteVacuna(id: Int) = withContext(Dispatchers.IO) {
        vacunaDao.deleteVacunaById(id)
    }
}
