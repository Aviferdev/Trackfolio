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
- **Core Layer:** Infraestructura transversal (seguridad, cifrado, preferencias)
- **UI Layer:** `@Composable` + `ViewModel()` (estado + eventos UI)
- **Domain Layer:** modelos, repos interfaces, use cases, calculadoras
- **Data Layer:** datasources, repos implementations, database (SQLDelight)

## Estructura de Paquetes
```
es.aviferdev.trackfolio/
├── App.kt                     # Composable raíz
├── core/                      # Infraestructura transversal
│   └── security/              # Cifrado, biometría, backup, preferencias
├── di/                        # Módulos Koin (Database, Repository, UseCase, UI)
├── domain/
│   ├── model/                 # Modelos de dominio (44 clases)
│   ├── repository/            # Interfaces de repositorio (18 interfaces)
│   ├── usecase/               # Casos de uso (96 clases en 65 archivos)
│   ├── calculator/            # Calculadoras de dominio (amortización, cartera, RF)
│   └── report/                # Generación de informes (PDF expect)
├── data/
│   ├── database/              # SQLDelight (driver, init, mappers)
│   ├── datasource/            # 9 fuentes de datos locales (interfaz + impl)
│   └── repository/            # Implementaciones de repositorio (18 clases)
└── ui/
    ├── theme/                 # Tema oscuro, colores, formateo
    ├── navigation/            # Rutas (Screen.kt), NavHost, barras (TopBar, BottomNav, TimeStepper)
    ├── common/                # Componentes UI compartidos
    │   ├── component/         # Atómicos: EmptyStateView, IconActionButton, SelectablePill/Chip, SectionHeader
    │   ├── chart/             # Gráficos: DonutSlice, LineChartCard, MonthlyBarChart
    │   ├── input/             # Formularios: SearchBar, AmountInputField, DatePickerRow, InlineAmountField
    │   ├── metric/            # Datos: TransactionRow, MetricCell
    │   ├── dialog/            # Diálogos: DeleteConfirmDialog
    │   └── loading/           # Global loading overlay
    ├── home/                  # HomeScreen, HeroCard, QuickAccessSection, RecentTransactionsSection, BottomSheets
    ├── account/               # AccountListScreen, AccountSelectorBar, AccountSession
    ├── portfolio/             # PortfolioScreen, AssetCard, PortfolioSummaryCard, AssetDetailScreen, etc.
    ├── transaction/           # TransactionListScreen, TransactionDetailScreen
    ├── fixedincome/           # FixedIncomeDetailScreen, BottomSheets de RF
    ├── loan/                  # LoanDetailScreen, AddEditLoanBottomSheet
    ├── debt/                  # DebtListScreen, AddDebtBottomSheet
    ├── networth/              # NetWorthScreen
    ├── fiscal/                # FiscalReportScreen (+ subcomponentes)
    ├── annual/                # AnnualSummaryScreen, gráficos anuales
    ├── settings/              # SettingsScreen, Income/Expense settings, backup
    ├── reconciliation/        # ReconciliationReminderBanner, ReconcileBalanceBottomSheet
    └── security/              # LockScreen
```

## Convenciones de Código
- **ViewModels:** `XxxViewModel` (ej: `PortfolioViewModel`)
- **Repositorios:** `XxxRepository`, `XxxRepositoryImpl`
- **DataSources:** `XxxLocalDataSource`, `XxxLocalDataSourceImpl`
- **UseCases:** `XxxUseCase` para lógica compleja; `XxxUseCases.kt` para CRUD simple agrupado
- **Componentes UI:** un archivo por componente `@Composable` público; los componentes extraídos de screens van en su propio archivo
- **Componentes comunes:** en `ui/common/` categorizados por tipo (`component/`, `input/`, `metric/`, `dialog/`, `chart/`, `loading/`)
- **Previews:** cada componente público debe tener al menos un `@Preview`
- **Paquetes UI:** por funcionalidad (`home`, `account`, `portfolio`, `fixedincome`, etc.)

## Build Commands

