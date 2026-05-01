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

## Open Items

Recommended next order:

1. Enforce Java 21 locally with `.sdkmanrc`, Maven Enforcer, or toolchain docs.
2. Clean up `User` entity equality.
3. Reduce DTO mapper force unwraps by making guaranteed fields non-nullable or adding explicit fallback/error behavior.
4. Add a database-level partial unique constraint for one active DPM per W2W color.
5. Revisit password reset design with reset tokens instead of emailed temporary passwords.

## Verification Snapshot

Most recent backend verification:

```bash
./mvnw verify
```

Result: 125 tests passing, 0 failures/errors, coverage checks met.

Most recent frontend verification:

```bash
npm run typecheck
npm run lint
npm run test:headless
```

Result: typecheck and lint pass; 761 headless browser tests pass.

## Environment Notes

- Docker is required for backend integration tests and compose DB checks.
- The current local JDK is newer than the project target, so Maven output is
  noisy with JaCoCo instrumentation warnings. The backend build still exits
  successfully.
- Do not push these branches unless the user explicitly asks.
