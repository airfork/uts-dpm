# Audit Triage Ledger - 2026-05-01

## Source

- Audit document: `docs/reviews/2026-05-01-code-quality-audit.md`
- Backend branch: `codex/backend-audit-triage`
- Frontend companion branch: `codex/frontend-post-user-actions`

## Current State

The audit document is the original findings list. This ledger is the current
status source for what has been fixed, what is still open, and where to resume.

## Completed

| Area | Backend commit | Frontend commit | Notes |
| --- | --- | --- | --- |
| Assistant docs consolidation | `8ce3f62` | n/a | `AGENTS.md` is canonical; `claude.md` points to it. |
| Compose/local DB setup and state hardening | `e7e4f44` | n/a | Added local migration support and applied it to the live compose DB. |
| Point balance transitions | `e7e4f44` | n/a | Deltas now derive from old/new effective contribution. |
| Autogen failure/idempotency/concurrency handling | `e7e4f44` | n/a | Added submitted date and uniqueness behavior. |
| Temporary password randomness and reset rollback | `e7e4f44` | n/a | Uses secure randomness; rollback path added for reset delivery failure. |
| User email side-effect routes use POST | `b96f5d2` | `c2a196f` | Frontend service calls updated. |
| Async executor registration | `270f85f` | n/a | Executor bean moved into Spring configuration path. |
| DPM list stale-order visibility and active filtering | `9e8e5b9` | n/a | Active groups/DPMs are preserved when saved order is stale. |
| `DpmOrder.equals()` cast | `1df5f01` | n/a | Entity equality regression test added. |
| DPM pagination validation | `00ef0d5` | n/a | Controller validation rejects invalid page/size. |
| Spreadsheet export cleanup and media type | `ddd6128` | n/a | Temp files are deleted and XLSX content type is returned. |
| Production Swagger UI config | `a00a280` | n/a | Prod disables Swagger UI in addition to API docs. |
| When2Work parsing errors | `e7d8376` | n/a | Empty/malformed assigned-shift responses are wrapped with context. |
| Manual DPM creation by driver ID | `89e1309` | `efbb238` | Backend requires `driverId`; frontend carries selected driver ID. |
| Strict DPM date/time parsing | `5227e90` | n/a | Blank parse inputs now fail instead of defaulting to current clock. |
| User manager assignment by ID | `8047347` | `b92a923` | User create/update now resolves managers by stable manager ID and returns manager options as `{id, name}` DTOs. |
| SQL bootstrap smoke coverage | `2ef20b8` | n/a | Fresh Postgres init now runs under Testcontainers with Hibernate validation; the local migration helper lives outside the init script directory. |
| Java 21 toolchain enforcement | `3897530` | n/a | Added `.sdkmanrc` and Maven Enforcer so Java 25 is rejected and Java 21 is the documented local path. |
| `User` entity equality | `3bbc2a7` | n/a | `User` now follows the Hibernate-safe persisted-ID equality pattern used by the other entities. |
| DPM DTO mapper null handling | `0a6b10e` | n/a | Audited DPM DTO mappers now report contextual row/field errors instead of throwing anonymous `NullPointerException`s from `!!`. |
| Active W2W color uniqueness | `73ec0e8` | n/a | Added a Postgres partial unique index so only one active DPM can own a non-null W2W color; inactive duplicates remain allowed. |
| Tokenized password reset flow | `3f155db` | `a41ae50` | Admin reset now emails a one-time token link instead of a temporary password; backend stores only token hashes and the frontend exposes `/passwordReset?token=...`. |
| PRAD deployment SQL script | current branch | n/a | Added one consolidated deployment script at `db_scripts/deployment/20260501_backend_audit_prad.sql`; it covers the audit PR schema changes and can be run with `psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f db_scripts/deployment/20260501_backend_audit_prad.sql` or through the gated Heroku one-off migration runner when local `psql` is unavailable. |

## Open Items

No open audit items remain from the 2026-05-01 audit triage queue. Future work
should come from new findings, product decisions, or follow-up review rather than
this audit's original open list.

## Verification Snapshot

Most recent backend verification:

```bash
./mvnw verify
```

Result: 136 tests passing, 0 failures/errors, coverage checks met. This run used Java 21.0.6.

Most recent frontend verification:

```bash
npm run typecheck
npm run lint
npm run format:check
npm run test:headless
npm run build
```

Result: typecheck, lint, and Prettier checks pass; 770 headless browser tests
pass. Production build passes with the existing `edit-dpms.component.css`
budget warning.

## Environment Notes

- Docker is required for backend integration tests and compose DB checks.
- If the default shell points at a newer JDK, run `sdk env` before Maven commands.
  Maven Enforcer now rejects non-Java-21 runtimes during `validate`.
- Do not push these branches unless the user explicitly asks.
- PRAD deployment should run `db_scripts/deployment/20260501_backend_audit_prad.sql`
  before starting the backend version from this PR. If local `psql` is not
  available, use the Heroku one-off command documented in `README.md`.
