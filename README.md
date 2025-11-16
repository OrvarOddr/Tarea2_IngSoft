# Mueblería Los Muebles Hermanos

> Proyecto de evaluación de Ingeniería de Software. En este repositorio dejo todo lo necesario para ejecutar, entender y evaluar el backend (y el pequeño frontend estático) que desarrollé para la mueblería “Los Muebles Hermanos”.

## 1. Visión general

- **Stack principal**: Spring Boot 3.5, Java 21, Spring Data JPA, MySQL (H2 en pruebas), JUnit 5.
- **Objetivo**: Gestionar catálogo de muebles, variaciones/estrategias de precio, cotizaciones y ventas asegurando los requisitos de la pauta.
- **Arquitectura**: Hexagonal ligera en capas (Controller → Service → Repository → Entity). DTOs y mappers para desacoplar la API de las entidades JPA.
- **Patrones de diseño**:
  - **Strategy + Factory** para calcular precios con variaciones (`PriceCalculator`, `PriceCalculatorFactory`).
  - **Service Layer** para encapsular reglas de negocio y mantener los controladores livianos.
- **Testing**: JUnit 5 con AssertJ y base H2 aislada. Incluyo pruebas de servicios críticos y de la fábrica de estrategias.

## 2. Requisitos técnicos cumplidos

| Ítem de la pauta | Implementación |
| --- | --- |
| Spring Boot + MySQL | `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, driver MySQL en `pom.xml`. Configuración en `src/main/resources/application.properties`. |
| CRUD catálogo | Entidad `Mueble` con atributos `nombre`, `tipo`, `precioBase`, `stock`, `estado`, `tamano`, `material`. Servicio y controlador para crear, listar, actualizar y cambiar estado (`/api/muebles`). |
| Variaciones | Entidad `Variacion` con `valorAjuste`, `precioStrategyType`, `activa`. API anidada `/api/muebles/{id}/variaciones` para CRUD y servicio que valida pertenencia al mueble. |
| Cotizaciones y ventas | Entidades `Cotizacion` y `CotizacionItem`. Servicio que arma cotizaciones, valida stock, descuenta inventario y mantiene estados `CREADA`, `CONFIRMADA`, `CANCELADA`. |
| Patrones de diseño | Strategy + Factory documentados y usados para el cálculo de precios; capa de servicios como patrón estructural. |
| Testing con JUnit | `MuebleServiceTest`, `CotizacionServiceTest`, `PriceCalculatorFactoryTest` más `MuebleshermanosApplicationTests` para contexto. |
| Repositorio documentado | Este README explica dependencias, configuración, ejecución, patrones y pruebas. |
| Extras | Frontend estático para consumir la API, docker-compose para ambiente completo y `data.sql` con datos iniciales. |

## 3. Dependencias y versiones

| Tipo | Dependencias claves |
| --- | --- |
| Spring | Web, Data JPA, Validation, DevTools (opcional en dev). |
| Base de datos | `mysql-connector-j` (runtime). En pruebas uso H2. |
| Test | `spring-boot-starter-test`, AssertJ (incluido), Mockito (modo subclass). |
| Construcción | Maven Wrapper (`./mvnw`). |

> Nota: configuré Surefire con `-Djdk.attach.allowAttachSelf=true` y un archivo `mockito-extensions/org.mockito.plugins.MockMaker` para evitar problemas con el mock-maker inline bajo JDK 21.

## 4. Configuración de la base de datos

Archivo: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/muebles_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

Se puede sobreescribir con variables de entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO`).  
El script `data.sql` elimina y vuelve a poblar las tablas `mueble`, `variacion`, `cotizacion` y `cotizacion_item` al iniciar.

## 5. Ejecución del proyecto

### 5.1 Con Docker (recomendado)

```bash
docker compose build
docker compose up -d
docker compose logs app -f          # para ver el arranque
```

Servicios:
- Backend: http://localhost:8080
- MySQL: mysql://localhost:3306 (user/password: `muebles_user` / `muebles_pass`)

Para detener todo:

```bash
docker compose down -v
```

### 5.2 Ejecución local

1. Tener MySQL corriendo y crear el esquema `muebles_db` (o ajustar el `application.properties`).
2. Ejecutar:

```bash
./mvnw spring-boot:run
```

La aplicación escucha en `http://localhost:8081` por defecto (puedes sobreescribir con `SERVER_PORT=8080 ./mvnw spring-boot:run`).

### 5.3 Frontend estático (opcionales)

Directorios: `frontend/index.html`, `frontend/catalogo.html`, `frontend/runas.html`, `frontend/tributos.html`, `frontend/app.js`.

Para levantarlo rápido:

```bash
npx serve frontend              # o
python3 -m http.server --directory frontend 5173
```

El frontend asume que la API está en `http://localhost:8081/api` en local; puedo cambiarlo desde consola:

```js
localStorage.setItem("muebleshermanos_api", "https://mi-backend/api");
```


## 6. Arquitectura y módulos

| Capa | Paquete | Descripción |
| --- | --- | --- |
| API (REST) | `cl.ubb.muebleria.muebleshermanos.api.controller` | Controladores REST (`MuebleController`, `VariacionController`, `CotizacionController`). Validación con `@Valid`. |
| DTO/Mapper | `api.dto`, `api.mapper` | DTOs de request/response y mappers para evitar exponer entidades JPA. |
| Servicio | `service` | Reglas de negocio, validaciones de stock, cambios de estado, asociación de variaciones. |
| Dominio | `domain.model`, `domain.enums`, `domain.strategy` | Entidades JPA, enums requeridos por la pauta, estrategias de cálculo de precios. |
| Repositorios | `domain.repository` | Interfaces que extienden `JpaRepository`. |
| Excepciones y errores | `exception`, `api.error` | Excepciones personalizadas y `GlobalExceptionHandler` para respuestas consistentes. |
| Configuración | `config` | `WebConfig` con CORS para el frontend. |

