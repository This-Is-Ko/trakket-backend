# Trakket Backend

A sports event tracking API that syncs fixtures and results from external data sources and exposes them via a REST API.

## Stack

- **Java 20** / **Spring Boot 3.5** — REST API framework
- **PostgreSQL 15** — primary database
- **H2** — in-memory database for local development
- **Spring Security + OAuth 2.0 Resource Server** — JWT-based auth (with Google OAuth and password login)
- **WebClient (Reactor Netty)** — asynchronous HTTP client for external API calls
- **AWS S3** — file storage
- **Lombok** — boilerplate reduction
- **Maven** — build tool

## Getting Started

### Prerequisites

- JDK 20+
- Docker Desktop (for PostgreSQL)
- Maven (or use the bundled `./mvnw` wrapper)

### 1. Start PostgreSQL

```sh
docker compose up -d postgres
```

This starts PostgreSQL 15 on port `5432` with credentials from `.env` (or the defaults in `application.properties`).

### 2. Run the Application

```sh
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with the `local` Spring profile. By default it connects to `jdbc:postgresql://localhost:5432/trakket_db`.

### Environment Variables

All configurable via environment variables (shown with defaults):

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/trakket_db` | Database connection string |
| `SPRING_DATASOURCE_USERNAME` | `trakket_admin_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `password` | Database password |
| `MOTORSPORT_SYNC_SEASON` | `2026` | Season year for motorsport data sync |
| `BUCKET_NAME` | `replace` | AWS S3 bucket name |
| `AWS_ACCESS_KEY_ID` | `replace` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | `replace` | AWS secret key |
| `JWT_KEY` | `replace` | JWT signing secret |
| `GOOGLE_CLIENT_ID` | `replace` | Google OAuth client ID |
| `DISCORD_WEBHOOK_URL` | `replace` | Discord webhook for alerting |

### Build & Deploy

See [BUILD.md](./BUILD.md) for packaging, Docker image building, and deployment instructions.

## Features

### Football (SofaScore & Fantasy Premier League)

- **Data Sources:** [SofaScore](https://www.sofascore.com) for fixtures, results, and standings; [Fantasy Premier League API](https://fantasy.premierleague.com/api) for FPL-specific data.
- **Competitions:** Premier League, La Liga, Serie A, Bundesliga, Champions League, Europa League, FA Cup, EFL Cup, World Cup (men's & women's), European Championships (men's & women's), and more.
- **Scheduled Sync:** Automatically syncs fixtures and results twice daily (9:00 and 21:00).
- **Per-competition / per-round sync:** Admin-triggered sync for specific competitions and rounds.

### Motorsport (Jolpi & MotoTiming)

- **Data Sources:** [Jolpi API](https://api.jolpi.ca/ergast/f1) for Formula One (Ergast mirror); [MotoTiming](https://mototiming.live) for MotoGP; [TheSportsDB](https://www.thesportsdb.com) for World Endurance Championship and Formula E.
- **Competitions:** Formula One, MotoGP, World Endurance Championship, Formula E.
- **Scheduled Sync:** Automatically fetches latest race results twice daily (9:00 and 21:00).
- **Initial Import:** Admin-triggered full season schedule + results import per competition.
- **Dynamic Season:** Season is configurable via `MOTORSPORT_SYNC_SEASON` env var.

### Watch Status Tracking

Authenticated users can mark events with a personal watch status, enabling a "watch list" experience across football and motorsport events.

### Statistics

Personalized statistics endpoint that returns counts and summaries scoped to the authenticated user.

### Health Checks

Spring Boot Actuator health endpoint at `/actuator/health` (public).

## API Overview

All endpoints are under `/api/`. Public routes are noted; all others require authentication.

### Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | Public | Login with username/password |
| POST | `/api/auth/google` | Public | Login with Google token |
| POST | `/api/auth/logout` | Authenticated | Logout and clear session |
| GET | `/api/auth/me` | Authenticated | Current user info |

### Football Events

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/football/competitions` | Authenticated | List available competitions |
| GET | `/api/football/events` | Authenticated | Get events (without user watch status) |
| GET | `/api/football/events/{id}` | Authenticated | Get single event |
| GET | `/api/football/events/with-status` | Authenticated | Get events with user's watch status |
| GET | `/api/football/events/status` | Authenticated | Get watch status for one event |
| POST | `/api/football/events/status` | Authenticated | Set watch status for an event |
| POST | `/api/football/events` | Admin | Create a football event |
| DELETE | `/api/football/events/{id}` | Admin | Delete a football event |
| GET | `/api/football/teams` | Authenticated | List all teams |

### Football Sync (Admin)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/football/sync` | Sync all SofaScore events |
| GET | `/api/football/sync/sofascore` | Sync a specific SofaScore competition/round |
| GET | `/api/football/sync/sofascore/all` | Sync all SofaScore events |
| GET | `/api/football/sync/sofascore/competition/init` | Initialise a SofaScore competition's events |

### Motorsport Events

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/motorsport/competitions` | Authenticated | List available competitions (Formula E excluded by default) |
| GET | `/api/motorsport/events/with-status` | Authenticated | Get events with user's watch status |
| POST | `/api/motorsport/events/status` | Authenticated | Set watch status for an event |
| POST | `/api/motorsport/events` | Admin | Create a motorsport event |

### Motorsport Sync (Admin)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/motorsport/sync` | Sync latest race results for all competitions |
| GET | `/api/motorsport/sync/init` | Initialise full season schedule & results for a competition |

### Statistics

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/statistics` | Authenticated | Get user-scoped statistics |

## Access Control

The API uses two-tier access control.

| Scope | Auth Mechanism | Accessible Routes |
|---|---|---|
| **Public** | None | `POST /api/auth/login`, `POST /api/auth/google`, `GET /actuator/health`, `OPTIONS /**` (CORS preflight) |
| **Authenticated** | JWT Bearer token or access-token cookie | All other `/api/**` endpoints |
| **Admin** | `ROLE_ADMIN` authority in JWT | All sync, create, and delete endpoints (marked as Admin in tables above) |

### Authentication Flow

1. A user authenticates via `POST /api/auth/login` (username/password) or `POST /api/auth/google` (Google OAuth ID token).
2. On success, the server returns a JWT access token in the response body and sets it as an HTTP-only cookie.
3. Subsequent requests are authenticated either by the `Authorization: Bearer <token>` header or by the access-token cookie (handled by `AccessTokenCookieFilter`).
4. The `AccessTokenCookieFilter` reads the cookie and populates the `Authorization` header before JWT validation, so clients calling from browsers (where cookie-based auth is more convenient) work seamlessly.
5. Admin-protected endpoints additionally check for the `ROLE_ADMIN` role in the JWT's scope/authorities.

### Error Handling

API errors return consistent JSON error responses via `@RestControllerAdvice` (`GlobalExceptionHandler`). Unauthorized requests return 401 via `JwtAuthenticationEntryPoint`.

## Scheduled Jobs

| Job | Cron | Description |
|-----|------|-------------|
| Football sync | `0 0 9,21 * * *` | Sync fixtures/results from SofaScore |
| Motorsport sync | `0 0 9,21 * * *` | Sync latest race results (F1, MotoGP, WEC) |
