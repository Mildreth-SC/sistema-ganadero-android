package com.ganadero.control.di

import com.ganadero.control.data.repository.AnimalRepository
import com.ganadero.control.data.repository.AnimalRepositoryImpl
import com.ganadero.control.data.repository.FinanzaRepository
import com.ganadero.control.data.repository.FinanzaRepositoryImpl
import com.ganadero.control.data.repository.ProduccionRepository
import com.ganadero.control.data.repository.ProduccionRepositoryImpl
import com.ganadero.control.data.repository.VacunaRepository
import com.ganadero.control.data.repository.VacunaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * INYECCIÓN DE DEPENDENCIAS (HILT):
 * Vincula (@Binds) cada interfaz de repositorio con su implementación concreta.
 * Un repositorio por entidad (SRP): Animal, Producción, Vacuna y Finanza.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAnimalRepository(impl: AnimalRepositoryImpl): AnimalRepository

    @Binds
    @Singleton
    abstract fun bindProduccionRepository(impl: ProduccionRepositoryImpl): ProduccionRepository

    @Binds
    @Singleton
    abstract fun bindVacunaRepository(impl: VacunaRepositoryImpl): VacunaRepository

    @Binds
    @Singleton
    abstract fun bindFinanzaRepository(impl: FinanzaRepositoryImpl): FinanzaRepository
}
