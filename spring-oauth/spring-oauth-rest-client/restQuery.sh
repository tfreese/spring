 #! /bin/bash
#
# Thomas Freese
#

clear;

HOST="http://localhost:8081/auth"
URL_TOKEN="$HOST/oauth/token"

#-H "Content-Type: application/x-www-form-urlencoded" 

#RESPONSE=$(curl -v -X POST -u 'my-client-id-read:my-secret' -d "grant_type=password&username=admin&password=pw" "$URL_TOKEN")
RESPONSE=$(curl -v -X POST -u 'my-client-id-write:my-secret' -d grant_type=password -d username=admin -d password=pw "$URL_TOKEN")
echo
echo "Response ="
#jq . <<< "$RESPONSE"
echo $RESPONSE | jq .

echo

TOKEN=$(echo $RESPONSE | jq -r .access_token)
echo "Token = $TOKEN"
echo
echo

curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/message"
echo
echo
echo

RESPONSE=$(curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/me")
echo $RESPONSE | jq .
echo
echo

RESPONSE=$(curl -v -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/message/Hello%20T%C3%A4schd")
echo $RESPONSE | jq .
echo

curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/message"
echo
echo
echo