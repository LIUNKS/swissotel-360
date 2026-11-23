# Swissotel 360 — Microservices Platform

Multi-module Maven monorepo (Java 25, Spring Boot 4.1.1, Spring Cloud 2025.1.5) implementing an Hexagonal/DDD hotel management platform routed through an API Gateway over PostgreSQL 17 (schema-per-service) and RabbitMQ 4.

## Architecture & Services

| Module / Container | Port | PostgreSQL Schema | Responsibilities |
| :--- | :---: | :---: | :--- |
| `gateway` (`swissotel-api-gateway`) | `8080` | — | Centralized CORS, edge routing, aggregated OpenAPI docs, blocks `/internal/**` (`403`) |
| `services/iam-service` | `8081` | `iam` | Users, roles, permissions, JWT issuance & auth cookie/bearer flow |
| `services/guests-service` | `8082` | `guests` | Guest registry, identity document validation (`DNI`, `PASSPORT`, `CE`), IAM account linking |
| `services/rooms-service` | `8083` | `rooms` | Room types, physical rooms, seasonal rates (`btree_gist`), quotes, room availability |
| `services/reservations-service` | `8084` | `reservations` | Reservation lifecycle, anti-overbooking advisory locks, hold expiration |
| `services/operations-service` | `8085` | `operations` | Stays, check-in/out Saga compensation, digital keys, service requests, RabbitMQ publisher |
| `services/billing-service` | `8086` | `billing` | Folios, charges, payments, invoices/receipts, RabbitMQ consumer (`swissotel.events` + DLQs) |
| `libs/shared-kernel` | — | — | Shared DDD base aggregates, `Money`, `Result`, `GlobalExceptionHandler`, JPA naming strategy |
| `libs/shared-security` | — | — | Stateless JWT parser, servlet filter, 401/403 security handlers, JWT forwarding interceptor |

### Communication & ID Contract
- **External public API (`/api/v1/**`):** Routed solely through the API Gateway (`:8080`). Public payloads and paths expose exclusively `UUID` public identifiers (or catalog `code` values such as `DNI`, `credit_card`, `room_service`), never internal database `Long` IDs.
- **Synchronous internal calls (`/internal/**`):** Service-to-service Spring `@HttpExchange` / `RestClient` calls forwarding caller JWT context; `/internal/**` is rejected (`403 FORBIDDEN`) at the API Gateway edge.
- **Asynchronous domain integration:** `operations-service` publishes `stay.checked-in`, `stay.checked-out`, and `service-request.completed` events to topic exchange `swissotel.events`; `billing-service` consumes durable queues with retry + DLQ (`swissotel.events.dlx`) and idempotent event tracking.

## Quickstart with Docker Compose

1. Copy `.env.example` if you want custom local overrides (all Compose defaults work out of the box):
   ```bash
   cp .env.example .env
   ```
2. Build and start all 9 containers (`postgres`, `rabbitmq`, 6 microservices, and `api-gateway`):
   ```bash
   docker compose up -d --build
   ```
3. Useful URLs once healthy:
   - **Unified Swagger UI:** http://localhost:8080/swagger-ui.html
   - **Gateway Health:** http://localhost:8080/actuator/health
   - **RabbitMQ Management:** http://localhost:15672 (default user/pass: `swissotel` / `swissotel_dev_password`)

## End-to-End Smoke Verification

After `docker compose up -d --build` reports all services healthy, run the automated E2E journey (login -> guest -> reservation -> check-in -> async RabbitMQ folio -> billable service request -> async charge -> payment -> receipt -> check-out, asserting zero raw `Long` IDs in every response):

- **Windows (PowerShell):**
  ```powershell
  ./scripts/e2e-smoke.ps1
  ```
- **Linux / macOS / Git Bash:**
  ```bash
  chmod +x ./scripts/e2e-smoke.sh && ./scripts/e2e-smoke.sh
  ```

## Local Maven Build & Unit/Slice Tests

Requires JDK 25:

```bash
./mvnw clean test
./mvnw package -DskipTests
```
