# Ganadero Control

Aplicación Android para la **gestión de una finca ganadera**. Permite administrar el inventario de animales, el registro de producción de leche, el calendario de vacunación y el control de finanzas (ingresos y gastos), todo **100% offline** con almacenamiento local.

El proyecto está construido con **Kotlin + Jetpack Compose** siguiendo el patrón **MVVM**, con **Room** para persistencia, **Hilt** para inyección de dependencias y **Navigation Compose** para la navegación.

---

## Funcionalidades

- **Inicio (Dashboard):** resumen con totales de animales, producción de leche, alertas de próximas dosis y balance financiero. Incluye gráfica de producción y **exportación de reporte PDF** (Inventario + Producción + Finanzas).
- **Animales:** lista con **activos** y una sección aparte de **inactivos** (terneras "En crecimiento", terneros "Vendidos", etc.). Alta, edición, eliminación y ficha de detalle (edad calculada, partos por año, genealogía, estado reproductivo).
- **Leche (Producción):** registro de ordeños por animal, fecha y turno (Mañana / Tarde), con totales y promedios.
- **Vacunas:** registro de vacunas aplicadas con **próxima dosis** programada.
- **Finanzas:** movimientos de tipo Ingreso/Gasto por categoría, con utilidad neta.

## Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM (ViewModel + `StateFlow` / `UiState` sellado) |
| Persistencia | Room (SQLite) |
| Inyección de dependencias | Hilt (Dagger) |
| Navegación | Navigation Compose (con argumentos de ruta) |
| Diseño adaptativo | `material3-window-size-class` (`WindowSizeClass`) |
| Imágenes | Coil |
| Reportes | `android.graphics.pdf.PdfDocument` (nativo, sin librerías extra) |
| Pruebas | JUnit4 + kotlinx-coroutines-test |

Versiones: `compileSdk 34`, `minSdk 24`, `targetSdk 34`, JVM 17. Room `2.6.1`, Hilt `2.51.1`, Navigation `2.7.7`, Compose BOM `2024.06.00`, Gradle `8.12`.

## Arquitectura y estructura

El código está organizado por capas dentro de `app/src/main/java/com/ganadero/control/`:

```
├── GanaderoApplication.kt     # @HiltAndroidApp (punto de entrada de Hilt)
├── MainActivity.kt            # @AndroidEntryPoint, Scaffold + NavHost + navegación adaptativa (NavigationBar/NavigationRail)
├── model/                     # Modelos de dominio (Animal, Produccion, Vacuna, Finanza)
├── domain/
│   └── usecase/               # Casos de uso (validación de reglas de negocio, `operator fun invoke`)
├── data/
│   ├── local/                 # Room: Entities, Daos, GanaderoDatabase (migraciones) y SeedData
│   └── repository/            # Un repositorio por entidad: Animal, Produccion, Vacuna y Finanza
├── di/                        # Módulos de Hilt (DatabaseModule, RepositoryModule)
├── ui/
│   ├── dashboard/             # Pantalla de inicio + reporte PDF
│   ├── animales/              # Lista, detalle y ViewModels
│   ├── produccion/            # Registro de leche
│   ├── vacunas/               # Vacunación
│   ├── finanzas/              # Ingresos y gastos
│   ├── navigation/            # Grafo de navegación y definición de rutas (Screen)
│   ├── components/            # Componentes reutilizables (formularios, selectores, diálogos)
│   └── theme/                 # Colores y tema
└── util/                      # Utilidades (cálculo de fechas, generador de PDF)
```

**Flujo de dependencias:** `Composable → ViewModel → UseCase (dominio) → Repository → DAO (Room) → SQLite`.
Cada ViewModel expone un `UiState` sellado mediante `StateFlow`, recolectado en la UI con `collectAsStateWithLifecycle()`. El detalle de animal recibe su `animalId` por la ruta y lo lee desde `SavedStateHandle`.

**Diseño adaptativo:** la actividad calcula el `WindowSizeClass`; en pantallas compactas (teléfono) se usa `NavigationBar` inferior y en medianas/expandidas (plegable, tablet) un `NavigationRail` lateral, reorganizando el layout en lugar de solo estirarlo.

## Base de datos

La base de datos Room (`ganadero_database`, versión **4**) incluye migraciones incrementales `1→2`, `2→3` y `3→4` registradas en `GanaderoDatabase`. Los esquemas exportados viven en `app/schemas/`.

Al crearse por primera vez, se **auto-pobla con datos de ejemplo** (`data/local/SeedData.kt`) para que la app sea demostrable de inmediato:

- 32 animales (16 vacas Girolando + 4 Gyr, 2 toros y 10 crías inactivas)
- 720 registros de producción (20 vacas × 18 días × 2 ordeños)
- 89 vacunas aplicadas con próxima dosis
- 32 movimientos financieros

> El seed corre **solo la primera vez** que se crea la base de datos (`onCreate`). No se reinserta en cada arranque para evitar duplicados.

## Requisitos

- Android Studio (Ladybug o superior recomendado)
- JDK 17
- Android SDK 34
- Un emulador o dispositivo físico con Android 7.0 (API 24) o superior

## Cómo compilar y ejecutar

```bash
# Compilar
./gradlew assembleDebug

# Ejecutar pruebas unitarias
./gradlew testDebugUnitTest

# Instalar en el dispositivo/emulador conectado
./gradlew installDebug
```

O simplemente abrir el proyecto en Android Studio y pulsar **Run ▶**.

### Restablecer los datos de ejemplo

Para volver a generar el seed desde cero en un dispositivo donde la app ya corrió:

```bash
adb shell pm clear com.ganadero.control
```

Luego abre la app: la base de datos se crea de nuevo y el seed se ejecuta.

## Pruebas

Pruebas unitarias de la lógica de datos y del ViewModel de animales:

```bash
./gradlew testDebugUnitTest
```

## Ramas

- `main` — rama estable.
- `feature/continuar-mvvm` — rama de desarrollo con la versión más reciente.

## Autores

Proyecto académico del curso de **Aplicaciones Móviles**.

- GUANOLUISA MOREIRA MILDRETH XIOMARA
- MOLINA PONCE FRANKLIN VICENTE
- MOLINA PONCE MELANI ANAHI

## Documentación adicional

- [`DATA_LAYER.md`](DATA_LAYER.md) — documentación detallada de la capa de datos (entidades, DAOs, migraciones y seed).
- [`CHECKLIST_RUBRICA.md`](CHECKLIST_RUBRICA.md) — verificación del cumplimiento de la rúbrica.
