 #! /bin/bash
#
# Thomas Freese
#

clear;

HOST="http://localhost:8081/auth"
URL_TOKEN="$HOST/oauth/token"

#-H "Content-Type: application/x-www-form-urlencoded" 
# "client_id=my-client-id&client_secret=my-secret&grant_type=password&username=user&password=pw"

#curl -v -X POST -u 'my-client-id:my-secret' -d "grant_type=password&username=user&password=pw" "$URL_TOKEN"
curl -v -X POST -u 'my-client-id:{noop}my-secret' -d grant_type=password -d username=user -d password={noop}pw "$URL_TOKEN"

#TOKEN=$(curl -i -X POST -u 'my-client-id:my-secret' -d grant_type=password -d username=user -d password=pw "$URL_TOKEN" | jq -r .access_token)
echo

TOKEN="30bc6888-6cd2-449a-9a28-917407a04fdf"
echo "Token = $TOKEN"

#curl -v -H "Accept: application/json" "$HOST/rest/hello"
#curl -v -H "Accept: application/json" "$HOST/rest/principal"
echo

curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/hello"
echo
echo

curl -v -H "Accept: application/json" -H "Authorization: Bearer $TOKEN" "$HOST/rest/me"
echo

# echo '{"token":"ac07098ad59ca6f3fccea0e2a2f6cb080df55c9a52fc9d65"}' | jq -r .token
# ac07098ad59ca6f3fccea0e2a2f6cb080df55c9a52fc9d65
