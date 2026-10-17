package com.ganadero.control.di

import javax.inject.Qualifier

/**
 * Qualifier para distinguir el CoroutineScope de aplicación
 * de cualquier otro CoroutineScope que se inyecte.
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope
