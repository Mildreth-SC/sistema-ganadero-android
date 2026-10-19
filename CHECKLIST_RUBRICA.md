# Checklist de rúbrica — Ganadero Control

Verificación de los criterios solicitados por la asignatura, con evidencia en el código.

| # | Criterio | Estado | Evidencia |
|---|---|:---:|---|
| 1 | Proyecto Android en Kotlin + Jetpack Compose | ✅ | `app/build.gradle.kts` (`compose = true`), `ui/theme/*` |
| 2 | Arquitectura **MVVM** | ✅ | `ui/*/` (Screen + ViewModel por pantalla), sep. UI/lógica |
| 3 | Estado de UI con `UiState` sellado + `StateFlow` | ✅ | `ui/animales/AnimalesViewModel.kt`, `ui/dashboard/DashboardViewModel.kt`, etc. |
| 4 | UI reactiva con `collectAsStateWithLifecycle()` | ✅ | pantallas en `ui/*/` |
| 5 | Persistencia con **Room** | ✅ | `data/local/GanaderoDatabase.kt`, `Daos.kt`, `Entities.kt` |
| 6 | **Migraciones** de base de datos | ✅ | `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4` en `GanaderoDatabase.kt`; esquemas en `app/schemas/` |
| 7 | **Repository** (interfaz + implementación) | ✅ | `data/repository/GanaderoRepository.kt` |
| 8 | Separación de **modelo de dominio** y entidades | ✅ | `model/Models.kt` vs `data/local/Entities.kt` (+ mappers) |
| 9 | Inyección de dependencias con **Hilt** | ✅ | `GanaderoApplication.kt`, `di/DatabaseModule.kt`, `di/RepositoryModule.kt`, `@HiltViewModel`, `@AndroidEntryPoint` |
| 10 | **Navegación** con Navigation Compose | ✅ | `ui/navigation/GanaderoNavGraph.kt`, `Screen.kt` |
| 11 | Navegación con **argumentos de ruta** | ✅ | `animal_detail/{animalId}` + `SavedStateHandle` en `AnimalDetailViewModel` |
| 12 | Barra de navegación inferior funcional | ✅ | `MainActivity.kt` (`NavigationBar` + `NavHost`) |
| 13 | CRUD completo de una entidad | ✅ | `ui/animales/AnimalesScreen.kt` (crear, editar, eliminar, listar, ver detalle) |
| 14 | Uso de corrutinas / operaciones asíncronas | ✅ | DAOs `suspend`, `viewModelScope`, `Dispatchers.IO` |
| 15 | Datos de ejemplo para demostración | ✅ | `data/local/SeedData.kt` (32 animales, 720 producciones, 89 vacunas, 32 finanzas) |
| 16 | Pruebas unitarias | ✅ | `app/src/test/.../GanaderoRepositoryTest.kt`, `AnimalesViewModelTest.kt` (10 `@Test`) |
| 17 | Funcionalidad extra (valor agregado) | ✅ | Reporte **PDF** (`util/ReportePdf.kt`), gráfica de producción, edad y partos/año calculados |
| 18 | Documentación | ✅ | `README.md`, `DATA_LAYER.md` |

## Criterios opcionales / de forma

| # | Criterio | Estado | Nota |
|---|---|:---:|---|
| 19 | Eventos de UI con `Channel` | ⚠️ | Se usan `SharedFlow` (`MutableSharedFlow`) — funcionalmente equivalente |
| 20 | Prueba de migración instrumentada | ⚠️ | No incluida (se verificó manualmente) |
| 21 | Documento de entrega (Word/PDF) | ⏳ | A cargo del equipo (incluye capturas) |

## Cómo verificar el build

```bash
./gradlew compileDebugKotlin testDebugUnitTest assembleDebug
```

Debe terminar en **BUILD SUCCESSFUL**.
