# Code Quality Audit - 2026-05-01

## Scope

Audit of the Kotlin/Spring Boot backend after restoring local startup and Docker database initialization.

Commands run during the audit:

```bash
./mvnw test
./mvnw verify
timeout 60s ./start.sh
docker compose ps --all
docker exec uts-dpm-db psql -U postgres -d uts_dpm -c '\dt'
```

Current verification status:

- `./mvnw verify` succeeds: 95 tests, 0 failures/errors, coverage checks met.
- The local app starts with the `local` profile and reaches `Started UtsDpmBackendApplicationKt`.
- The local Compose database is healthy on Postgres 16 with the expected seed data.
- The test/build output is very noisy on Java 25 because JaCoCo 0.8.12 cannot instrument some Java 24/25-era classes cleanly. The build still exits successfully.

## Executive Summary

The codebase is in a workable state, but there are several correctness and maintainability risks worth triaging before larger feature work. The highest-value fixes are around DPM point-balance state transitions, autogen submit idempotency, password/security behavior, and making the database bootstrap path testable.

## Findings

### P1 - DPM point balances can drift when an approved DPM is later changed

Evidence:

- `UserDpmService.updateDpm()` always applies `dto.points` before approval/ignore handling: `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:97-103`.
- `updateApproved()` adds points only on the first transition to approved, but setting `approved=false` only flips the flag and does not subtract points: `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:115-129`.
- `updateIgnored()` subtracts points when ignoring an approved DPM, but un-ignoring does not restore points: `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:151-166`.

Risk:

The user's `points` balance can become inconsistent with the approved, non-ignored DPM rows. Examples: approve a DPM, change its point value, then the user's total still reflects the old value; approve then unapprove leaves the awarded points in place; ignore then unignore does not restore points.

Recommendation:

Make DPM status changes derive the balance delta from the old effective contribution and the new effective contribution:

```text
oldContribution = if (approved && !ignored) points else 0
newContribution = if (newApproved && !newIgnored) newPoints else 0
user.points += newContribution - oldContribution
```

Add tests for approve, unapprove, ignore, unignore, and point edits after approval.

### P1 - Autogen submit records success even when DPM creation fails

Evidence:

- `AutogenService.autoSubmit()` catches per-DPM exceptions, increments `failCount`, and continues: `src/main/kotlin/com/tunjicus/utsdpm/services/AutogenService.kt:66-76`.
- It saves `AutoSubmission()` after the loop regardless of `failCount`: `src/main/kotlin/com/tunjicus/utsdpm/services/AutogenService.kt:79-85`.
- `alreadyCalledToday()` blocks future submissions once that row is saved: `src/main/kotlin/com/tunjicus/utsdpm/services/AutogenService.kt:100-106`.

Risk:

If every generated DPM fails to save because of a name mismatch, database issue, or validation gap, the system still marks the day as submitted and prevents retry through the normal endpoint.

Recommendation:

Treat failures as part of the transaction result. Either fail the whole submission if any DPM fails, or persist a richer submission record with success/failure counts and allow retry of failed rows. Add tests for all-fail and partial-fail submissions.

### P1 - Autogen submit is not protected against concurrent duplicate submissions

Evidence:

- `autoSubmit()` checks `alreadyCalledToday()` and later inserts a new `AutoSubmission`, but there is no database uniqueness constraint by day and no lock around the check/insert sequence: `src/main/kotlin/com/tunjicus/utsdpm/services/AutogenService.kt:49-85`.
- `auto_submissions` stores only `submitted` timestamp and `auto_submission_id`: `db_scripts/scripts/create_auto_submissions.sql:1-6`.

Risk:

Two managers can submit at nearly the same time. Both requests can pass `alreadyCalledToday()` before either saves the marker, creating duplicate user DPMs for the same shifts.

Recommendation:

Introduce a date-level submission key, a unique constraint, and handle the unique-violation path as a conflict. Consider also moving submission into one transaction that writes the marker and generated DPMs atomically.

### P1 - Temporary password generation uses non-cryptographic randomness

Evidence:

- `generateTempPassword()` uses `charset.random()`: `src/main/kotlin/com/tunjicus/utsdpm/services/UserService.kt:178-181`.
- Generated passwords are emailed to users in `createUser()` and `resetPassword()`: `src/main/kotlin/com/tunjicus/utsdpm/services/UserService.kt:92-107`, `src/main/kotlin/com/tunjicus/utsdpm/services/UserService.kt:148-156`.

Risk:

Kotlin's default random source is not intended for security-sensitive secrets. Temporary passwords are authentication credentials and should use `SecureRandom`.

Recommendation:

Use `java.security.SecureRandom`, increase length, and consider a clearer temporary-password policy. Add a small unit test for length/character class, not deterministic output.

