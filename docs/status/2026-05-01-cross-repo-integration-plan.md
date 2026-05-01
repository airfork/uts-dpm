# Cross-Repo Integration Plan - 2026-05-01

## Current Branch State

Backend repo: `/Users/tunji/code/uts-dpm/uts-dpm`

- Active branch: `codex/backend-audit-triage`
- Target base: `origin/main`
- Current relationship to `origin/main`: 28 commits ahead, 0 behind
- Working tree: clean
- Latest verification: `./mvnw verify` passes with 136 tests, 0 failures/errors, coverage checks met

Frontend repo: `/Users/tunji/code/uts-dpm/uts-dpm-frontend`

- API/feature branch: `codex/frontend-post-user-actions`
- Design branch: `feat/design-pass`
- Local integration rehearsal branch: `codex/frontend-integration-audit-design`
- The API/feature and design branches are each 3 commits behind current `origin/main`
- Merge simulation and local integration merge completed without manual conflicts

## Frontend Integration Rehearsal

Local-only worktree:

```text
/Users/tunji/code/uts-dpm/uts-dpm-frontend/.worktrees/integration-audit-design
```

Created from `origin/main`, then merged:

1. `codex/frontend-post-user-actions`
2. `feat/design-pass`

Result:

- No textual merge conflicts
- `npm ci` succeeds
- `npm run typecheck` succeeds
- `npm run lint` succeeds
- `npm run format:check` succeeds
- `npm run test:headless` succeeds with 775 tests
- `npm run build` succeeds

Observed warnings to keep on the follow-up list:

- `npm ci` reports 5 moderate vulnerabilities that need dependency review
- `npm run build` reports CSS budget warnings for `edit-dpms.component.css` and design-preview mock CSS files
- Node is currently v25.9.0 locally, and Angular warns that odd-numbered Node versions are not LTS

## Recommended Merge Strategy

Use separate PRs per repo. The backend and frontend are separate repositories,
and combining them into one conceptual PR would not actually give atomic GitHub
review or merge behavior.

### Backend

Open one backend PR:

```text
codex/backend-audit-triage -> main
```

Use a normal merge commit if possible. The audit ledger references individual
commit hashes, so squash-merging would make those references historical rather
than branch-local commit IDs. If squash merge is required, update the ledger
after the merge.

### Frontend

Open and merge the smaller API/feature PR first:

```text
codex/frontend-post-user-actions -> main
```

Then update the design branch from the new `main` and open the design PR:

```text
feat/design-pass -> main
```

Do not use `codex/frontend-integration-audit-design` as the default PR branch.
It is useful as a local rehearsal branch, but it combines API fixes,
dependency bumps, design previews, and design implementation into one review.
That would make review harder than necessary.

## Recommended Next Work Tracks

### 1. Flyway Migration Track

Branch:

```text
codex/backend-flyway-migrations
```

Rationale:

- The backend already has SQL bootstrap scripts plus ad hoc migration scripts.
- Spring Boot recommends using a higher-level migration tool such as Flyway or
  Liquibase alone instead of mixing basic SQL initialization with migration
  tooling.
- Spring Boot can run Flyway migrations at startup when the Flyway dependency is
  on the classpath.

Suggested implementation direction:

1. Add Flyway dependencies, including the PostgreSQL database module.
2. Move canonical schema migrations under `src/main/resources/db/migration`.
3. Convert existing `db_scripts/scripts/create_*.sql` schema into an initial
   Flyway migration.
4. Convert current audit migration scripts into ordered Flyway migrations.
5. Decide whether seed data belongs in Flyway, a local-only profile migration,
   or Docker-only setup. Keep production schema and demo/test seed data clearly
   separated.
6. Update Testcontainers tests to run migrations instead of Hibernate
   `create-drop` for at least one integration path.
7. Keep `db_scripts` only as a compatibility wrapper or retire it once compose
   and tests use Flyway.

### 2. OpenAPI Contract Track

Branch:

```text
codex/backend-openapi-contract
```

Rationale:

- The backend already uses Springdoc.
- Recent fixes exposed backend/frontend contract drift around write DTOs and
  route methods.
- A generated OpenAPI artifact gives the frontend a concrete contract to type
  against and gives CI something to compare.

Suggested implementation direction:

1. Add a repeatable command to generate `openapi.json` from the backend.
2. Review DTO schemas for naming, nullability, read-only/write-only fields, and
   enum shape.
3. Add CI or a verification command that fails when generated OpenAPI output is
   stale.

### 3. Frontend OpenAPI Types Track

Branch:

```text
codex/frontend-openapi-types
```

Recommended first step:

Use `openapi-typescript` for generated types only, while keeping the existing
Angular services. This is the least disruptive path because the app already has
hand-written `AuthService`, `UserService`, and `DpmService` wrappers with local
notification/error behavior.

Later option:

Evaluate OpenAPI Generator's `typescript-angular` generator if replacing the
hand-written service layer with a generated Angular client becomes worth the
larger refactor. It supports Angular 9.x through 21.x, but it will produce a
client library rather than just DTO types.

## External References Checked

- Spring Boot database initialization and Flyway guidance:
  `https://docs.spring.io/spring-boot/how-to/data-initialization.html`
- OpenAPI Generator `typescript-angular` generator:
  `https://openapi-generator.tech/docs/generators/typescript-angular/`
- `openapi-typescript` CLI:
  `https://openapi-ts.dev/cli`
