 #! /bin/bash
#
# Thomas Freese
#

HOST="http://localhost:8081/auth"
URL_TOKEN="$HOST/oauth/token"

#-H "Content-Type: application/x-www-form-urlencoded" 
# "client_id=my-client-id&client_secret=my-secret&grant_type=password&username=user&password=pw"

#curl -v -X POST -u 'my-client-id:my-secret' -d "grant_type=password&username=user&password=pw" "$URL_TOKEN"
curl -v -X POST -u 'my-client-id:my-secret' -d grant_type=password -d username=user -d password=pw "$URL_TOKEN"
echo

TOKEN="e5b40eed-8bf8-430a-8eef-9a540f5afbe6"
curl -v -X GET -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/principal"
echo

#curl -H "Accept: application/json" my-client-with-secret:secret@localhost:8080/oauth/token -d grant_type=client_credentials
#curl -H "Authorization: Bearer $TOKEN" localhost:8080/

#curl -H "Authorization: Bearer a503faf9-45b5-4fec-8334-337284a66ea4" http://localhost:9001/rest/v1/electronics/custoers/current
