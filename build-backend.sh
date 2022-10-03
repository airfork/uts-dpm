#!/usr/bin/env bash

set -e

npx nx run uts-dpm-backend:build
java -jar ./apps/uts-dpm-backend/target/uts-dpm-0.0.1.jar
