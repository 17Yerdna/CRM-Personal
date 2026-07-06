# Arquitectura Hexagonal Objetivo

## Contexto

CRM Personal es una aplicacion desktop local para gestionar contactos, etiquetas jerarquicas, campos dinamicos, timeline de notas/media, exportacion PDF e importacion Excel. La aplicacion usa Java 21, JavaFX, Spring Boot 3, SQLite, Hibernate, PDFBox y Apache POI.

## Regla de dependencia

La arquitectura objetivo aplica Puertos y Adaptadores de forma estricta:

- `domain` no depende de Spring, Jakarta Persistence, Hibernate, JavaFX ni infraestructura.
- `application` orquesta casos de uso mediante puertos de entrada y salida.
- `infrastructure` implementa puertos de salida con Spring Data JPA, SQLite, filesystem, Flyway y seguridad.
- `presentation` implementa adaptadores de entrada con JavaFX/FXML y publica eventos de UI.

## Estructura objetivo

```text
src/main/java/com/crm/personal/
├── Main.java
├── CrmApplication.java
├── domain/
│   ├── contact/
│   │   ├── model/
│   │   ├── port/
│   │   └── event/
│   ├── tag/
│   │   ├── model/
│   │   └── port/
│   ├── dynamicfield/
│   │   ├── model/
│   │   ├── port/
│   │   └── validation/
│   ├── timeline/
│   │   ├── model/
│   │   └── port/
│   ├── media/
│   │   ├── model/
│   │   └── port/
│   └── shared/
├── application/
│   ├── contact/
│   │   ├── command/
│   │   ├── dto/
│   │   ├── port/
│   │   └── service/
│   ├── dynamicfield/
│   ├── timeline/
│   └── shared/
├── infrastructure/
│   ├── config/
│   ├── persistence/
│   ├── filesystem/
│   ├── security/
│   └── transaction/
└── presentation/
    ├── javafx/
    └── controller/
```

## Decisiones clave

- Los IDs de dominio son value objects (`ContactId`, `TagId`, `DynamicFieldId`).
- Los DTOs y comandos de aplicacion son `record` de Java 21.
- Los eventos de UI son `record` y se publican desde adaptadores JavaFX.
- Los mappers JPA viven en infraestructura y convierten JPA entity a dominio puro.
- Las rutas multimedia se persisten como relativas y se resuelven contra `app.data.dir` solo en infraestructura.
- El HTML del timeline se sanitiza mediante un puerto de aplicacion antes de persistir.
- Flyway reemplaza `hibernate.ddl-auto=update` para versionar schema SQLite.

## Plan por fases

1. Preservar estado actual en Git.
2. Introducir documentacion de arquitectura objetivo.
3. Introducir modelo de dominio puro y puertos.
4. Agregar casos de uso de aplicacion.
5. Agregar adaptadores de infraestructura.
6. Reemplazar `PasswordHolder` por bootstrap con bean `MasterPassword`.
7. Desacoplar UI con eventos.
8. Validar EAV con Strategy en dominio.
9. Guardar multimedia con rutas relativas.
10. Sanitizar HTML y manejar errores JavaFX globalmente.
11. Migrar schema a Flyway.
12. Verificar build y subir commits.
