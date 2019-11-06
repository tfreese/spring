 #! /bin/bash
#
# Thomas Freese
#

clear;

HOST_AUTHSERVER="http://localhost:9999/authsrv"
HOST_RESSERVER="http://localhost:8888"

#-H "Content-Type: application/x-www-form-urlencoded" 

# curl -v -X POST http://localhost:9999/authsrv/oauth/token -u my-app:app-secret -d grant_type=password -d username=admin -d password=pw
# --silent


RESPONSE=$(curl --silent -X POST http://localhost:9999/authsrv/oauth/token -u my-app:app-secret -d grant_type=password -d username=admin -d password=pw)
echo
#echo "Response ="
#jq . <<< "$RESPONSE"
#echo $RESPONSE | jq .

echo


TOKEN=$(echo $RESPONSE | jq -r .access_token)
echo "Token = $TOKEN"
echo
echo

# curl -v http://localhost:8888/secured/user/me -H "Accept: application/json" -H "Authorization: Bearer $TOKEN"
curl --silent "$HOST_RESSERVER/secured/user/me" -H "Accept: application/json" -H "Authorization: Bearer $TOKEN"
echo
echo
curl --silent "$HOST_RESSERVER/secured" -H "Accept: application/json" -H "Authorization: Bearer $TOKEN"
echo

# curl -v http://localhost:8888/unsecured -H "Accept: application/json"


#RESPONSE=$(curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST_RESSERVER/secured/user/me")
#echo $RESPONSE | jq .
#echo
#echo

#RESPONSE=$(curl -v -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/message/Hello%20T%C3%A4schd")
#echo $RESPONSE | jq .
#echo

#curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST_RESSERVER/secured/user/me"
#echo
#echo
#echo