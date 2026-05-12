# AGENTS.md - Trackfolio

## Stack Tecnológico
- **Kotlin:** 2.1.0
- **Compose Multiplatform:** 1.7.3
- **Koin (DI):** 4.0.0
- **SQLDelight:** 2.0.2
- **Coroutines:** 1.9.0
- **Android AGP:** 8.5.2 / Compile SDK: 34 / Min SDK: 24

## Arquitectura
Clean Architecture + MVVM:
- **UI Layer:** `@Composable` + `ViewModel()` (estado + eventos UI)
- **Domain Layer:** modelos, repos interfaces, use cases
- **Data Layer:** datasources, repos implementations, database (SQLDelight)

## Estructura de Paquetes
```
es.aviferdev.trackfolio/
├── ui/{feature}/          # screens, viewmodel (home, account, portfolio, transaction, settings)
├── domain/
│   ├── model/             # modelos dominio
│   ├── repository/        # interfaces repositorio
│   └── usecase/           # casos de uso
├── data/
│   ├── datasource/        # fuentes datos local
│   ├── repository/        # implementaciones repositorio
│   └── database/          # SQLDelight (mappers, init)
└── di/                    # módulos Koin
```

## Convenciones de Código
- **ViewModels:** `XxxViewModel` (ej: `PortfolioViewModel`)
- **Repositorios:** `XxxRepository`, `XxxRepositoryImpl`
- **DataSources:** `XxxLocalDataSource`, `XxxLocalDataSourceImpl`
- **UseCases:** `XxxUseCase` o `XxxUseCases.kt`
- **Paquetes UI:** por funcionalidad (`home`, `account`, `portfolio`)

## Build Commands

### Android
```bash
./gradlew :composeApp:assembleDebug    # Debug APK
./gradlew :composeApp:build            # Build completo
```

### iOS
```bash
# Ver errores de compilación iOS
./gradlew :composeApp:compileKotlinIosArm64 2>&1 | grep "^e:"
```

### SQLDelight
- **Paquete generado:** `es.aviferdev.trackfolio.data.database`
- **Base de datos:** `TrackfolioDatabase`

## Screens y Features

| Feature | Screens/Componentes |
|---------|---------------------|
| **home** | HomeScreen, AddTransactionBottomSheet, SetInitialBalanceBottomSheet, PriceUpdateBottomSheet, PriceReminderBanner |
| **account** | AccountListScreen, AddEditAccountBottomSheet, AccountSelectorBar, AccountSession |
| **portfolio** | PortfolioScreen, AssetDetailScreen, AssetHistoryScreen, AssetCategoryDetailScreen, PortfolioSettingsScreen |
| **transaction** | TransactionListScreen |
| **settings** | SettingsScreen, IncomeSettingsScreen, ExpenseSettingsScreen, CategoryViewModel |
| **debt** | DebtListScreen, AddDebtBottomSheet |
| **fiscal** | FiscalReportScreen |
| **annual** | AnnualSummaryScreen |
| **reconciliation** | ReconciliationReminderBanner, ReconcileBalanceBottomSheet |
| **security** | LockScreen |


## Notas
- Proyecto Kotlin Multiplatform (Android + iOS)
- DI con Koin: módulos separados (PlatformModule, DatabaseModule, RepositoryModule, UseCaseModule)
- Navigation: Navigation Compose 2.8.0-alpha10

## Workflow de Agentes

Dos roles separados que colaboran en serie:

| Rol | Modelo | Tareas | Límites |
|---|---|---|---|
| **Analista de Requisitos** | deepseek-v4 | Analizar requisitos, explorar código existente, generar especificación técnica con el formato `## Análisis / Arquitectura propuesta / Especificación técnica / Implementación sugerida`, preguntar ambigüedades. | **Nunca escribe ni modifica código fuente.** Solo produce specs. |
| **Desarrollador** | deepseek-v4-flash | Implementar código siguiendo la spec del analista, respetando Clean Architecture, MVVM y las convenciones del proyecto. | **No toma decisiones de diseño ni arquitectura.** Si detecta ambigüedades, las devuelve al analista. |

**Flujo:**
```
Usuario pide feature → Analista genera spec → Usuario revisa/aprueba → Desarrollador implementa
```

**Regla de oro:** Si el analista está en una sesión, debe rechazar peticiones de escribir código y redirigirlas al desarrollador. Si el desarrollador necesita tomar una decisión de diseño, debe consultar al analista.

## Skill: LYRA (Prompt Engineering)

Para activar esta skill, el usuario debe decir "Actúa como LYRA" o "Modo LYRA".

Cuando se active, sigue estas reglas por encima del comportamiento por defecto:

1. **NO ejecutes** la petición del usuario directamente.
2. Aplica el marco **4D**:
   - **DECONSTRUCT:** Analiza la petición. Identifica ambigüedades, falta de contexto, áreas con detalles insuficientes.
   - **DIAGNOSE:** Haz entre 3 y 5 preguntas críticas y breves (objetivo, audiencia, tono, formato, restricciones).
   - **DEVELOP:** Cuando el usuario responda, redacta un prompt de alta ingeniería usando técnicas avanzadas (Chain-of-Thought, roles, delimitadores XML, few-shot examples si aplica).
   - **DELIVER:** Presenta el prompt final en un bloque de código y explica brevemente por qué las mejoras optimizan el resultado.
3. Preséntate como LYRA solo al inicio de la sesión.