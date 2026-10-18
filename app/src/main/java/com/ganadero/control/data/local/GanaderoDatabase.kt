package com.ganadero.control.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ganadero.control.model.Animal
import com.ganadero.control.model.Finanza
import com.ganadero.control.model.Produccion
import com.ganadero.control.model.Vacuna
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * PERSISTENCIA LOCAL Y ÚNICA FUENTE DE VERDAD (ROOM / SQLITE):
 * Configuración central de la base de datos Room.
 * - Versión 2 con MIGRATION_1_2 (FKs + índices de integridad referencial y unificación
 *   del estado del animal en el campo `activo`).
 * - exportSchema = true: los esquemas se exportan a app/schemas/ para diseñar migraciones futuras.
 */
@Database(
    entities = [AnimalEntity::class, ProduccionEntity::class, VacunaEntity::class, FinanzaEntity::class],
    version = 3,
    exportSchema = true
)
abstract class GanaderoDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun produccionDao(): ProduccionDao
    abstract fun vacunaDao(): VacunaDao
    abstract fun finanzaDao(): FinanzaDao

    companion object {
        /**
         * Migración 1 → 2:
         * 1. `animales`: se eliminan las columnas redundantes `estado` e `inactivo`
         *    (el estado queda unificado en `activo`).
         * 2. `producciones` y `vacunas`: se recrean con FOREIGN KEY a `animales(id)`
         *    con ON DELETE CASCADE + índice sobre `animalId`.
         * SQLite (API 24) no soporta ALTER TABLE DROP COLUMN, por eso se recrean las tablas.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `animales_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `codigo` TEXT NOT NULL,
                        `nombre` TEXT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `raza` TEXT NOT NULL,
                        `sexo` TEXT NOT NULL,
                        `fechaNacimiento` TEXT NOT NULL,
                        `pesoActual` REAL NOT NULL,
                        `madreNombre` TEXT NOT NULL,
                        `madreRaza` TEXT NOT NULL,
                        `padreNombre` TEXT NOT NULL,
                        `padreRaza` TEXT NOT NULL,
                        `fotoUri` TEXT,
                        `activo` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `animales_new` (`id`,`codigo`,`nombre`,`tipo`,`raza`,`sexo`," +
                        "`fechaNacimiento`,`pesoActual`,`madreNombre`,`madreRaza`,`padreNombre`," +
                        "`padreRaza`,`fotoUri`,`activo`) " +
                        "SELECT `id`,`codigo`,`nombre`,`tipo`,`raza`,`sexo`,`fechaNacimiento`," +
                        "`pesoActual`,`madreNombre`,`madreRaza`,`padreNombre`,`padreRaza`," +
                        "`fotoUri`,`activo` FROM `animales`"
                )
                db.execSQL("DROP TABLE `animales`")
                db.execSQL("ALTER TABLE `animales_new` RENAME TO `animales`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `producciones_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `animalId` INTEGER NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `turno` TEXT NOT NULL,
                        `litros` REAL NOT NULL,
                        FOREIGN KEY(`animalId`) REFERENCES `animales`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `producciones_new` (`id`,`animalId`,`fecha`,`turno`,`litros`) " +
                        "SELECT `id`,`animalId`,`fecha`,`turno`,`litros` FROM `producciones`"
                )
                db.execSQL("DROP TABLE `producciones`")
                db.execSQL("ALTER TABLE `producciones_new` RENAME TO `producciones`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_producciones_animalId` ON `producciones` (`animalId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `vacunas_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `animalId` INTEGER NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `fechaAplicacion` TEXT NOT NULL,
                        `proximaFecha` TEXT NOT NULL,
                        FOREIGN KEY(`animalId`) REFERENCES `animales`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `vacunas_new` (`id`,`animalId`,`tipo`,`fechaAplicacion`,`proximaFecha`) " +
                        "SELECT `id`,`animalId`,`tipo`,`fechaAplicacion`,`proximaFecha` FROM `vacunas`"
                )
                db.execSQL("DROP TABLE `vacunas`")
                db.execSQL("ALTER TABLE `vacunas_new` RENAME TO `vacunas`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vacunas_animalId` ON `vacunas` (`animalId`)")
            }
        }

        /**
         * Migración 2 → 3:
         * Se agrega la columna `estadoReproductivo` a la tabla `animales` con valor
         * por defecto 'Ideal'. SQLite (API 24) sí soporta ADD COLUMN cuando la columna
         * tiene un valor por defecto, por eso es una migración simple.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `animales` ADD COLUMN `estadoReproductivo` TEXT NOT NULL DEFAULT 'Ideal'"
                )
            }
        }

        @Volatile
        private var INSTANCE: GanaderoDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GanaderoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GanaderoDatabase::class.java,
                    "ganadero_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(GanaderoDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GanaderoDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        database.animalDao(),
                        database.produccionDao(),
                        database.vacunaDao(),
                        database.finanzaDao()
                    )
                }
            }
        }

        private suspend fun populateDatabase(
            animalDao: AnimalDao,
            produccionDao: ProduccionDao,
            vacunaDao: VacunaDao,
            finanzaDao: FinanzaDao
        ) {
            val today = todayStr()
            val lunaId = animalDao.insertAnimal(
                Animal(codigo = "A-001", nombre = "Luna", tipo = "Leche", raza = "Holstein", sexo = "Hembra", pesoActual = 480.0).toEntity()
            ).toInt()
            val estrellaId = animalDao.insertAnimal(
                Animal(codigo = "A-002", nombre = "Estrella", tipo = "Leche", raza = "Jersey", sexo = "Hembra", pesoActual = 450.0).toEntity()
            ).toInt()
            animalDao.insertAnimal(
                Animal(codigo = "A-003", nombre = "Tormenta", tipo = "Carne", raza = "Brahman", sexo = "Macho", pesoActual = 720.0).toEntity()
            )

            produccionDao.insertProduccion(
                Produccion(animalId = lunaId, fecha = today, turno = "Mañana", litros = 12.5).toEntity()
            )
            produccionDao.insertProduccion(
                Produccion(animalId = estrellaId, fecha = today, turno = "Tarde", litros = 10.0).toEntity()
            )

            vacunaDao.insertVacuna(
                Vacuna(animalId = lunaId, tipo = "Fiebre aftosa", fechaAplicacion = today, proximaFecha = today).toEntity()
            )

            finanzaDao.insertFinanza(
                Finanza(fecha = today, tipo = "Ingreso", categoria = "Venta de leche", monto = 350000.0, descripcion = "Venta semanal").toEntity()
            )
            finanzaDao.insertFinanza(
                Finanza(fecha = today, tipo = "Gasto", categoria = "Alimento", monto = 120000.0, descripcion = "Concentrado").toEntity()
            )
        }

        private fun todayStr(): String {
            val c = Calendar.getInstance()
            return "%04d-%02d-%02d".format(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
