# Claude Code Context

Project context for AI assistants working on this codebase.

## Project Overview

Spring Boot 3.4.5 Kotlin backend for UTS DPM (Driver Performance Management). Manages employee DPMs (performance metrics), integrates with When2Work for shift data, uses JWT auth.

## Key Architecture

```
controllers/ → services/ → repositories/ → entities/
     ↓
   dtos/
```

### Important Services

| Service | Purpose |
|---------|---------|
| `AutogenService` | Generates DPMs from When2Work shifts |
| `UserDpmService` | CRUD for user DPM records |
| `AuthService` | JWT authentication, current user |
| `TimeService` | Timezone-aware dates (America/New_York) |

### Autogen Flow

1. `GET /api/autogen` → `AutogenService.autogenDtos()`
2. Fetches shifts via `ShiftProvider` (real or mock)
3. Filters by: color mapping, block format (`[...]`), published status
4. Transforms `Shift` → `AutogenDpm` → `UserDpm`

## Configuration

### Application Properties

| Property | Description |
|----------|-------------|
| `app.autogen-mock-enabled` | Toggle mock shift provider |
| `app.w2wKey` | When2Work API key |
| `app.jwt.*` | JWT settings |
| `app.email.*` | Email settings |

### Profiles

- `local` - Development with mock autogen, local Postgres
- Default - Production settings

## Testing

### Framework

- JUnit Jupiter + Spring Boot Test
- Testcontainers 1.21.4 (PostgreSQL 16)
- Mockito with `@MockitoBean` / `@MockitoSpyBean`

### Key Patterns

```kotlin
// Mock a bean
@MockitoBean private lateinit var authService: AuthService

// Spy on a bean (partial mock)
@MockitoSpyBean private lateinit var autogenService: AutogenService

// Mock method on spy
doReturn(mockShifts).`when`(autogenService).getAssignedShifts()
```

### Test Data Helpers

`BaseIntegrationTest` provides:
- `createDpm(name, points, group)`
- `createGroup(name)`
- Autowired repositories

### Running Tests

```bash
./mvnw test                              # All tests
./mvnw test -Dtest=AutogenServiceTest    # Single class
```

Requires Docker for Testcontainers.

## Recent Changes

### Autogen Mock Mode (Jan 2025)

Added configurable mocking for the autogen endpoint:

**New Files:**
- `services/ShiftProvider.kt` - Interface
- `services/RealShiftProvider.kt` - When2Work API
- `services/MockShiftProvider.kt` - Uses DB users
- `configs/AutogenConfig.kt` - Bean config + startup warning
- `test/.../models/AutogenDpmTest.kt` - Unit tests for description parsing
- `test/.../services/MockShiftProviderTest.kt` - Tests for mock provider

**Modified:**
- `AutogenService.kt` - Uses injected `ShiftProvider`, added debug logging
- `AppProperties.kt` - Added `autogenMockEnabled`

## Common Tasks

### Add a new DPM type
1. Insert into `dpms` table with `dpm_group_id`
2. Optionally link to `w2w_colors` for autogen

### Debug autogen
1. Set `logging.level.com.tunjicus.utsdpm.services=DEBUG`
2. Check logs for filter counts at each step

### Test with mock data
Set `app.autogen-mock-enabled=true` in properties or use local profile.

## Gotchas

- Time format is `HHmm` (no colons) in `UserDpm`
- Block must start with `[` to be processed by autogen
- `AutoSubmission` tracks daily submission - only one per day
- Tests use `entityManager.flush()` and `clear()` to control persistence context