### P1 - Password reset can lock a user out if email delivery fails

Evidence:

- `resetPassword()` encodes and saves the new password before sending the reset email: `src/main/kotlin/com/tunjicus/utsdpm/services/UserService.kt:148-156`.
- Email methods are async and endpoint callers do not observe async failures: `src/main/kotlin/com/tunjicus/utsdpm/services/EmailService.kt:30-44`.
- `sendEmail()` calls Mailgun directly and exceptions would complete the async future exceptionally: `src/main/kotlin/com/tunjicus/utsdpm/services/EmailService.kt:46-62`.

Risk:

The API can report success after replacing the user's password even if the email fails later, leaving the user with no usable credential.

Recommendation:

Prefer reset tokens over emailing raw passwords. If keeping the current flow temporarily, send first through a reliable transactional path or record reset delivery state and expose retry/recovery.

### P2 - GET routes perform state-changing operations

Evidence:

- `GET /api/users/{id}/points` sends an email: `src/main/kotlin/com/tunjicus/utsdpm/controllers/UserController.kt:260-262`.
- `GET /api/users/{id}/reset` resets a user's password: `src/main/kotlin/com/tunjicus/utsdpm/controllers/UserController.kt:289-291`.
- `GET /api/users/points` sends email to all users: `src/main/kotlin/com/tunjicus/utsdpm/controllers/UserController.kt:313-315`.

Risk:

GET requests can be prefetched, retried, cached, crawled, or triggered by previews. A password reset is especially risky as a GET route.

Recommendation:

Move these to POST endpoints. Keep temporary compatibility routes only if the frontend needs a migration window, and make the new endpoints the only documented API.

### P2 - The custom async executor is probably not registered

Evidence:

- `@EnableAsync` is on the application class: `src/main/kotlin/com/tunjicus/utsdpm/UtsDpmBackendApplication.kt:37-40`.
- The `@Bean fun taskExecutor()` method is top-level, not inside a `@Configuration` or component class: `src/main/kotlin/com/tunjicus/utsdpm/UtsDpmBackendApplication.kt:47-56`.

Risk:

Spring processes `@Bean` methods on configuration/component classes. A top-level Kotlin function compiles into a separate generated class, so this executor is unlikely to be registered. `@Async` may fall back to Spring's default executor rather than the bounded pool intended here.

Recommendation:

Move `taskExecutor()` into `UtsDpmBackendApplication` or a dedicated `@Configuration` class. Add a context test asserting the `taskExecutor` bean exists and has the expected thread prefix/capacity.

### P2 - SQL bootstrap is not covered by tests

Evidence:

- Tests use Hibernate `create-drop`: `src/test/resources/application.properties:27-31`.
- Testcontainers starts an empty `postgres:16-alpine` database without applying `db_scripts/init.sql`: `src/test/kotlin/com/tunjicus/utsdpm/config/TestContainersConfig.kt:13-20`.
- The local startup failure fixed in commit `35e9444` came from SQL script order drift, and the existing test suite did not catch it.

Risk:

Entity tests can pass while the local/prod bootstrap scripts fail. This already happened with the `dpms`/`dpm_groups` ordering problem.

Recommendation:

Add a dedicated bootstrap smoke test or script that starts Postgres with `db_scripts` mounted and asserts Hibernate validation can connect to it. Longer term, move schema evolution to Flyway or Liquibase and use the same migrations in tests and local startup.

### P2 - DPM order listing can hide active groups not present in the saved order

Evidence:

- `getDpmGroupList()` iterates only through `groupOrder`; active groups not represented there are never appended: `src/main/kotlin/com/tunjicus/utsdpm/services/DpmService.kt:26-41`.
- The unordered fallback runs only when no order record exists: `src/main/kotlin/com/tunjicus/utsdpm/services/DpmService.kt:28-29`.

Risk:

If the order JSON is stale, manually repaired, partially seeded, or missing a newly active group, the API silently omits that active group from `/api/dpms/list`.

Recommendation:

After applying ordered groups, append any remaining active groups in a deterministic fallback order. Add a test where `dpm_order` omits one active group.

### P2 - Active DPM filtering is inconsistent

Evidence:

- Ordered group DTOs filter active DPMs: `src/main/kotlin/com/tunjicus/utsdpm/services/DpmService.kt:143-153`.
- The unordered fallback maps all `group.dpms`, including inactive ones: `src/main/kotlin/com/tunjicus/utsdpm/services/DpmService.kt:156-157`.

Risk:

If the order row is missing or unreadable, inactive DPMs can become visible again.

Recommendation:

Have `createUnorderedDpmGroupDto()` filter `group.dpms` by `active`, matching the ordered path.

### P2 - `DpmOrder.equals()` is broken

