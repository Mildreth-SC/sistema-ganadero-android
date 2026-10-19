# Documentación de la Capa de Datos — Etapa 2 (Sistema Ganadero)

Este documento describe la arquitectura, configuración y componentes de la **Capa de Datos** implementada con **Room Database** y **KSP** para la aplicación **Ganadero Control**.

---

## 1. Arquitectura y Componentes

La capa de datos sigue los principios de arquitectura limpia de Android, separando los modelos de persistencia (Base de Datos) de los modelos de dominio (UI).

### A. Dependencias y Herramientas
- **Room Database** (`v2.6.1`): ORM oficial de Android para SQLite.
- **KSP** (Kotlin Symbol Processing): Procesador de anotaciones rápido y eficiente para compilar Room y Hilt.
- **Coroutines / Dispatchers**: Operaciones de base de datos ejecutadas de forma asíncrona mediante corrutinas.
- **Hilt** (`v2.51.1`): Inyección de dependencias para proveer la base de datos, los DAOs y el repositorio.

### B. Estructura de Paquetes
```
com.ganadero.control
├── data
│   ├── local          → Entities, DAOs y Database (Room)
│   └── repository     → Interfaz e implementación del Repositorio
├── model              → Modelos de dominio (Animal, Produccion, Vacuna, Finanza)
├── di                 → Módulos Hilt (DatabaseModule, RepositoryModule)
└── ui                 → Pantallas (View) y ViewModels (MVVM)
```

### C. Entidades (`@Entity`) — `data/local/Entities.kt`
Representan las tablas en SQLite:
1. **`AnimalEntity`** (`animales`): Almacena información de identificación, raza, sexo, peso, genealogía, estado reproductivo, número de partos (`partos`) y el motivo del estado (`motivoEstado`: En crecimiento / Pendiente de venta / Vendido).
2. **`ProduccionEntity`** (`producciones`): Registra la producción lechera vinculada a un animal (`animalId`), fecha, turno y litros.
3. **`VacunaEntity`** (`vacunas`): Registra el historial de vacunación, tipo de vacuna y fechas de aplicación/próxima dosis.
4. **`FinanzaEntity`** (`finanzas`): Control de ingresos y gastos de la finca (categoría, monto, fecha, descripción).

### D. DAOs (`@Dao`) — `data/local/Daos.kt`
Interfaces de acceso a datos que definen las consultas SQL:
- `AnimalDao`
- `ProduccionDao`
- `VacunaDao`
- `FinanzaDao`

Retornan `Flow<T>` para observación reactiva y `suspend fun` para operaciones de escritura.

### E. Base de Datos (`GanaderoDatabase`) — `data/local/GanaderoDatabase.kt`
Clase singleton `RoomDatabase` (versión 4) que gestiona las conexiones y migraciones de la base de datos local `ganadero_database`. Registra `MIGRATION_1_2`, `MIGRATION_2_3` y `MIGRATION_3_4` (columnas `partos` y `motivoEstado`) y crea datos de ejemplo (seed) en la primera ejecución.

### F. Modelos de Dominio — `model/Models.kt`
`Animal`, `Produccion`, `Vacuna` y `Finanza`: modelos limpios que consume la UI, desacoplados de las entidades de Room.

### G. Mapeadores y Repositorio
- **Mappers** (`data/local/Entities.kt`): funciones de extensión `toDomain()` / `toEntity()` que transforman entidades de Room a modelos de dominio y viceversa.
- **Repositorio** (`data/repository/GanaderoRepository.kt`):
  - `GanaderoRepository` (interfaz): define los contratos de acceso a datos.
  - `GanaderoRepositoryImpl` (implementación): recibe los DAOs por inyección de dependencias y ejecuta las operaciones, aplicando los mappers.

---

## 2. Inyección de Dependencias (Hilt)

Los componentes se proveen mediante módulos Hilt:
- **`di/DatabaseModule.kt`**: provee la instancia de `GanaderoDatabase` y los cuatro DAOs.
- **`di/RepositoryModule.kt`**: enlaza `GanaderoRepository` con `GanaderoRepositoryImpl`.

Los ViewModels (`@HiltViewModel`) reciben el repositorio inyectado automáticamente.

---

## 3. Instrucciones para Integrantes del Equipo

1. **Sincronización con Gradle**: Sincroniza el proyecto en Android Studio tras clonar o actualizar para que KSP genere los stubs de Room y Hilt.
2. **Uso en UI**: Los ViewModels consumen los datos a través de `GanaderoRepository`, garantizando persistencia local automática tras reiniciar la app. La UI nunca accede directamente a los DAOs.
