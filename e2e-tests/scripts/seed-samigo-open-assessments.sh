#!/usr/bin/env bash

set -euo pipefail

if [[ $# -lt 4 || $# -gt 6 ]]; then
  cat <<'EOF'
Usage:
  e2e-tests/scripts/seed-samigo-open-assessments.sh BASE_URL SITE_URL_OR_ID USERNAME PASSWORD [COUNT] [PREFIX]

Example:
  e2e-tests/scripts/seed-samigo-open-assessments.sh \
    http://127.0.0.1:8080 \
    /portal/site/6f0c7b6f-1234-4567-89ab-0123456789ab \
    instructor1 \
    sakai \
    200

Notes:
  - Requires the `sakai-ws` webservices app to be deployed.
  - Requires `webservices.allowlogin=true`.
  - Creates draft assessments via `/sakai-ws/rest/testsandquizzes/createAssessmentFromText`.
  - Publishes each draft through Samigo Settings so the assessments get an
    available date and due date before going live.
EOF
  exit 64
fi

BASE_URL="${1%/}"
SITE_INPUT="$2"
USERNAME="$3"
PASSWORD="$4"
COUNT="${5:-200}"
PREFIX="${6:-samigo-perf-$(date +%Y%m%d%H%M%S)}"

if [[ "$SITE_INPUT" =~ /portal/site/([^/?#]+) ]]; then
  SITE_ID="${BASH_REMATCH[1]}"
  SITE_URL="$SITE_INPUT"
elif [[ "$SITE_INPUT" =~ ^https?:// ]]; then
  if [[ "$SITE_INPUT" =~ /portal/site/([^/?#]+) ]]; then
    SITE_ID="${BASH_REMATCH[1]}"
  else
    echo "Unable to derive site id from URL: $SITE_INPUT" >&2
    exit 65
  fi
  SITE_URL="$SITE_INPUT"
else
  SITE_ID="$SITE_INPUT"
  SITE_URL="/portal/site/$SITE_ID"
fi

if ! [[ "$COUNT" =~ ^[0-9]+$ ]] || [[ "$COUNT" -lt 1 ]]; then
  echo "COUNT must be a positive integer" >&2
  exit 66
fi

LOGIN_URL="$BASE_URL/sakai-ws/rest/login/login"
CREATE_URL="$BASE_URL/sakai-ws/rest/testsandquizzes/createAssessmentFromText"
LOGOUT_URL="$BASE_URL/sakai-ws/rest/login/logout"

echo "Logging into webservices at $LOGIN_URL"
SESSION_ID="$(curl --silent --show-error --fail --get \
  --data-urlencode "id=$USERNAME" \
  --data-urlencode "pw=$PASSWORD" \
  "$LOGIN_URL")"

if [[ -z "$SESSION_ID" ]]; then
  echo "Webservices login did not return a session id" >&2
  exit 67
fi

cleanup() {
  curl --silent --show-error --get \
    --data-urlencode "sessionid=$SESSION_ID" \
    "$LOGOUT_URL" >/dev/null || true
}
trap cleanup EXIT

QUESTION_TEXT=$'1. (1 point)\nWhat is 1 + 1?\n*a. 2\nb. 3'

echo "Creating $COUNT draft assessments in site $SITE_ID with prefix $PREFIX"
for ((i = 1; i <= COUNT; i++)); do
  TITLE="$(printf '%s %03d' "$PREFIX" "$i")"
  RESPONSE="$(curl --silent --show-error --fail --get \
    --data-urlencode "sessionid=$SESSION_ID" \
    --data-urlencode "siteid=$SITE_ID" \
    --data-urlencode "title=$TITLE" \
    --data-urlencode "description=Performance seed assessment $i" \
    --data-urlencode "textdata=$QUESTION_TEXT" \
    "$CREATE_URL")"

  if [[ "$RESPONSE" != "true" ]]; then
    echo "Assessment creation failed for '$TITLE': $RESPONSE" >&2
    exit 68
  fi

  if (( i % 25 == 0 )) || (( i == COUNT )); then
    echo "  created $i / $COUNT"
  fi
done

sleep 2

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"
export PLAYWRIGHT_BASE_URL="$BASE_URL"
export PLAYWRIGHT_HEADLESS="${PLAYWRIGHT_HEADLESS:-true}"
export PLAYWRIGHT_PASSWORD="$PASSWORD"
export SAMIGO_SITE_URL="$SITE_URL"
export SAMIGO_DRAFT_PREFIX="$PREFIX"
export SAMIGO_USERNAME="$USERNAME"
export SAMIGO_EXPECTED_COUNT="$COUNT"

echo "Publishing drafts with availability and due dates via Playwright"
mvn -f e2e-tests/pom.xml -Dtest=SamigoBulkPublishTest#publishDraftAssessmentsMatchingPrefix test

echo "Completed. Published $COUNT assessments in $SITE_URL with prefix $PREFIX"
