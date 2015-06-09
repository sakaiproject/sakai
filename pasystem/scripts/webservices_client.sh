#!/bin/bash

jq '.' /dev/null &>/dev/null

if [ "$?" != "0" ]; then
    echo "Missing dependency: jq"
fi

set -e

USERNAME="admin"
PASSWORD="admin"
HOST="http://localhost:8080"

COOKIES=$(mktemp)

# Log in to Sakai
curl -s -c "$COOKIES" -F_username="$USERNAME" -F_password="$PASSWORD" "$HOST/direct/session/new" &>/dev/null

# Grab a session token for interacting with the PASystem
pasystem_token=$(curl -s -b "$COOKIES" -X POST "$HOST/direct/pasystem-admin/startSession" | jq -r '.session')


# All popups and banners will expire after two minutes.
# end_time = 0 means "no end time"
two_mins_from_now_ms=$[(`date +%s` * 1000) + 120000]

echo
echo "Creating high priority banner delivered to all hosts..."

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fmessage="CRAZED GOAT LOOSE ON CAMPUS" \
     -Fstart_time=0 -Fend_time="$two_mins_from_now_ms" \
     -Fis_active=true \
     -Ftype=high \
     "$HOST/direct/pasystem-admin/createBanner"


echo
echo "Creating medium priority banner delivered to hosts sakaihost1, sakaihost2..."

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fmessage="This host will self-destruct in 2 minutes" \
     -Fstart_time=0 -Fend_time="$two_mins_from_now_ms" \
     -Fis_active=true \
     -Fhosts=sakaihost1,sakaihost2 \
     -Ftype=medium \
     "$HOST/direct/pasystem-admin/createBanner"


echo
echo "Creating popup visible to everyone (an open campaign)..."

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fdescriptor="GOAT WARNING" \
     -Fis_open_campaign="true" \
     -Ftemplate=" <div><h2>Have you seen this goat?</h2><br><img style=\"width: 600px;\" src=\"http://upload.wikimedia.org/wikipedia/commons/b/b2/Hausziege_04.jpg\"></div>" \
     -Fstart_time=0 -Fend_time="$two_mins_from_now_ms" \
     "$HOST/direct/pasystem-admin/createPopup"


echo
echo "Creating popup visible only to admin user..."

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fdescriptor="Hello administrator" \
     -Fis_open_campaign="false" \
     -Fassign_to_users="admin" \
     -Ftemplate=" <div><h2>Hello, Admin</h2></div>" \
     -Fstart_time=0 -Fend_time="$two_mins_from_now_ms" \
     "$HOST/direct/pasystem-admin/createPopup"


# Delete works too
echo
echo "Deleting a popup (which won't exist, but that's OK)"

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fid="db13aae5-c665-4814-89d9-70bbe9c5d59d" \
     "$HOST/direct/pasystem-admin/deletePopup"

echo
echo "Deleting a banner (which also won't exist, but that's OK too)"

curl -w "\n" -s -b "$COOKIES" \
     -Fsession="$pasystem_token" \
     -Fid="db13aae5-c665-4814-89d9-70bbe9c5d59d" \
     "$HOST/direct/pasystem-admin/deleteBanner"




trap "rm -f '$cookies'" EXIT
