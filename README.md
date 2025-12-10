# Mueblería Los Muebles Hermanos

Backend (Spring Boot) y un frontend estático para la evaluación. Quise dejar algo fácil de levantar, de leer y de probar: API REST, datos de ejemplo, Docker y tests listos.

## Qué hace

- CRUD de muebles con tipo, tamaño, material, precio base, stock y estado.
- Variaciones de precio (ninguna, aditiva o porcentual) aplicadas con Strategy + Factory.
- Cotizaciones y ventas: calcula precios, valida estado/stock y descuenta inventario al confirmar.
- Datos de ejemplo al arrancar y un frontend estático que consume la API.

## Stack y decisiones

- Spring Boot 3.5, Java 21, Spring Data JPA, MySQL (H2 en tests), Maven Wrapper.
- Arquitectura por capas simple (Controller → Service → Repository) con DTOs y mappers para no exponer entidades JPA.
- Strategy + Factory para el cálculo de precios; servicios con transacciones para mantener reglas de negocio en un solo lugar.
- Tests con JUnit 5 + AssertJ en H2; mock-maker inline habilitado para Java 21.

## Cómo correrlo

### Docker (rápido)

```bash
docker compose build
docker compose up -d
docker compose logs app -f
```

Servicios:
- Backend: http://localhost:8081
- MySQL: mysql://localhost:3306 (user/pass `muebles_user` / `muebles_pass`)
- Frontend estático: http://localhost:5173

> El servicio `app` se construye con `backend/Dockerfile`, apuntado desde `docker-compose.yml`.

Para detener y limpiar volúmenes:

```bash
docker compose down -v
```

### Local (sin Docker)

1. Levanta MySQL y crea el esquema `muebles_db` (o ajusta el `application.properties`).
2. Ejecuta:

```bash
./mvnw spring-boot:run
```

Escucha en `http://localhost:8081` (cámbialo con `SERVER_PORT=8080 ./mvnw spring-boot:run`).

### Frontend estático

Los HTML/CSS/JS están en `frontend/`. Para servirlos:

```bash
npx serve frontend
# o
python3 -m http.server --directory frontend 5173
```

La API esperada es `http://localhost:8081/api`. Se puede redefinir en tiempo de ejecución:

```js
localStorage.setItem("muebleshermanos_api", "https://mi-backend/api");
```

## Configuración de BD

Archivo base: `backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/muebles_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
```

Variables de entorno útiles: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO`, `SERVER_PORT`.  
`data.sql` limpia y carga muebles, variaciones y cotizaciones de ejemplo en cada arranque.

## Pruebas

```bash
./mvnw test
```

- `MuebleServiceTest`: crear/actualizar y validación de cambio de estado.
- `CotizacionServiceTest`: cálculo de precios, confirmaciones y errores de stock.
- `PriceCalculatorFactoryTest`: cada estrategia y su fallback.
- `MuebleshermanosApplicationTests`: arranque del contexto.

H2 en perfil de pruebas (`src/test/resources/application.properties`) y mock-maker inline activado en `mockito-extensions/org.mockito.plugins.MockMaker`.

## API en breve

- `GET /api/muebles?estado=` lista el catálogo.
- `POST /api/muebles` crea; `PUT /api/muebles/{id}` actualiza; `PATCH /api/muebles/{id}/estado` cambia estado.
- `POST /api/muebles/{id}/variaciones` crea variaciones; `PUT/DELETE` para mantenerlas.
- `POST /api/cotizaciones` arma cotizaciones; `POST /api/cotizaciones/{id}/confirmar` confirma y descuenta stock; `POST /api/cotizaciones/{id}/cancelar` cancela.

Errores controlados: `BusinessException` (400) y `ResourceNotFoundException` (404), formateados por `GlobalExceptionHandler`.

## Datos y frontend

- `backend/src/main/resources/data.sql` carga 8 muebles y 12 variaciones al iniciar.
- El frontend en `frontend/` consume la API y permite ver catálogo, variaciones, estadísticas y crear/confirmar cotizaciones.