Evidence:

- `DpmOrder.equals()` verifies the other object's effective class is `DpmOrder`, then casts it to `Dpm`: `src/main/kotlin/com/tunjicus/utsdpm/entities/DpmOrder.kt:29-41`.

Risk:

Comparing two distinct `DpmOrder` instances can throw `ClassCastException`. That may surface through collections, AssertJ comparisons, Hibernate internals, or future tests.

Recommendation:

Change `other as Dpm` to `other as DpmOrder` and add a small entity equality test. Consider extracting a common ID-based equality helper for entities that use the same pattern.

### P2 - Pagination accepts invalid or unbounded sizes

Evidence:

- `/api/dpms/approvals` accepts raw `size` and passes it to `PageRequest.of(...)`: `src/main/kotlin/com/tunjicus/utsdpm/controllers/DpmController.kt:126-133`, `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:71-80`.
- `/api/dpms/user/{id}` does the same: `src/main/kotlin/com/tunjicus/utsdpm/controllers/DpmController.kt:191-199`, `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:106-112`.

Risk:

`size=0` or negative values produce 500-style argument errors unless handled elsewhere. Very large sizes can produce expensive queries and responses.

Recommendation:

Clamp or validate page size, for example `1..100`, at the controller boundary using validation annotations or a helper. Add controller tests for invalid and excessive sizes.

### P2 - Spreadsheet generation leaks temp files and does not close the output stream safely

Evidence:

- `saveWorkbook()` creates a temp file and opens `FileOutputStream` without `use`: `src/main/kotlin/com/tunjicus/utsdpm/services/DataGenService.kt:90-97`.
- `returnExcelFile()` reads the temp file into memory but never deletes it: `src/main/kotlin/com/tunjicus/utsdpm/controllers/DataGenController.kt:88-104`.

Risk:

Repeated exports can accumulate files under the system temp directory. If `workbook.write()` throws, the output stream may stay open longer than needed.

Recommendation:

Avoid temp files by writing to `ByteArrayOutputStream`, or delete the temp file after reading it. Use Kotlin `use` blocks for streams/workbooks.

### P2 - User identity is resolved by display name

Evidence:

- `UserDpmService.newDpm()` finds drivers by `dpmDto.driver` full name: `src/main/kotlin/com/tunjicus/utsdpm/services/UserDpmService.kt:33-40`.
- `UserService.updateUser()` finds managers by full name: `src/main/kotlin/com/tunjicus/utsdpm/services/UserService.kt:71-75`.
- `UserRepository.findByFullName()` uses exact `CONCAT(firstname, ' ', lastname) = :name`: `src/main/kotlin/com/tunjicus/utsdpm/repositories/UserRepository.kt:10-13`.

Risk:

Duplicate names, spacing/case differences, and name changes can point work at the wrong user or fail unexpectedly. This also makes API clients depend on presentation text.

Recommendation:

Use user IDs in write DTOs, keep names only for display/search suggestions, and add uniqueness/ambiguity handling if full-name lookup remains as a compatibility layer.

### P2 - Java version is documented but not enforced locally

Evidence:

- Maven declares Java 21: `pom.xml:17-21`.
- Heroku/runtime metadata declares Java 21: `system.properties:1`.
- This laptop is currently running Java 25 for Maven commands, which caused repeated JaCoCo `Unsupported class file major version 68/69` stack traces during otherwise passing test/verify runs.
- The repo has no `.sdkmanrc`, `.java-version`, Maven toolchain config, or Maven enforcer rule.

Risk:

Developers can unknowingly run with unsupported/newer JDKs and get noisy or misleading build output. A future JDK/library combination could turn the current instrumentation warnings into hard failures.

Recommendation:

Add one or more of:

- `.sdkmanrc` with `java=21.0.6-tem`
- Maven Enforcer `requireJavaVersion`
- Maven toolchains documentation
- README setup step that uses `sdk env`

### P3 - Production Swagger UI is not explicitly disabled

Evidence:

- Security permits `/v3/api-docs/**` and `/swagger-ui/**`: `src/main/kotlin/com/tunjicus/utsdpm/security/SecurityAdapter.kt:24-30`.
- Production config disables only `springdoc.api-docs.enabled`: `src/main/resources/application-prod.properties:1-4`.
- Startup logs recommend disabling both `/v3/api-docs` and `/swagger-ui.html`.

Risk:

Even if API docs are disabled, the Swagger UI route may remain exposed as a shell or static page depending on Springdoc behavior. It is low-risk compared with data bugs but easy to close.

Recommendation:

Set `springdoc.swagger-ui.enabled=false` in production and consider profile-specific permit rules for docs.

### P3 - `User` entity equality is mutable and association-heavy

Evidence:

