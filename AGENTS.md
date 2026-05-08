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
# Compilar para dispositivo real (ARM64)
./gradlew :composeApp:compileKotlinIosArm64

# Compilar para simulador
./gradlew :composeApp:compileKotlinIosSimulatorArm64

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

## Navigation Routes
```
home                    → HomeScreen
transactions            → TransactionListScreen
portfolio               → PortfolioScreen
portfolio_asset/{id}    → AssetDetailScreen
asset_history/{id}     → AssetHistoryScreen
portfolio_category/{id}→ AssetCategoryDetailScreen
portfolio_settings     → PortfolioSettingsScreen
debts                  → DebtListScreen
settings               → SettingsScreen
settings_expense       → ExpenseSettingsScreen
settings_income        → IncomeSettingsScreen
settings_income/{name} → IncomeTypeDetailScreen
fiscal_report         → FiscalReportScreen
charts                → AnnualSummaryScreen
```

## Modelos Principales
- **Asset** - Activos (acciones, fondos, crypto, bonos)
- **Account** - Cuentas bancarias
- **Transaction** - Transacciones (ingresos/gastos)
- **AssetCategory** - Categorías de activos
- **AssetTransaction** - Compras/ventas de activos
- **Debt** - Deudas
- **IncomeType** / **Category** - Tipos de ingreso/gasto

## Notas
- Proyecto Kotlin Multiplatform (Android + iOS)
- DI con Koin: módulos separados (PlatformModule, DatabaseModule, RepositoryModule, UseCaseModule)
- Navigation: Navigation Compose 2.8.0-alpha10