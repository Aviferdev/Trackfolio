# Architecture Decision Records (ADR)

Este directorio contiene los **Architecture Decision Records** del proyecto Trackfolio.

Cada ADR documenta una decisión importante de diseño, arquitectura o tecnología,
explicando el contexto, las opciones evaluadas y la justificación de la elección.

## Índice

| ADR | Fecha | Título | Estado |
|-----|-------|--------|--------|
| 0001 | 2026-05-17 | Decisiones tecnológicas iniciales | Aprobado |

## Convenciones

- **Numeración:** secuencial (`0001`, `0002`, ...). No se reutilizan números aunque un ADR quede obsoleto.
- **Nombres de archivo:** `NNNN-slug-descriptivo.md` en minúsculas con guiones.
- **Inmutabilidad:** una vez aprobado, un ADR no se modifica. Si la decisión cambia, se crea un nuevo ADR que referencia y deja obsoleto al anterior.
- **Ubicación:** todos los ADR viven en `docs/adr/`.

## ¿Cuándo crear un ADR?

Crear un ADR cuando la decisión:

- Afecta la arquitectura general del proyecto.
- Implica elegir entre dos o más librerías, frameworks o tecnologías.
- Cambia un patrón de diseño establecido.
- Introduce un trade-off significativo (rendimiento vs. mantenibilidad, etc.).
- Debe ser recordada y entendida por futuros desarrolladores.

**No** crear un ADR para:
- Tareas rutinarias de implementación.
- Corrección de bugs.
- Refactors menores sin cambio de comportamiento.

## ¿Cómo crear un nuevo ADR?

1. Copia `template.md` a `NNNN-slug-descriptivo.md` (el número es el siguiente disponible).
2. Rellena la plantilla con la decisión.
3. Añade una entrada en este índice (`docs/adr/README.md`).
4. Incluye el ADR en el mismo PR que implementa la decisión.
