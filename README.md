# BlockchainUZ — Backend

Spring Boot service that monitors the **Ethereum Sepolia** testnet, persists blocks /
transactions / wallet balances into PostgreSQL, and exposes a paginated REST API plus
CSV/JSON report exports. Built around a strict three-layer architecture
(Access → Business Logic → Reporting) so that the blockchain client is swappable and the
business logic is independently testable.

> Monorepo siblings:
> - Frontend: [`../BlockchainUZ_frontend`](../BlockchainUZ_frontend/README.md) (Next.js 16)
> - Project board: [ProgramistUZ · Project 3](https://github.com/orgs/ProgramistUZ/projects/3)
> - Postman collection: [`README_POSTMAN.md`](./README_POSTMAN.md)
> - Final project report: [`../Finalna wersja projektu.md`](../Finalna%20wersja%20projektu.md)

---

## Features

- **Web3j-backed blockchain client** — `CryptoNodeClient` interface with a `Web3jCryptoNodeClient`
  implementation. JSON-RPC over HTTPS, configurable timeouts, automatic wei → ETH conversion,
  custom `CryptoNodeException` for transparent error propagation.
- **Scheduled sync service** — `BlockchainSyncService` runs every 60 s (configurable),
  batches 10 blocks per iteration, deduplicates against the database, and updates wallet
  balances for every address that appears in the synced block.
- **Hybrid data strategy** — wallet/balance lookups hit the DB first and fall back to a live
  blockchain query if the address has not been indexed yet.
- **REST API** — paginated, sortable endpoints for blocks, transactions, wallets, sync
  status and aggregated reports. Full Swagger / OpenAPI 3 spec at `/swagger-ui.html`.
- **Report exports** — `/api/reports/stats`, `/api/reports/volume`, `/api/reports/top-addresses`,
  plus streaming CSV / JSON exports at `/api/reports/export/{csv,json}`.
- **Spring Security** — admin-only mutating sync endpoints (`/api/sync/trigger`,
  `/api/sync/range`); JDBC-backed sessions.
- **Global error handling** — `GlobalExceptionHandler` returns structured `ErrorResponseDTO`
  payloads with stable error codes.

---

## Tech stack

| Layer       | Choice                                                         |
| ----------- | -------------------------------------------------------------- |
| Language    | Java 25 (Gradle toolchain)                                     |
| Framework   | Spring Boot 4.0.5 (Web MVC, Data JPA, Security, Session JDBC)  |
| Blockchain  | Web3j 4.12.3 (Ethereum JSON-RPC)                               |
| Database    | PostgreSQL 16 (via `docker-compose.yml`)                       |
| Build       | Gradle Kotlin DSL (wrapper included)                           |
| API docs    | springdoc-openapi 3.0.2 (Swagger UI)                           |
| Tests       | JUnit 5, Spring Boot Test, MockMvc, Spring Security Test       |
| Tooling     | Lombok, Spring Boot DevTools, REST Docs (Asciidoctor)          |

---

## Architecture

The backend strictly follows the three-layer model required by the academic project brief.

```
┌────────────────────┐  Web3j JSON-RPC   ┌─────────────────────────┐
│ Ethereum Sepolia   │◄──────────────────│ Access Layer            │
│ (publicnode.com)   │                   │ client/                 │
└────────────────────┘                   │  CryptoNodeClient (IF)  │
                                         │  Web3jCryptoNodeClient  │
                                         └────────────┬────────────┘
                                                      │ DTOs
                                                      ▼
┌────────────────────┐                   ┌─────────────────────────┐
│ PostgreSQL         │◄──── JPA ─────────│ Business Logic Layer    │
│ blocks / tx /      │                   │ service/                │
│ wallets / users    │                   │  BlockchainSyncService  │
└────────────────────┘                   │  Block/Tx/Wallet/Report │
                                         └────────────┬────────────┘
                                                      │ DTOs
                                                      ▼
                                         ┌─────────────────────────┐
                                         │ Reporting Layer         │
                                         │ controller/ + dto/      │
                                         │  REST + CSV/JSON export │
                                         └─────────────────────────┘
```

| Layer            | Package                                  | Responsibility                                                                                            |
| ---------------- | ---------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| Access           | `org.example.blockchainuz.client`        | JSON-RPC communication via Web3j; map `EthBlock` / `Transaction` to internal DTOs; wei → ETH conversion. |
| Business Logic   | `org.example.blockchainuz.service`       | Scheduled sync, batch processing, statistics, balance aggregation, rate-limit handling, deduplication.    |
| Reporting        | `org.example.blockchainuz.controller`    | REST endpoints, Swagger annotations, exception translation, CSV / JSON exports.                          |
| Persistence      | `org.example.blockchainuz.{entity,repository,mapper}` | JPA entities, Spring Data repositories, hand-written mappers between entities and DTOs.       |
| Configuration    | `org.example.blockchainuz.config`        | `Web3jConfig` (RPC + OkHttp), `SecurityConfig`, `CorsConfig`, `OpenApiConfig`.                            |

Why this matters: the `CryptoNodeClient` interface keeps Web3j types out of the business
layer, which (a) makes the sync service unit-testable with simple mocks, and (b) lets us
swap the provider (Web3j → ethers-bridge → multi-chain adapter) without touching anything
above the `client/` package.

---

## Getting started

### Prerequisites

- Java 25 (the Gradle toolchain will download a matching JDK automatically)
- Docker & Docker Compose (for PostgreSQL)

### Run the database

```bash
docker compose up -d
```

This starts PostgreSQL 16 on `localhost:5432` with the schema/credentials wired into
`src/main/resources/application-dev.properties`.

### Run the application

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The service listens on `http://localhost:8080`. Wait ~10 s for the initial sync delay and
then check `/api/sync/status` to confirm blocks are flowing in.

### API documentation

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Postman collection: see [`README_POSTMAN.md`](./README_POSTMAN.md)

### Tests

```bash
./gradlew test                            # unit tests only (JUnit 5)
./gradlew test -PincludeIntegration       # include @Tag("integration") classes
./gradlew bootJar                         # production jar
```

Unit tests live in `src/test/java/.../{controller,service,client}/` and follow the project's
testing convention: every business-logic and controller class has a dedicated `*Test`. The
single integration test (`Web3jCryptoNodeClientIT`) is gated behind the `integration` JUnit
tag so the default `./gradlew test` run never hits the live Sepolia RPC.

---

## Configuration

All settings come from `src/main/resources/application-{dev,prd}.properties`. The most
important keys:

| Property                            | Default                                              | Notes                                                  |
| ----------------------------------- | ---------------------------------------------------- | ------------------------------------------------------ |
| `blockchain.rpc-url`                | `https://ethereum-sepolia-rpc.publicnode.com`        | Override via env to use Alchemy / Infura.              |
| `blockchain.api-key`                | _(empty)_                                            | Appended to the URL when set (premium RPC providers).  |
| `blockchain.connection-timeout`     | `30`                                                 | Seconds. Sepolia under load can exceed the default 10. |
| `blockchain.read-timeout`           | `30`                                                 | Seconds.                                               |
| `blockchain.sync.enabled`           | `true`                                               | Disable to stop the scheduler entirely.                |
| `blockchain.sync.interval`          | `60000`                                              | Milliseconds between sync iterations.                  |
| `blockchain.sync.batch-size`        | `10`                                                 | Maximum blocks fetched per iteration.                  |
| `blockchain.sync.start-block`       | `1`                                                  | First block to consider when the DB is empty.          |
| `spring.datasource.url`             | `jdbc:postgresql://localhost:5432/blockchainuz`      | Match the docker-compose service.                      |
| `spring.jpa.hibernate.ddl-auto`     | `update`                                             | Dev only — production migrations should be explicit.   |

> The free public RPC works for development but rate-limits aggressively under sustained
> load. For demos longer than a few minutes, set `blockchain.rpc-url` /
> `blockchain.api-key` to an Alchemy or Infura endpoint.

---

## REST API surface

All endpoints are documented through Swagger; the highlights are:

| Method + Path                                       | Purpose                                                 |
| --------------------------------------------------- | ------------------------------------------------------- |
| `GET  /api/blocks`                                  | Paginated, sortable list of indexed blocks.             |
| `GET  /api/blocks/latest`                           | Most recent indexed block.                              |
| `GET  /api/blocks/hash/{hash}`                      | Block by hash, embeds its transactions.                 |
| `GET  /api/blocks/number/{n}`                       | Block by number, embeds its transactions.               |
| `GET  /api/blocks/number/{n}/{previous,next}`       | Boundary-aware neighbour lookups.                       |
| `GET  /api/transactions`                            | Paginated transactions.                                 |
| `GET  /api/transactions/{hash}`                     | Single transaction.                                     |
| `GET  /api/transactions/search`                     | Filter by `hash`, `blockNumber`, `status`, `address`.   |
| `GET  /api/wallets/{address}`                       | Wallet info; falls back to live blockchain on miss.     |
| `GET  /api/wallets/{address}/balance`               | Live (or cached) balance in ETH.                        |
| `GET  /api/wallets/{address}/transactions`          | Wallet transaction history (paginated).                 |
| `GET  /api/sync/status`                             | Sync lag, latest indexed block, scheduler state.        |
| `POST /api/sync/trigger`                            | Manually trigger a sync iteration. **ADMIN only.**      |
| `POST /api/sync/range?startBlock=&endBlock=`        | Backfill a specific block range. **ADMIN only.**        |
| `GET  /api/reports/stats`                           | Aggregate KPIs (counts, totals, averages).              |
| `GET  /api/reports/volume?period=daily\|weekly`     | Time-bucketed transaction volume.                       |
| `GET  /api/reports/top-addresses?limit=N`           | Most active addresses.                                  |
| `GET  /api/reports/export/csv`                      | Streamed CSV export (filterable by date / address).     |
| `GET  /api/reports/export/json`                     | Streamed JSON export.                                   |

---

## Project layout

```
src/main/java/org/example/blockchainuz/
├── client/                       # Access Layer
│   ├── CryptoNodeClient.java
│   ├── Web3jCryptoNodeClient.java
│   └── dto/                      # BlockResponse, TransactionResponse
├── config/                       # Web3jConfig, SecurityConfig, CorsConfig, OpenApiConfig
├── controller/                   # Reporting Layer (REST)
│   ├── BlockController.java
│   ├── TransactionController.java
│   ├── WalletController.java
│   ├── SyncController.java
│   ├── ReportController.java
│   └── GlobalExceptionHandler.java
├── dto/                          # API DTOs (Block/Transaction/Wallet/Stats/Volume/Error/Paged)
├── entity/                       # JPA entities (Block, Transaction, Wallet, User, enums)
├── mapper/                       # Entity ↔ DTO mappers
├── repository/                   # Spring Data JPA repositories
├── service/                      # Business Logic Layer
│   ├── BlockchainSyncService.java
│   ├── BlockService.java
│   ├── TransactionService.java
│   ├── WalletService.java
│   └── ReportService.java
└── BlockchainUzApplication.java  # @SpringBootApplication + @EnableScheduling

src/main/resources/
├── application.properties        # shared
├── application-dev.properties    # dev profile (local Postgres + Sepolia public RPC)
└── application-prd.properties    # prod profile

src/test/java/org/example/blockchainuz/
├── client/                       # Web3jCryptoNodeClientTest + IT
├── controller/                   # MockMvc tests for every REST controller
└── service/                      # BlockchainSyncServiceTest, WalletServiceTest
```

---

## Engineering practices

- **SOLID** — `CryptoNodeClient` interface (Dependency Inversion); narrowly-scoped services
  (Single Responsibility); the BLL never imports Web3j types (Open/Closed).
- **Clean Code & DRY** — shared `PagedResponseDTO`, hand-written mappers, no business logic
  in controllers, `GlobalExceptionHandler` as a single source of error responses.
- **KISS / YAGNI** — polling instead of WebSockets in the MVP; this is enough for the
  100-block / 10-tx-per-block requirement on Sepolia.
- **Error handling** — `CryptoNodeException` at the access boundary, graceful degradation in
  `updateWalletBalances()` (one bad address never aborts the batch), `Thread.sleep` /
  bounded batch size to respect rate limits.
- **Testing focus** — unit tests concentrate on the Business Logic Layer per the project
  brief. Controllers are covered with MockMvc; the live RPC is exercised by a single
  integration test gated behind the `integration` JUnit tag.

---

## Sprint history

| Sprint        | Output                                                                                  |
| ------------- | --------------------------------------------------------------------------------------- |
| **Sprint 0**  | Repo bootstrap, Gradle + Web3j wiring, docker-compose for Postgres.                     |
| **Sprint 1–2**| Access Layer, scheduled sync, sync controller, Postman collection. See [`docs/SPRINT_1_2_SUMMARY.md`](./docs/SPRINT_1_2_SUMMARY.md). |
| **Sprint 3**  | Reporting Layer: stats, volume, top-addresses, CSV/JSON exports. See [`docs/REPORTING_FEATURE.md`](./docs/REPORTING_FEATURE.md). |
| **Sprint 4**  | Hardening, MockMvc test suite, documentation, presentation.                             |

---

## Future work

- Integrate **JaCoCo** for objective coverage reporting (currently planned, not yet wired
  into `build.gradle.kts`).
- Switch the sync trigger to **WebSocket subscriptions** (`eth_subscribe`) for sub-second
  latency.
- Add a **Redis** cache for hot wallet balances.
- Extract a multi-chain adapter behind `CryptoNodeClient` (Polygon, Arbitrum).
- GitHub Actions CI/CD with automated deployment.

---

## License

See [`LICENSE`](./LICENSE).
