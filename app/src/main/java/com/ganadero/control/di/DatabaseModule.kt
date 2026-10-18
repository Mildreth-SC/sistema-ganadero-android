package com.ganadero.control.di

import android.content.Context
import com.ganadero.control.data.local.AnimalDao
import com.ganadero.control.data.local.FinanzaDao
import com.ganadero.control.data.local.GanaderoDatabase
import com.ganadero.control.data.local.ProduccionDao
import com.ganadero.control.data.local.VacunaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * INYECCIÓN DE DEPENDENCIAS (HILT):
 * Módulo que provee la instancia singleton de la base de datos Room, los DAOs
 * y el CoroutineScope de aplicación (calificado con @ApplicationScope).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideGanaderoDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): GanaderoDatabase {
        return GanaderoDatabase.getDatabase(context, scope)
    }

    @Provides
    fun provideAnimalDao(database: GanaderoDatabase): AnimalDao = database.animalDao()

    @Provides
    fun provideProduccionDao(database: GanaderoDatabase): ProduccionDao = database.produccionDao()

    @Provides
    fun provideVacunaDao(database: GanaderoDatabase): VacunaDao = database.vacunaDao()

    @Provides
    fun provideFinanzaDao(database: GanaderoDatabase): FinanzaDao = database.finanzaDao()
}