## 7. Patrones de diseño

1. **Strategy Pattern**  
   - Interface `PriceCalculator` define `calculate(basePrice, adjustment)`.  
   - Implementaciones: `NoPriceAdjustmentStrategy`, `AdditivePriceStrategy`, `PercentagePriceStrategy`.  
   - Permite agregar nuevas estrategias sin modificar la lógica de negocio de cotizaciones.

2. **Factory Pattern**  
   - `PriceCalculatorFactory` recibe todas las estrategias via inyección y entrega la correcta según `PriceStrategyType`.  
   - Evita `switch` en el servicio y permite fallback a `NONE`.

3. **Service Layer** (patrón estructural clásico)  
   - Los controladores interactúan solo con servicios (`MuebleService`, `VariacionService`, `CotizacionService`).  
   - Las reglas de negocio (validar stock, estados, pertenencia de variaciones) viven en los servicios.

Cada patrón está documentado en el código con clases claras y también se detalla en este README, cumpliendo la pauta.

## 8. Flujo de negocio principal

1. **Catálogo**  
   - POST `/api/muebles` crea un mueble nuevo.  
   - GET `/api/muebles` lista con filtros por `estado`.  
   - PUT `/api/muebles/{id}` actualiza datos.  
   - PATCH `/api/muebles/{id}/estado?estado=ACTIVO|INACTIVO` cambia estado.

2. **Variaciones**  
   - POST `/api/muebles/{muebleId}/variaciones` crea variaciones con estrategia de precio y estado activo/inactivo.  
   - GET lista variaciones del mueble.  
   - PUT y DELETE permiten mantener el catálogo de variaciones.

3. **Cotizaciones**  
   - POST `/api/cotizaciones` recibe lista de items (mueble + variación + cantidad).  
   - Al crear, usa la `PriceCalculatorFactory` para calcular `precioUnitario` y `subtotal`.  
   - POST `/api/cotizaciones/{id}/confirmar` valida stock, estado del mueble y descuenta inventario.  
   - POST `/api/cotizaciones/{id}/cancelar` cambia a cancelada si aún no se confirma.

4. **Errores controlados**  
   - `ResourceNotFoundException` → 404  
   - `BusinessException` → 400  
   - Validaciones de DTO → 400 con mensajes claros (`GlobalExceptionHandler`).

## 9. Datos de ejemplo

`src/main/resources/data.sql` precarga:
- 8 muebles con distintos tipos, tamaños y estados.
- 12 variaciones (aditivas/porcentaje/none).
- Se limpia la base para evitar duplicados durante el desarrollo.

Esto ayuda a probar manualmente el flujo completo desde el frontend.

## 10. Pruebas automatizadas

Ejecución:

```bash
./mvnw test
```

Aspectos validados:
- `PriceCalculatorFactoryTest`: verifica cada estrategia y el fallback.  
- `MuebleServiceTest`: crear/actualizar y validación de cambio de estado.  
- `CotizacionServiceTest`: cálculo de precios con variaciones, confirmaciones de venta y errores de stock.  
- `MuebleshermanosApplicationTests`: asegura que el contexto Spring carga.  

Las pruebas usan H2 gracias al perfil `test` (`src/test/resources/application.properties`). El reporte se genera en `target/surefire-reports/`.

## 11. Documentación de la API (resumen)

| Método | Endpoint | Descripción |
| --- | --- | --- |
| GET | `/api/muebles?estado=` | Lista muebles (filtra por estado opcional). |
| GET | `/api/muebles/{id}` | Obtiene un mueble con sus variaciones. |
| POST | `/api/muebles` | Crea un mueble. |
| PUT | `/api/muebles/{id}` | Actualiza un mueble. |
| PATCH | `/api/muebles/{id}/estado` | Cambia estado (query param `estado`). |
| GET | `/api/muebles/{id}/variaciones` | Lista variaciones. |
| POST | `/api/muebles/{id}/variaciones` | Crea variación. |
| PUT | `/api/muebles/{id}/variaciones/{variacionId}` | Actualiza variación. |
| DELETE | `/api/muebles/{id}/variaciones/{variacionId}` | Elimina variación. |
| GET | `/api/cotizaciones?estado=` | Lista cotizaciones. |
| GET | `/api/cotizaciones/{id}` | Detalle de cotización. |
| POST | `/api/cotizaciones` | Crea cotización a partir de items. |
| POST | `/api/cotizaciones/{id}/confirmar` | Confirma venta, descuenta stock. |
| POST | `/api/cotizaciones/{id}/cancelar` | Cancela cotización. |

Las respuestas de error siguen el formato:

```json
{
  "timestamp": "2024-11-05T12:34:56",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente para el mueble Mesa Festín Nórdico",
  "path": "/api/cotizaciones/10/confirmar"
}
```

## 12. Notas personales

- Opté por **DTOs tipo record** en Java 21 para reducir verbosidad y mantener validaciones cerca de la API.
- Dejé el **Service Layer** con anotaciones `@Transactional` para asegurar atomicidad en operaciones de negocio.
- Las pruebas de servicio corren en transacciones y usan rollback automático para mantener la base limpia entre métodos.
- El frontend es completamente estático para simplificar la evaluación; consume la API vía fetch y ofrece filtros, estadísticas y gestión de cotizaciones.
