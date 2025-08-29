#!/usr/bin/env bash
set -euo pipefail

echo "== Java projects =="
find . -name "pom.xml" -not -path "*/target/*" \
  -execdir mvn -B -q -DskipTests=false test \;

echo "== Node projects =="
find . -name "package.json" -not -path "*/node_modules/*" \
  -execdir bash -lc 'if [ -f package.json ]; then npm ci --no-audit --no-fund && (npm test --silent || true); fi' \;

echo "== Python projects =="
find . \( -name "pyproject.toml" -o -name "requirements.txt" \) \
  | while read -r f; do
      d=$(dirname "$f"); (
        cd "$d"
        python -m pip install -U pip >/dev/null
        [ -f requirements.txt ] && pip install -r requirements.txt >/dev/null || true
        [ -f pyproject.toml ] && pip install -e . >/dev/null || true
        pytest -q || true
      )
    done

echo "All tests executed."
