#!/usr/bin/env bash
set -euo pipefail

# AutoDev Manual Dispatch Script
# Usage: GH_TOKEN=ghp_xxx ./scripts/gh-dispatch-autodev.sh [scope] [issue_number]

if [[ -z "${GH_TOKEN:-}" ]]; then
    echo "❌ Set GH_TOKEN (Personal Access Token with 'repo' and 'workflow' permissions)" >&2
    exit 1
fi

OWNER="gajdukiewicz00"
REPO="smart-jarvis"
REF="$(git rev-parse --abbrev-ref HEAD)"
WF_FILE="autodev.yml"
API="https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/${WF_FILE}/dispatches"

# Parse arguments
SCOPE="${1:-docs}"
ISSUE_NUM="${2:-}"

echo "🚀 Dispatching AutoDev workflow..."
echo "   Repository: ${OWNER}/${REPO}"
echo "   Branch: ${REF}"
echo "   Scope: ${SCOPE}"
echo "   Issue: ${ISSUE_NUM:-'Manual dispatch'}"

# Validate scope
case "$SCOPE" in
    "docs"|"scripts"|"config"|"all")
        echo "✅ Valid scope: $SCOPE"
        ;;
    *)
        echo "❌ Invalid scope: $SCOPE" >&2
        echo "✅ Allowed scopes: docs, scripts, config, all" >&2
        exit 1
        ;;
esac

# Prepare payload
PAYLOAD=$(cat <<EOF
{
  "ref": "${REF}",
  "inputs": {
    "scope": "${SCOPE}",
    "issue_number": "${ISSUE_NUM}",
    "target_branch": "main"
  }
}
EOF
)

# Dispatch workflow
echo "📡 Sending dispatch request..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${API}" \
  -d "${PAYLOAD}")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

if [[ "$HTTP_CODE" == "204" ]]; then
    echo "✅ Workflow dispatched successfully!"
    echo "🔍 Check the Actions tab: https://github.com/${OWNER}/${REPO}/actions"
    echo "📋 Workflow will create PR with scope: ${SCOPE}"
else
    echo "❌ Failed to dispatch workflow" >&2
    echo "HTTP Code: $HTTP_CODE" >&2
    echo "Response: $BODY" >&2
    exit 1
fi

echo "🎯 Next steps:"
echo "   1. Monitor Actions tab for progress"
echo "   2. Review generated PR carefully"
echo "   3. Merge if changes are safe and correct"
echo "   4. AutoDev PR will auto-delete after merge"
