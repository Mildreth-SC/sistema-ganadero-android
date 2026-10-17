package com.ganadero.control.di

import com.ganadero.control.data.GanaderoRepository
import com.ganadero.control.data.GanaderoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * INYECCIÓN DE DEPENDENCIAS (HILT):
 * Módulo de Hilt encargado de vincular (@Binds) la interfaz del repositorio (GanaderoRepository)
 * con su implementación concreta (GanaderoRepositoryImpl).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGanaderoRepository(
        impl: GanaderoRepositoryImpl
    ): GanaderoRepository
}
