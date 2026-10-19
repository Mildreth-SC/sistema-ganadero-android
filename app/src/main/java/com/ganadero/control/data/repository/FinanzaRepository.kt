package com.ganadero.control.data.repository

import com.ganadero.control.data.local.FinanzaDao
import com.ganadero.control.data.local.toDomain
import com.ganadero.control.data.local.toEntity
import com.ganadero.control.model.Finanza
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * REPOSITORIO DE FINANZAS:
 * Abstrae el acceso a la tabla `finanzas`.
 */
interface FinanzaRepository {
    val finanzas: Flow<List<Finanza>>
    suspend fun addFinanza(f: Finanza)
    suspend fun updateFinanza(f: Finanza)
    suspend fun deleteFinanza(id: Int)
}

class FinanzaRepositoryImpl @Inject constructor(
    private val finanzaDao: FinanzaDao
) : FinanzaRepository {

    override val finanzas: Flow<List<Finanza>> =
        finanzaDao.getAllFinanzas().map { list -> list.map { it.toDomain() } }

    override suspend fun addFinanza(f: Finanza) = withContext(Dispatchers.IO) {
        finanzaDao.insertFinanza(f.toEntity())
        Unit
    }

    override suspend fun updateFinanza(f: Finanza) = withContext(Dispatchers.IO) {
        finanzaDao.updateFinanza(f.toEntity())
    }

    override suspend fun deleteFinanza(id: Int) = withContext(Dispatchers.IO) {
        finanzaDao.deleteFinanzaById(id)
    }
}
