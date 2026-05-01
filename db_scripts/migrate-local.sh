#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

docker compose up -d postgres

for _ in {1..30}; do
  if docker compose exec -T postgres pg_isready -q -U postgres -d uts_dpm; then
    break
  fi
  sleep 1
done

if ! docker compose exec -T postgres pg_isready -q -U postgres -d uts_dpm; then
  echo "Postgres did not become ready in time" >&2
  exit 1
fi

shopt -s nullglob
migrations=(db_scripts/migrations/*.sql)

if ((${#migrations[@]} == 0)); then
  echo "No local database migrations found."
  exit 0
fi

for migration in "${migrations[@]}"; do
  echo "Applying ${migration}"
  docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U postgres -d uts_dpm < "$migration"
done
