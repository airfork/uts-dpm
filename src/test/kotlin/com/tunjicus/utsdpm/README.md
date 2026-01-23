# Test Suite Architecture

## Overview

The UTS-DPM test suite uses a Modern Layered strategy to balance fast feedback with comprehensive coverage. Service tests run as integration tests with real database access to catch query bugs. Controller tests split into fast unit tests (@WebMvcTest) for logic validation and full integration tests for JWT filter chain verification. This dual approach provides sub-second unit test feedback while maintaining end-to-end confidence. JaCoCo enforces 80% line coverage to prevent regression.

## Architecture

```
BaseIntegrationTest (abstract)
       |
+------+------+------+
|      |      |      |
Service  Controller   Model
Tests    Tests        Tests
  |         |
  |    +----+----+
  |    |         |
  | @WebMvcTest  @SpringBootTest
  | (unit)       (integration)
```

### Test Base Class

`BaseIntegrationTest` provides the single inheritance point for all integration tests. It autowires repositories and exposes helper methods (`createUser()`, `createRole()`, `createDpm()`, `createGroup()`). Changes to this class affect all tests, so additions must be backward-compatible. The class extension pattern was chosen over test fixtures/factories because the infrastructure already existed and is familiar to the team.

### Service Test Strategy

Service tests extend `BaseIntegrationTest` and use real database access. This decision reflects that service layer complexity lies in repository interactions, which are hard to mock correctly. Mocking repositories would miss query bugs (N+1 queries, missing joins, incorrect predicates). The tradeoff is slower tests (~30-45s per run including container startup), but Testcontainers reuses containers across test classes to mitigate this.

### Controller Test Strategy

Controllers use both @WebMvcTest and @SpringBootTest. @WebMvcTest provides fast (<1s) unit tests that verify controller logic without starting the full application. However, it doesn't load the JWT security filter chain, so authorization vulnerabilities could slip through. @SpringBootTest integration tests run the full stack including JWT filters. The duplication (some logic tested twice) is acceptable for critical authentication paths.

## Design Decisions

### 80% Coverage Threshold

Target was user-specified during planning. It balances coverage goals with development velocity. The existing 30% baseline left critical paths underprotected. Higher thresholds (90%+) would force testing trivial code (getters, data class equals/hashCode). Lower thresholds (70%) would leave business logic gaps. 80% covers all service and controller logic without busywork.

### Service Tests as Integration Tests

Services have complex repository interactions (joins, filtering, pagination) that are difficult to mock accurately. Real database access catches:
- Query construction bugs
- N+1 query problems
- Transaction boundary errors
- Entity relationship mapping issues

The alternative (mocking repositories) would test mock behavior, not actual database semantics.

### JaCoCo Exclusions

Coverage excludes `**/config/**` and `**/TestContainersConfig*`. Configuration classes contain framework wiring (no business logic). Testing them would distort metrics without improving production code quality. This follows Maven/JaCoCo standard practice.

### AssertJ for All Tests

AssertJ was already available via `spring-boot-test-autoconfigure`. Some existing tests used `assert()`, others used AssertJ. Standardizing on AssertJ improves readability (fluent assertions are self-documenting) and provides better failure messages. Migration happened in M8 after coverage implementation to avoid delaying new tests.

### Vertical Slice Organization (Auth/User/DPM Stacks)

Test implementation followed vertical slices (M1: AuthService → M4: AuthController) rather than horizontal layers (all services, then all controllers). This enabled parallel work on independent feature stacks. Horizontal organization would create sequential dependencies (can't test UserController until all services done).

## Invariants

### Test Isolation

Each test must clean up its data or use `@Transactional`. Tests cannot depend on execution order (JUnit doesn't guarantee order). Shared state between tests causes flaky failures. Use `entityManager.flush()` and `clear()` to control persistence context for testing state changes.

### Mock Consistency

- **EmailService**: Always mocked to prevent real email sending. Tests verify `emailService.sendWelcomeEmail()` or `sendDpmEmail()` calls via `verify()`.
- **AuthService**: Tests needing authenticated users must mock `getCurrentUser()`. Use `createUser()` helper to create entity, then stub `authService.getCurrentUser()` to return it.
- **JwtProvider**: Mocked in service tests (unit tests don't need real crypto). Real JWT generation only in @SpringBootTest integration tests.

### BaseIntegrationTest Helper Usage

Helper methods (`createUser()`, `createRole()`, etc.) are the standard way to create test entities. Don't create entities manually in test methods. Helpers provide consistent defaults and reduce boilerplate. If helpers grow beyond 500 lines, refactor into trait/mixin pattern.

### Role-Based Access Testing

Tests document existing authorization model (from UserDpmService.kt:75-82):
- ADMIN sees all unapproved DPMs
- MANAGER sees only managed users' DPMs
- DRIVER throws UserNotAuthorizedException when accessing unapproved DPMs

This isn't enforced by types, so tests prevent regression.

## Tradeoffs

### Speed vs Completeness

Unit tests provide fast feedback (<1s). Integration tests provide confidence (full stack). Both are required. Eliminating either sacrifices critical value. The cost is longer CI time and some test duplication in controllers.

### Test Duplication in Controllers

Some controller logic is tested twice (unit + integration). This is acceptable because:
- Unit tests catch logic bugs quickly during development
- Integration tests catch security configuration bugs (JWT filter chain)
- Controllers are thin (most logic in services), so duplication is minimal

### Helper Method Growth

BaseIntegrationTest will accumulate more helpers as test coverage expands. This is acceptable for maintaining consistency across the test suite. Alternative (factory pattern, test fixtures) would require new abstractions and training. Refactor if BaseIntegrationTest exceeds 500 lines.

### Service Integration Tests (Not Pure Unit Tests)

Service tests use real database instead of mocking repositories. This makes tests slower but catches real bugs. Pure unit tests with mocked repositories would be faster but miss query construction issues. For this codebase, repository interactions are complex enough to justify integration approach.

## Test Data Patterns

### User Creation

```kotlin
val user = createUser(
  firstname = "Test",
  lastname = "User",
  email = "test@example.com",
  role = adminRole,          // optional
  manager = managerUser,     // optional
  fullTime = true,           // optional, default true
  points = 0,                // optional, default 0
  changed = false            // optional, default false
)
```

### Authenticated User Mocking

```kotlin
val currentUser = createUser("Test", "User", "test@example.com")
`when`(authService.getCurrentUser()).thenReturn(currentUser)
```

### DPM Creation

```kotlin
val group = createGroup("Test Group")
val dpm = createDpm("Test DPM", points = 10, group = group)
```

## Known Test Behaviors

### 6-Month DPM Visibility Window

Tests document that `UserDpmService.getCurrentDpms()` filters to last 6 months (from UserDpmService.kt:64). Older DPMs remain in database (not deleted) but don't appear in user dashboard. This balances UI relevance with data retention.

### FullTime Transition Point Reset

Tests document that changing `user.fullTime` from false to true resets points to 0 and ignores unapproved DPMs (from UserService.kt:62-69). Part-time point system doesn't apply to full-timers. This prevents misleading UI when employees transition.

### Part-Timer Point Reset Filtering

`UserService.resetPointBalances()` uses repository query that filters by `fullTime=false`. Only part-time employees affected. Full-time compensation model is different. Tests verify this boundary (from UserService.kt:110-117).
