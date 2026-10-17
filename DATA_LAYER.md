# Documentación de la Capa de Datos — Etapa 2 (Sistema Ganadero)

Este documento describe la arquitectura, configuración y componentes de la **Capa de Datos** implementada con **Room Database** y **KSP** para la aplicación **Ganadero Control**.

---

## 1. Arquitectura y Componentes

La capa de datos sigue los principios de arquitectura limpia de Android, separando los modelos de persistencia (Base de Datos) de los modelos de dominio (UI).

### A. Dependencias y Herramientas
- **Room Database** (`v2.6.1`): ORM oficial de Android para SQLite.
- **KSP** (Kotlin Symbol Processing): Procesador de anotaciones rápido y eficiente para compilar Room.
- **Coroutines / Dispatchers**: Operaciones de base de datos ejecutadas de forma asíncrona mediante corrutinas.

### B. Entidades (`@Entity`)
Representan las tablas en SQLite:
1. **`AnimalEntity`** (`animales`): Almacena información de identificación, raza, sexo, peso y genealogía de cada animal.
2. **`ProduccionEntity`** (`producciones`): Registra la producción lechera vinculada a un animal (`animalId`), fecha, turno y litros.
3. **`VacunaEntity`** (`vacunas`): Registra el historial de vacunación, tipo de vacuna y fechas de aplicación/próxima dosis.
4. **`FinanzaEntity`** (`finanzas`): Control de ingresos y gastos de la finca (categoría, monto, fecha, descripción).

### C. DAOs (`@Dao`)
Interfaces de acceso a datos que definen las consultas SQL:
- `AnimalDao`
- `ProduccionDao`
- `VacunaDao`
- `FinanzaDao`

### D. Base de Datos (`GanaderoDatabase`)
Clase singleton `RoomDatabase` que gestiona las conexiones y migraciones de la base de datos local `ganadero_database`.

### E. Mapeadores y Repositorios
- **Mappers**: Funciones de extensión (`toDomain()` / `toEntity()`) que transforman entidades de Room a modelos de la aplicación.
- **Repositorio (`Repo`)**: Fachada centralizada que opera directamente sobre los DAOs de Room y provee los datos cacheados y persistidos a la capa de UI.

---

## 2. Instrucciones para Integrantes del Equipo

1. **Sincronización con Gradle**: Asegúrate de sincronizar el proyecto en Android Studio tras clonar o actualizar para que KSP genere los stubs de Room.
2. **Uso en UI**: Los componentes de interfaz consumen los datos a través del objeto `Repo` (o repositorios específicos), garantizando persistencia local automática tras reiniciar la app.
