#!/usr/bin/env bash
# GBL-013 US-5 (wave 2) — CI contract for slider-gradle.
# The BDD/functional shards (Playwright, pgvector) stay on the host adapters;
# this contract covers the fast and unit gates.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT/slider-plugin"

job="${1:-}"
case "$job" in
  fast)
    ./gradlew test --no-daemon --console=plain
    ;;
  test)
    ./gradlew test ktlintCheck --no-daemon --console=plain
    ;;
  *)
    printf 'unsupported job for slider-gradle: %s\n' "${job:-<none>}" >&2
    printf 'supported: fast | test\n' >&2
    exit 2
    ;;
esac