- `User.equals()` compares manager, password, names, flags, points, DPM collections, and role: `src/main/kotlin/com/tunjicus/utsdpm/entities/User.kt:46-66`.
- `User.hashCode()` includes the same mutable fields and lazy associations: `src/main/kotlin/com/tunjicus/utsdpm/entities/User.kt:68-81`.
- Other entities mostly use ID-based equality.

Risk:

`User` instances can change equality/hash while in collections. Comparing users can trigger lazy loads or recurse through associations. This is a common source of subtle persistence/test behavior.

Recommendation:

Move `User` to the same ID-based equality pattern as the other entities, or avoid overriding equality if entity identity is not needed.

### P3 - Many DTO mappers use force-unwrapping on database fields

Evidence:

- `ApprovalDpmDto.from()` force unwraps user, names, type, points, block, and location: `src/main/kotlin/com/tunjicus/utsdpm/dtos/ApprovalDpmDto.kt:20-33`.
- `DpmDetailDto.from()` and `HomeDpmDto.from()` do the same: `src/main/kotlin/com/tunjicus/utsdpm/dtos/DpmDetailDto.kt:24-39`, `src/main/kotlin/com/tunjicus/utsdpm/dtos/HomeDpmDto.kt:16-25`.

Risk:

The schema says many of these are non-null, so this is not immediately wrong. But legacy rows, manual DB repair, or partially-created rows can turn a list endpoint into a 500.

Recommendation:

Use non-nullable entity fields where the schema guarantees non-null, then remove `!!`. For relationships that can be missing due to legacy data, map to explicit fallback or throw a domain-specific exception with row context.

### P3 - `FormatHelpers` silently substitutes current date/time for missing values

Evidence:

- `inboundDpmDate(null/blank)` returns `LocalDate.now()`: `src/main/kotlin/com/tunjicus/utsdpm/helpers/FormatHelpers.kt:26-29`.
- `inboundDpmTime(null/blank)` returns `LocalTime.now()`: `src/main/kotlin/com/tunjicus/utsdpm/helpers/FormatHelpers.kt:31-34`.

Risk:

DTO validation prevents blanks for normal API calls, but this helper can hide bugs in internal callers or future code by creating plausible but wrong timestamps.

Recommendation:

Make parsing strict and let callers decide defaults explicitly. If a default is needed, expose a separately named helper such as `inboundDpmDateOrToday`.

### P3 - `RealShiftProvider` could surface low-context parsing failures

Evidence:

- It reads `response.body?.string()` and passes the result directly to Jackson: `src/main/kotlin/com/tunjicus/utsdpm/services/RealShiftProvider.kt:30-39`.
- Kotlin reports the safe call as unnecessary during compile, so this path is already generating a warning.

Risk:

An empty body, unexpected JSON, or vendor response shape change will throw a generic parsing exception rather than a clear `AutogenException` with enough context.

Recommendation:

Use a non-null body read, validate empty body, wrap Jackson failures in `AutogenException`, and include the request date/status in the message. This also removes the compile warning.

### P3 - Excel responses use generic content type

Evidence:

- `DataGenController.returnExcelFile()` returns `application/octet-stream`: `src/main/kotlin/com/tunjicus/utsdpm/controllers/DataGenController.kt:99-103`.
- The OpenAPI docs describe Excel output: `src/main/kotlin/com/tunjicus/utsdpm/controllers/DataGenController.kt:30-38`.

Risk:

Most browsers still download the file, but clients lose useful type information.

Recommendation:

Use `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

### P3 - Some database constraints live only in application code

Evidence:

- `dpms.w2w_color_id` can be assigned to multiple active DPMs at the database level, while `DpmService.updateDpms()` enforces one color per DPM update request in memory: `src/main/kotlin/com/tunjicus/utsdpm/services/DpmService.kt:56-88`.
- No unique constraints exist in `db_scripts/scripts/create_dpms.sql` for active W2W color ownership.

Risk:

Manual edits, seed scripts, or future write paths can violate assumptions used by autogen color mapping.

Recommendation:

If the rule is truly global, model it in the database. For Postgres, a partial unique index on active rows with non-null `w2w_color_id` is a good fit.

## Suggested Triage Order

1. Fix point-balance transition logic and add state-transition tests.
2. Fix autogen submit idempotency/failure behavior.
3. Replace temporary password generation and revisit reset delivery semantics.
4. Add SQL bootstrap verification or migrate to Flyway/Liquibase.
5. Clean up HTTP side-effect routes and pagination validation.
6. Address infrastructure polish: Java version enforcement, async executor registration, Swagger production config, and spreadsheet temp-file handling.

## Notes

- The setup fix was committed separately as `35e9444 fix local database bootstrap`.
- `AGENTS.md` is currently untracked and was not included in that commit.
