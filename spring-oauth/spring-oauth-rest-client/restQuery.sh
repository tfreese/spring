 #! /bin/bash
#
# Thomas Freese
#

clear;

HOST="http://localhost:8081/auth"
URL_TOKEN="$HOST/oauth/token"

#-H "Content-Type: application/x-www-form-urlencoded" 

#curl -v -X POST -u 'my-client-id:{noop}my-secret' -d "grant_type=password&username=user&password=pw" "$URL_TOKEN"
#curl -v -X POST -u 'my-client-id:{noop}my-secret' -d grant_type=password -d username=user -d password=pw "$URL_TOKEN"

RESPONSE=$(curl -v -X POST -u 'my-client-id-read:my-secret' -d grant_type=password -d username=user -d password=pw "$URL_TOKEN")
echo
echo "Response ="
#jq . <<< "$RESPONSE"
echo $RESPONSE | jq .

echo

TOKEN=$(echo $RESPONSE | jq -r .access_token)
echo "Token = $TOKEN"
echo
echo

curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/hello"
echo
echo
echo

RESPONSE=$(curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/me")
echo $RESPONSE | jq .
echo
