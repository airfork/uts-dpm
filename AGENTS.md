# Agent Instructions

Use your superpowers when possible.

This file is the canonical project context for assistants working in this repository.
Other assistant-specific files should point here instead of duplicating the same guidance.

## Project Overview

UTS DPM is a Kotlin/Spring Boot backend for Driver Performance Management. It manages users, DPM records, DPM types/groups, daily autogen submissions, email notifications, and When2Work shift integration.

Current stack:

- Kotlin on Spring Boot 4.0.1
- Java 21 target/runtime
- Maven wrapper
- PostgreSQL 16 for local development and tests
- JWT authentication
- Freemarker email templates sent through Mailgun

## Architecture

The main flow is conventional Spring layering:

```text
controllers -> services -> repositories -> entities
     |
    dtos
```

Important services:

| Service | Purpose |
| --- | --- |
| `AutogenService` | Generates and submits DPMs from When2Work shifts. |
| `UserDpmService` | Creates, reads, approves, ignores, and updates user DPM records. |
| `DpmService` | Manages active DPM groups, DPM types, W2W color mappings, and display order. |
| `UserService` | Manages users, managers, point resets, password resets, and user emails. |
| `AuthService` | Handles login, current user lookup, and password-change flow. |
| `TimeService` | Centralizes app dates/times in `America/New_York`. |

## Local Setup

1. Use Java 21.
2. Start Docker Desktop.
3. Copy `.env.example` to `.env` and fill in local values.
4. Start the local database:

```bash
docker compose up -d postgres
```

5. Start the backend with the local profile:

```bash
./start.sh
```

Swagger is available locally at:

```text
http://localhost:8080/swagger-ui/index.html
```

If the local database was partially initialized, reset it and let the init scripts run again:

```bash
docker compose down -v
docker compose up -d postgres
```

Do not commit `.env` or real secrets.

## Configuration

Key properties:

| Property | Purpose |
| --- | --- |
| `app.autogen-mock-enabled` | Selects mock shift provider instead of When2Work API. |
| `app.w2wKey` | When2Work API key. |
| `app.jwt.*` | JWT cookie, expiration, and secret settings. |
| `app.email.*` | Email domain/from/override settings. |
| `app.mailgun_key` | Mailgun API key. |

Profiles:

- `local`: local Postgres, mock autogen enabled by default.
- `prod`: production config; Docker Compose lifecycle disabled.

## Database

Local Docker Compose mounts `db_scripts` into Postgres initialization and uses a named volume for persistent local data. Schema validation is enabled in the app, so local startup fails if SQL bootstrap scripts drift away from JPA entities.

When changing schema or seed data:

- Update `db_scripts/init.sql` ordering if dependencies change.
- Keep foreign keys aligned with JPA relationships.
- Run the fresh DB bootstrap check, not only Hibernate `create-drop` tests:
  `./mvnw test -Dtest=SqlBootstrapSmokeTest`.

## Autogen Flow

1. `GET /api/autogen` calls `AutogenService.autogenDtos()`.
2. `ShiftProvider` fetches shifts from When2Work or mock data.
3. Shifts are filtered by active W2W color mapping, block format (`[...]`), and published status.
4. `Shift` is transformed into `AutogenDpm`.
5. `POST /api/autogen/submit` persists generated DPMs and records an `AutoSubmission`.

Autogen gotchas:

- The block must start with `[` to be processed.
- W2W color mappings are tied to active DPM types.
- `AutoSubmission` is intended to allow one submission per day.
- Local mock mode uses real database users and active W2W colors.

## Testing

Tests use:

- JUnit Jupiter
- Spring Boot Test
- Testcontainers with PostgreSQL 16
- Mockito with `@MockitoBean` and `@MockitoSpyBean`

Common commands:

```bash
./mvnw test
./mvnw test -Dtest=AutogenServiceTest
./mvnw verify
```

`BaseIntegrationTest` provides common repositories and helpers such as:

- `createDpm(name, points, group)`
- `createGroup(name)`
- `createRole(roleName)`
- `createUser(...)`
- `createUserDpm(...)`

Use `entityManager.flush()` and `entityManager.clear()` when a test needs to verify persisted state rather than persistence-context state.

## Codebase Notes

- User DPM times use `HHmm` strings at the API boundary.
- App dates and timestamps should go through `TimeService` when app-local time matters.
- Controller write endpoints should return behavior through services; keep repository access out of controllers.
- Prefer IDs for write APIs when possible. Full-name lookup is fragile when names are duplicated or changed.
- Keep email-sending side effects explicit and testable.
- Be careful with point-balance changes: approval, ignore, unapprove, unignore, and point edits all affect user totals.

## Review Notes

The latest repo-local audit is in:

```text
docs/reviews/2026-05-01-code-quality-audit.md
```

Read it before doing larger cleanup or quality work.

## Plans, Audits, And Status Ledgers

When working through a multi-step plan, audit, review, or cleanup sequence, keep a
repo-local status ledger so another assistant can resume without relying on chat
history.

Use `docs/status/` for these ledgers. Name files by date and task, for example:

```text
docs/status/2026-05-01-audit-triage-ledger.md
```

Each ledger should include:

- The source plan/audit/review document being worked from.
- The active backend branch, and any companion frontend branch.
- Completed items with commit hashes when available.
- In-progress or next recommended item.
- Open items deliberately left for later.
- Verification commands already run and their results.
- Any known local environment caveats, such as required services or noisy-but-passing tool output.

Update the ledger before stopping, before switching tasks, and after each
meaningful commit. If a source audit becomes stale because fixes were committed,
do not rewrite history silently; record the current status in the ledger.
