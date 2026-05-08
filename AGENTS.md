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

## Guía de Modelos IA para Desarrollo

Antes de ejecutar una tarea, razona qué tipo de trabajo es y usa el modelo recomendado:

### Análisis de Tarea

| Tipo de tarea | Preguntas a responder |
|---------------|----------------------|
| **Código complejo** | ¿Requiere arquitectura, patrones, refactorización grande? |
| **Contexto amplio** | ¿Necesita entender múltiples archivos o proyecto completo? |
| **Código simple** | ¿Es un fix, bug simple, o tarea pequeña? |
| **Razonamiento** | ¿Necesita lógica compleja, algoritmos, matemáticas? |
| **Economía** | ¿Importa más el costo que la máxima calidad? |

### Modelo Recomendado por Tarea

| Tarea | Modelo | Puntuación | Precio | Por qué |
|-------|--------|------------|--------|---------|
| **Código complejo** (arquitectura, patrones, features grandes) | **Kimi K2.6** | 87/100 | ~$0.30/ej | Mejor relación calidad-precio para desarrollo Android completo |
| **Contexto amplio** (múltiples archivos, proyecto grande) | **DeepSeek V4 Pro** | 89/100 | ~$3.14/ej | 1M tokens de contexto, ideal para proyectos grandes |
| **Fix rápido / código simple** | **DeepSeek V4 Flash** | 78/100 | ~$0.01/ej | Barato y rápido para tareas menores |
| **Tareas económicas** con buena calidad | **Qwen3.6 Plus** | 71/100 | ~$0.15/ej | Económico y competente |
| **Proyectos medianos** | **Kimi K2.5** | 69/100 | ~$0.10/ej | Alternativa económica a K2.6 |

### Modelos NO recomendados para desarrollo

- **GLM-5.1**: Cae a 46/100 en tareas de código reales (por debajo de benchmarks)
- **MiniMax M2.7**: 41/100, no bueno para desarrollo

### Flujo de trabajo obligatorio (SIEMPRE seguir)

**ANTES de ejecutar cualquier tarea, siempre hacer:**

1. **Analiza la tarea** - Answer: ¿Qué tipo de trabajo es? (complejo/simple/contexto/razonamiento/economía)
2. **Identifica complejidad** - ¿Cuántos archivos afecta? ¿Requiere arquitectura?
3. **Recomienda modelo** - Según la tabla superior, indica qué modelo usar y por qué
4. **Pide confirmación** - **IMPORTANTE:** Antes de ejecutar, usa la herramienta `question` para dar dos opciones:
   - "Cambiar al modelo" (recomendado): Pausa la tarea y espera a que el usuario ejecute el comando `/models` para cambiar de modelo. Cuando el usuario diga "continuar", ejecuta la tarea.
   - "Mantenerme en el modelo actual": Usa el modelo actual y ejecuta automáticamente
   - Espera la respuesta del usuario antes de proceder.
5. **Ejecuta** - Una vez confirmado, procede con la tarea

**Importante:** Este análisis es obligatorio para CADA tarea, sin excepción. El paso 4 (confirmación) debe hacerse SIEMPRE.

### Precios orientativos (2026)

- Kimi K2.6: $0.95/M input, $4.00/M output
- DeepSeek V4 Pro: $1.74/M input, $3.48/M output
- DeepSeek V4 Flash: $0.14/M input, $0.28/M output
- Qwen3.6 Plus: ~$0.325/M input, ~$1.95/M output

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