### Android
```bash
./gradlew :composeApp:assembleDebug    # Debug APK
./gradlew :composeApp:build            # Build completo
./gradlew :composeApp:compileDebugKotlinAndroid  # Solo compilación Kotlin
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
| **home** | HomeScreen, HomeViewModel, HeroCard, QuickAccessSection, RecentTransactionsSection, AddTransactionBottomSheet, SetInitialBalanceBottomSheet, PriceUpdateBottomSheet, PriceReminderBanner, MaturityReminderBanner, CategoryPickerScreen, CategoryPickerViewModel, AddTransactionViewModel |
| **account** | AccountListScreen, AccountViewModel, AddEditAccountBottomSheet, AccountSelectorBar, AccountSession |
| **portfolio** | PortfolioScreen, PortfolioViewModel, PortfolioSummaryCard, CategoryGroupHeader, AssetCard, ClosedAssetCard, ClosedFixedIncomeCard, ClosedPositionsHeader, FixedIncomeSectionHeader, PortfolioDistributionCard, AssetDetailScreen, AssetDetailViewModel, AssetHistoryScreen, AssetHistoryViewModel, AssetCategoryDetailScreen, AssetCategoryDetailViewModel, PortfolioSettingsScreen, AddEditAssetBottomSheet, AddEditAssetTransactionBottomSheet, AddDividendBottomSheet, TransferFundBottomSheet, UpdateCurrentPriceSheet, SectorManagementSheet, RegionManagementSheet, AddEditPlatformSheet, LinkPlatformToCategorySheet, AssetCatalogViewModel, AssetCategoryViewModel, PlatformViewModel |
| **fixedincome** | FixedIncomeDetailScreen, FixedIncomeDetailViewModel, FixedIncomeCard, FixedIncomePositionCard, CreateFixedIncomeBottomSheet, EditFixedIncomeBottomSheet, CloseFixedIncomeBottomSheet, RegisterCouponBottomSheet |
| **loan** | LoanDetailScreen, LoanDetailViewModel, AddEditLoanBottomSheet, UpdateLoanRateSheet |
| **debt** | DebtListScreen, DebtViewModel, AddDebtBottomSheet |
| **networth** | NetWorthScreen, NetWorthViewModel |
| **transaction** | TransactionListScreen, TransactionViewModel, TransactionDetailScreen, TransactionDetailViewModel |
| **fiscal** | FiscalReportScreen, FiscalReportViewModel |
| **annual** | AnnualSummaryScreen, AnnualViewModel, DonutChartCard, InvestmentBarChart, CategoryExpenseComparison |
| **settings** | SettingsScreen, IncomeSettingsScreen, ExpenseSettingsScreen, IncomeTypeDetailScreen, CategoryViewModel, CategorySheets, IssuerSheet, IssuerViewModel, BackupPasswordSheet, BackupViewModel |
| **reconciliation** | ReconciliationReminderBanner, ReconcileBalanceBottomSheet, ReconciliationViewModel |
| **security** | LockScreen |

## Componentes UI Compartidos (`ui/common/`)

| Categoría | Componentes |
|-----------|-------------|
| **component/** | `EmptyStateView`, `IconActionButton`, `IconButtonApp`, `SelectablePill`, `SelectableChip`, `SectionHeader`, `TrackfolioLabel`, `SectionLabel`, `DeltaIndicator`, `AlertBanner`, `InfoRow`, `StatusTag`, `ProgressBar`, `SwipeRowApp`, `InitialsAvatar`, `IconBadge` |
| **chart/** | `DonutSlice`, `LineChartCard`, `MonthlyBarChart`, `LegendItem`, `VariationBadge` |
| **input/** | `SearchBar`, `AmountInputField`, `DatePickerRow`, `InlineAmountField` |
| **metric/** | `MetricCell`, `TransactionRow`, `ReportCard` |
| **dialog/** | `DeleteConfirmDialog` |
| **loading/** | `GlobalLoadingManager`, `GlobalLoadingOverlay` |
| **icon/** | `MaterialIconMapper` |

## Core (infraestructura transversal)

| Paquete | Componentes |
|---------|-------------|
| **core.security** | `AesCrypto` (AES-256-CBC), `AppSettings` (expect/actual), `AppLockManager`, `BalanceVisibilityManager`, `BiometricAuthenticator` (expect/actual), `DatabaseBackupManager` (expect/actual), `PendingImport` |

## Notas
- Proyecto Kotlin Multiplatform (Android + iOS)
- DI con Koin: `DatabaseModule`, `RepositoryModule`, `UseCaseModule`, `UIModule`, `KoinInitializer` (common) + `AndroidModule`/`IosModule` (plataforma)
- Navigation: Navigation Compose 2.8.0-alpha10
- Los componentes de UI grandes (>300 líneas) se dividen en archivos por componente dentro del paquete de la feature
- Los patrones UI que aparecen en ≥2 features se extraen a `ui/common/`

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
