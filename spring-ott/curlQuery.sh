 #! /bin/bash
#
# Thomas Freese
#

clear;

HOST_AUTH_SERVER="http://localhost:9999/authsrv"
HOST_RES_SERVER="http://localhost:8888/res_srv"

#-H "Content-Type: application/x-www-form-urlencoded" 

# curl $HOST_AUTH_SERVER/oauth/token -u my-app:app-secret -d "grant_type=password&username=admin&password=pw" -H "Content-type:application/x-www-form-urlencoded; charset=utf-8"
# curl -v -X POST $HOST_AUTH_SERVER/oauth/token -u my-app:app-secret -d grant_type=password -d username=admin -d password=pw
# curl -v -X POST $HOST_AUTH_SERVER/oauth/token -u my-app:app-secret -d "grant_type=password&username=admin&password=pw"

RESPONSE=$(curl --silent -X POST $HOST_AUTH_SERVER/oauth/token -u my-app:app-secret -d grant_type=password -d username=admin -d password=pw)
echo
#echo "Response ="
#jq . <<< "$RESPONSE"
#echo $RESPONSE | jq .

echo


ACCESS_TOKEN=$(echo $RESPONSE | jq -r .access_token)
echo "ACCESS_TOKEN = $ACCESS_TOKEN"
echo
echo

# curl -v $HOST_RES_SERVER/secured/user/me -H "Accept: application/json" -H "Authorization: Bearer $ACCESS_TOKEN"
# curl -v $HOST_RES_SERVER/secured         -H "Accept: application/json" -H "Authorization: Bearer $ACCESS_TOKEN"

curl --silent "$HOST_RES_SERVER/secured/user/me" -H "Accept: application/json" -H "Authorization: Bearer $ACCESS_TOKEN"
echo
echo
curl --silent "$HOST_RES_SERVER/secured" -H "Accept: application/json" -H "Authorization: Bearer $ACCESS_TOKEN"
echo

#RESPONSE=$(curl -v -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "$HOST/rest/message/Hello%20T%C3%A4schd")
