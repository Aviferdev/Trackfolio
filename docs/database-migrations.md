# Guía de Migraciones de Base de Datos

SQLDelight 2.0.2 gestiona las migraciones del esquema automáticamente mediante
archivos `.sqm` y el objeto `Schema` generado en compilación.

---

## Estado Actual

- **Versión del esquema:** 1
- **Archivos `.sqm`:** Ninguno (esquema inicial, sin datos en producción)
- **Archivos `.sq`:** 31 tablas

---

## Flujo para Añadir una Migración

### 1. Modificar los archivos `.sq`

Edita los archivos en
`composeApp/src/commonMain/sqldelight/es/aviferdev/n3to/data/database/`
para reflejar el nuevo esquema deseado.

Ejemplos de cambios típicos:

```sql
-- Añadir columna
ALTER TABLE TransactionEntity ADD COLUMN new_field TEXT;

-- Nueva tabla
CREATE TABLE NewEntity (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Eliminar tabla
DROP TABLE IF EXISTS ObsoleteEntity;
```

### 2. Crear el archivo `.sqm` de migración

Crea un archivo `N.sqm` en el **mismo directorio** que los `.sq`.
Donde `N` es la versión **desde** la que se migra.

| Situación | Nombre del archivo | Contenido |
|---|---|---|
| Primera migración (v1 → v2) | `1.sqm` | Cambios DDL entre v1 y v2 |
| Segunda migración (v2 → v3) | `2.sqm` | Cambios DDL entre v2 y v3 |
| Tercera migración (v3 → v4) | `3.sqm` | Cambios DDL entre v3 y v4 |

**Ejemplo `1.sqm`** (primera migración):

```sql
-- 1.sqm: Migración de versión 1 a versión 2
-- Añade soporte para foo

ALTER TABLE TransactionEntity ADD COLUMN foo TEXT;

CREATE TABLE NewEntity (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO NewEntity (id, name) VALUES ('default', 'Default');
```

### 3. Incrementar la versión en `build.gradle.kts`

```kotlin
// composeApp/build.gradle.kts
sqldelight {
    databases {
        create("N3toDatabase") {
            packageName.set("es.aviferdev.n3to.data.database")
            version = 2  // ← incrementado
        }
    }
}
```

### 4. Recompilar el proyecto

SQLDelight regenerará el objeto `Schema` incluyendo la migración:

```bash
# Android
./gradlew :composeApp:assembleDebug

# Solo compilar Kotlin (más rápido)
./gradlew :composeApp:compileKotlinAndroid
```

### 5. Verificar

- La app debe abrirse sin errores en dispositivos con BD versión 1.
- Los datos existentes deben conservarse.
- Ejecutar queries de prueba en las tablas afectadas.

---

## Buenas Prácticas

### ✅ Hacer

- Probar migraciones con datos reales (exportar BD antes).
- Añadir un índice en cada nueva FK que se introduzca.
- Usar `ALTER TABLE` sobre `DROP TABLE` + `CREATE TABLE` siempre que sea posible.
- Versionar los `.sqm` junto con los `.sq` en el mismo commit.
- Mantener los `.sqm` inmutables una vez publicados.

### ❌ Evitar

- `DROP COLUMN` no es soportado en SQLite < 3.35.0 (Min SDK 24 = SQLite 3.28+).
  Para eliminar columnas en versiones antiguas, recrear la tabla.
- `ALTER TABLE RENAME COLUMN` requiere SQLite 3.25.0+ (disponible desde Min SDK 24).
- Migraciones destructivas (DROP TABLE) sin copia de seguridad previa.
- Incluir datos semilla en `.sqm` que ya se insertan en `DatabaseInitializer`.

### ⚠️ Atención

- Una vez publicada una versión de la app con las migraciones, **no modificar** los archivos
  `.sqm` de esa versión. Si se necesita corregir algo, crear una nueva migración `.sqm`.
- La migración se aplica **una sola vez** (cuando la app detecta que la BD está en la
  versión anterior). SQLDelight gestiona esto automáticamente.

---

## Integración con DatabaseMigrationHelper

El proyecto incluye `DatabaseMigrationHelper` que proporciona:

```kotlin
// En cualquier ViewModel o UseCase
val migrationHelper: DatabaseMigrationHelper = get()

// Comprobar si hay migración pendiente
if (migrationHelper.isMigrationPending()) {
    // Opcional: hacer backup antes de migrar
    backupManager.performBackup()
}

// Después de abrir la BD correctamente:
migrationHelper.markMigrationComplete()
```

**Uso recomendado:** en el `DatabaseInitializer` o en el init del módulo de BD,
llamar a `markMigrationComplete()` después de la inicialización.

---

## Rollback

SQLDelight **no soporta rollback automático** de migraciones. Para deshacer:

1. Restaurar la copia de seguridad de la BD.
2. Revertir los cambios en `.sq` y eliminar el `.sqm`.
3. Devolver `version` al valor anterior en `build.gradle.kts`.

> **Importante:** Un rollback implica pérdida de datos si ya se ha escrito
> información nueva. Siempre hacer backup antes de migrar.

---

## Referencias

- [Documentación oficial SQLDelight — Migrations](https://cashapp.github.io/sqldelight/2.0.0/migrations/)
- [SQLite ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
