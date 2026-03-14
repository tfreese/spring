#! /bin/bash
#
# Thomas Freese

# Exit, if one Command fails.
set -e

trap bashtrap SIGINT SIGTERM

bashtrap()
{
	echo "Exit"
	exit 1;
}

readonly PW="password"

echo;
echo "####################################################################################################";
echo "Generating a private RSA key in PKCS#8 Format.";
echo "####################################################################################################";

# Deprecated but working
#openssl genrsa -out src/main/resources/certs/private_key.pem 4096;

# Not encrypted key.
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out src/main/resources/certs/private_key.pem;

# Password encrypted key.
#openssl genpkey -algorithm RSA -aes256 -out src/main/resources/certs/private_key.pem -pkeyopt rsa_keygen_bits:4096 -pass pass:"$PW";

echo;
echo "####################################################################################################";
echo "Extract the public key.";
echo "####################################################################################################";

# Not encrypted key.
openssl pkey -in src/main/resources/certs/private_key.pem -pubout -out src/main/resources/certs/public_key.pem;

# Password encrypted key.
#openssl pkey -in src/main/resources/certs/private_key.pem -pubout -out src/main/resources/certs/public_key.pem -passin pass:"$PW";


## With KeyTool.
#rm src/main/resources/certs/keystore.p12;
#
#keytool -genkeypair -alias myKey -keyalg RSA -keysize 4096 -validity 365 \
#  -keystore src/main/resources/certs/keystore.p12 -storetype PKCS12 \
#  -storepass "$PW" -keypass "$PW" \
#  -dname "CN=MyCertificate, O=MyCompany, C=DE";
#
## KeyTool works only with Certificates.
#keytool -exportcert -alias myKey -keystore src/main/resources/certs/keystore.p12 -storepass "$PW" \
#  -file src/main/resources/certs/certificate.pem;
#openssl x509 -inform DER -in src/main/resources/certs/certificate.pem -pubkey -noout > src/main/resources/certs/public_key.pem;
#
## Keytool doesn't export the private Key, so openssl is required.
#openssl pkcs12 -in src/main/resources/certs/keystore.p12 -passin pass:"$PW" -nodes -nocerts \
#    | openssl pkcs8 -topk8 -nocrypt -out src/main/resources/certs/private_key.pem -passin pass:"$PW" ;