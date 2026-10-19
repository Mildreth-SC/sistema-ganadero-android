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
| 7 | **Repository** (interfaz + implementación) | ✅ | `data/repository/`: un repositorio por entidad (`AnimalRepository`, `ProduccionRepository`, `VacunaRepository`, `FinanzaRepository`) |
| 8 | Separación de **modelo de dominio** y entidades | ✅ | `model/Models.kt` vs `data/local/Entities.kt` (+ mappers) |
| 9 | Inyección de dependencias con **Hilt** | ✅ | `GanaderoApplication.kt`, `di/DatabaseModule.kt`, `di/RepositoryModule.kt`, `@HiltViewModel`, `@AndroidEntryPoint` |
| 10 | **Navegación** con Navigation Compose | ✅ | `ui/navigation/GanaderoNavGraph.kt`, `Screen.kt` |
| 11 | Navegación con **argumentos de ruta** | ✅ | `animal_detail/{animalId}` + `SavedStateHandle` en `AnimalDetailViewModel` |
| 12 | Navegación funcional (barra inferior / riel) | ✅ | `MainActivity.kt` (`NavigationBar` en compacto y `NavigationRail` en mediano/expandido + `NavHost`) |
| 13 | CRUD completo de una entidad | ✅ | `ui/animales/AnimalesScreen.kt` (crear, editar, eliminar, listar, ver detalle) |
| 14 | Uso de corrutinas / operaciones asíncronas | ✅ | DAOs `suspend`, `viewModelScope`, `Dispatchers.IO` |
| 15 | Datos de ejemplo para demostración | ✅ | `data/local/SeedData.kt` (32 animales, 720 producciones, 89 vacunas, 32 finanzas) |
| 16 | Pruebas unitarias | ✅ | `app/src/test/.../RepositoriesTest.kt` (5), `AnimalesViewModelTest.kt` (6) → **11 `@Test`** |
| 17 | Funcionalidad extra (valor agregado) | ✅ | Reporte **PDF** (`util/ReportePdf.kt`), gráfica de producción, edad y partos/año calculados |
| 18 | Documentación | ✅ | `README.md`, `DATA_LAYER.md` |

## Criterios opcionales implementados (puntos extra)

| # | Criterio | Estado | Evidencia |
|---|---|:---:|---|
| 19 | **Capa de dominio / Use Cases** (`operator fun invoke`) | ✅ | `domain/usecase/`: `RegistrarAnimalUseCase`, `ActualizarAnimalUseCase`, `RegistrarProduccionUseCase`, `RegistrarVacunaUseCase`, `RegistrarFinanzaUseCase` |
| 20 | **Ícono adaptativo vectorizado** (background + foreground `.xml`) | ✅ | `res/mipmap-anydpi-v26/ic_launcher(_round).xml` → `res/drawable/ic_launcher_background.xml` + `ic_launcher_foreground.xml` |
| 21 | **Diseño adaptativo** con `WindowSizeClass` | ✅ | `MainActivity.kt` (`calculateWindowSizeClass`, `NavigationRail` en medium/expanded) |

## Otros criterios / de forma

| # | Criterio | Estado | Nota |
|---|---|:---:|---|
| 22 | Eventos de UI con `Channel` | ⚠️ | Se usan `SharedFlow` (`MutableSharedFlow`) — funcionalmente equivalente |
| 23 | Prueba de migración instrumentada | ⚠️ | No incluida (se verificó manualmente) |
| 24 | Documento de entrega (Word/PDF) | ⏳ | A cargo del equipo (incluye capturas) |

## Cómo verificar el build

```bash
./gradlew compileDebugKotlin testDebugUnitTest assembleDebug
```

Debe terminar en **BUILD SUCCESSFUL**.
