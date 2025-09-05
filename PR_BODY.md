This PR bootstraps the autonomous development pipeline:

**Added**
- AutoDev workflow: `.github/workflows/autodev.yml`
- PR-Agent workflow: `.github/workflows/pr_agent.yml`
- Issue template: `.github/ISSUE_TEMPLATE/autodev.yml`
- PR template & CODEOWNERS
- Unified test runner: `ci/run_all_tests.sh`
- Architecture guardrails: `.cursor/rules/architecture.md`

**Notes**
- Do NOT set `AutoDev / implement` as a required status check; it is not PR-triggered.
- Required checks should come from the CI workflow (Lint/Test, Python Lint, Avro Schema, Docker Build, etc).
- After merge:
  1) Add/verify `OPENAI_API_KEY` in repo secrets.
  2) Create an issue with label `auto-dev` OR run **Actions → AutoDev → Run workflow**.
  3) PR-Agent will auto-review subsequent PRs.

ACCEPTANCE CRITERIA
- Files listed above exist in `main`.
- `ci/run_all_tests.sh` is executable (mode 755).
- YAML validated; no tabs; LF line endings.
- Branch pushed and PR opened.
