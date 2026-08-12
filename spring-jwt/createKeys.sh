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

# Prüfung der Eingangsparameter
if [ $# -eq 0 ]; then
	echo "Parameter: DIRECTORY";
	exit 1;
fi

readonly CERT_DIR="$1"

mkdir -p "$CERT_DIR"

echo;
echo "####################################################################################################";
echo "Generating a private RSA key in PKCS#8 Format.";
echo "####################################################################################################";

# Deprecated but working
#openssl genrsa -out "$CERT_DIRs/private_key.pem" 4096;

# Not encrypted key.
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out "$CERT_DIR/private_key.pem";

# Password encrypted key.
#openssl genpkey -algorithm RSA -aes256 -out "$CERT_DIR/private_key.pem" -pkeyopt rsa_keygen_bits:4096 -pass pass:"$PW";

echo;
echo "####################################################################################################";
echo "Extract the public key.";
echo "####################################################################################################";

# Not encrypted key.
openssl pkey -in "$CERT_DIR/private_key.pem" -pubout -out "$CERT_DIR/public_key.pem";

# Password encrypted key.
#openssl pkey -in "$CERT_DIR/private_key.pem" -pubout -out "$CERT_DIR/public_key.pem" -passin pass:"$PW";


## With KeyTool.
#rm "$CERT_DIR/keystore.p12";
#
#keytool -genkeypair -alias myKey -keyalg RSA -keysize 4096 -validity 365 \
#  -keystore "$CERT_DIR/keystore.p12" -storetype PKCS12 \
#  -storepass "$PW" -keypass "$PW" \
#  -dname "CN=MyCertificate, O=MyCompany, C=DE";
#
## KeyTool works only with Certificates.
#keytool -exportcert -alias myKey -keystore "$CERT_DIR/keystore.p12" -storepass "$PW" \
#  -file src/main/resources/certs/certificate.pem;
#openssl x509 -inform DER -in "$CERT_DIR/certificate.pem" -pubkey -noout > "$CERT_DIR/public_key.pem";
#
## Keytool doesn't export the private Key, so openssl is required.
#openssl pkcs12 -in "$CERT_DIR/keystore.p12" -passin pass:"$PW" -nodes -nocerts \
#    | openssl pkcs8 -topk8 -nocrypt -out "$CERT_DIR/private_key.pem" -passin pass:"$PW" ;