package com.ganadero.control

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * INYECCIÓN DE DEPENDENCIAS (HILT):
 * Clase de aplicación anotada con @HiltAndroidApp.
 * Debe coincidir con el nombre declarado en AndroidManifest.xml (android:name=".GanaderoApplication").
 */
@HiltAndroidApp
class GanaderoApplication : Application()
