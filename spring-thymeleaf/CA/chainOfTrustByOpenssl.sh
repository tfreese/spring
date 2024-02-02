#! /bin/bash
#
# Thomas Freese
# Erzeugt ein self-signed RootCA und signiert damit weitere Zertifikate.
#
readonly PW="password"
readonly DNAME="CN=Root CA 1,OU=Dev,O=thofre,L=BS,ST=NS,C=DE";

rm -rf "openssl";
mkdir "openssl";

#rm -f *.p12;
#rm -f *.jks;
#rm -f *.crt;
#rm -f *.csr;

# A Self Signed certificate is easy to generate with one command:
# openssl req -newkey rsa:4096 -nodes -keyout key.pem -x509 -days 365 -out openssl/certificate.pem

echo;
echo "####################################################################################################";
echo "Erzeugen eines self signed key pair root CA Zertifikats.";
echo "####################################################################################################";
openssl genrsa -aes256 -passout pass:"$PW" -out openssl/root_ca.key 4096;
openssl rsa -in openssl/root_ca.key -check -noout -passin pass:"$PW";

echo;
echo "####################################################################################################";
echo "Exportieren des CA public Zertifikats als *.crt so das es in TrustStores verwendet werden kann.";
echo "####################################################################################################";
openssl req -x509 -new -key openssl/root_ca.key -passin pass:"$PW" -out openssl/root_ca.crt -days 36500 -sha512 -subj "/CN=Root CA 1"

echo;
echo "####################################################################################################";
echo "Erzeugen eines Server Zertifikats, z.B. server.";
echo "####################################################################################################";
openssl genrsa -aes256 -passout pass:"$PW" -out openssl/server.key 4096;
openssl rsa -in openssl/server.key -check -noout -passin pass:"$PW";

#keytool -genkey -v \
#  -storetype PKCS12 \
#  -keystore keytool/server_keystore.p12 \
#  -storepass "$PW" \
#  -alias server \
#  -dname "CN=Thomas Freese: server, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE" \
#  -keyalg RSA \
#  -keysize 4096 \
#  -validity 36500;

#echo;
#echo "####################################################################################################";
#echo "Erzeugen eines Client Zertifikats, z.B. client.";
#echo "####################################################################################################";
#keytool -genkey -v \
#  -storetype PKCS12 \
#  -keystore keytool/client_keystore.p12 \
#  -storepass "$PW" \
#  -alias client \
#  -dname "CN=Thomas Freese: client, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE" \
#  -keyalg RSA \
#  -keysize 4096 \
#  -validity 36500;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Certificate Signing Requests (CSR) für server.";
echo "####################################################################################################";
openssl req -new -key openssl/server.key -sha512 -out openssl/server.csr -subj "/$DNAME" -passin pass:"$PW"
#openssl req -text -noout -verify -in server.csr

#keytool -certreq -v \
#  -keystore keytool/server_keystore.p12 \
#  -storepass "$PW" \
#  -alias server \
#  -file keytool/server.csr;

#echo;
#echo "####################################################################################################";
#echo "Erzeugen eines Certificate Signing Requests (CSR) für client.";
#echo "####################################################################################################";
#keytool -certreq -v \
#  -keystore keytool/client_keystore.p12 \
#  -storepass "$PW" \
#  -alias client \
#  -file keytool/client.csr;

echo;
echo "####################################################################################################";
echo "Signieren des server-Zertifikats und des -CSR mit dem ROOT CA Zertifikat.";
echo "####################################################################################################";
openssl x509 -req -CA openssl/root_ca.crt -CAkey openssl/root_ca.key -in openssl/server.csr -out openssl/server.pem -days 36500 -CAcreateserial -sha512 -passin pass:"$PW"

#keytool -gencert -v \
#  -keystore keytool/root_ca.p12 \
#  -storepass "$PW" \
#  -alias root_ca_1 \
#  -infile keytool/server.csr \
#  -outfile keytool/server.crt \
#  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
#  -ext EKU="serverAuth" \
#  -ext SAN="DNS:localhost" \
#  -rfc;

#echo;
#echo "####################################################################################################";
#echo "Signieren des client-Zertifikats und des -CSR mit dem ROOT CA Zertifikat.";
#echo "KeyEncipherment for RSA: DHE or ECDHE.";
#echo "####################################################################################################";
#keytool -gencert -v \
#  -keystore keytool/root_ca.p12 \
#  -storepass "$PW" \
#  -alias root_ca_1 \
#  -infile keytool/client.csr \
#  -outfile keytool/client.crt \
#  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
#  -ext EKU="serverAuth" \
#  -ext SAN="DNS:localhost" \
#  -rfc;

#echo;
#echo "####################################################################################################";
#echo "Import des ROOT CA public Zertifikats in den Server TrustStore.";
#echo "####################################################################################################";
#keytool -import -v \
#  -keystore keytool/server_truststore.p12 \
#  -storepass "$PW" \
#  -alias root_ca_1 \
#  -file keytool/root_ca_1_pub.crt << EOF
#ja
#EOF

#echo;
#echo "####################################################################################################";
#echo "Import des ROOT CA public Zertifikats in den Client TrustStore.";
#echo "####################################################################################################";
#keytool -import -v \
#  -keystore keytool/client_truststore.p12 \
#  -storepass "$PW" \
#  -alias root_ca_1 \
#  -file keytool/root_ca_1_pub.crt << EOF
#ja
#EOF

echo;
echo "####################################################################################################";
echo "Create Server KeyStore.";
echo "####################################################################################################";
cat openssl/root_ca.crt openssl/server.pem > openssl/serverca.pem

openssl pkcs12 -export -in openssl/serverca.pem -inkey openssl/server.key -name localhost -passin pass:"$PW" -passout pass:"$PW" > openssl/server_keystore.p12

#keytool -importkeystore -srckeystore KEYSTORE-FILENAME.jks -destkeystore KEYSTORE-FILENAME.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass somepassword \
#-deststorepass somepassword


echo;
echo "####################################################################################################";
echo "Create Server TrustStore.";
echo "####################################################################################################";
openssl pkcs12 -export -in openssl/root_ca.crt -inkey openssl/root_ca.key -name localhost -passin pass:"$PW" -passout pass:"$PW" > openssl/server_truststore.p12

#echo;
#echo "####################################################################################################";
#echo "Import des ROOT CA public Zertifikats in den Client KeyStore.";
#echo "####################################################################################################";
#keytool -import -v \
#  -keystore keytool/client_keystore.p12 \
#  -storepass "$PW" \
#  -alias root_ca_1 \
#  -file keytool/root_ca_1_pub.crt << EOF
#ja
#EOF

#echo;
#echo "####################################################################################################";
#echo "Import des signierten Server-Zertifikats in den Server KeyStore, das alte unsignierte wird dabei überschrieben.";
#echo "####################################################################################################";
#keytool -import -v \
#  -keystore keytool/server_keystore.p12 \
#  -storepass "$PW" \
#  -alias server \
#  -file keytool/server.crt;

#echo;
#echo "####################################################################################################";
#echo "Import des signierten Client-Zertifikats in den Client KeyStore, das alte unsignierte wird dabei überschrieben.";
#echo "####################################################################################################";
#keytool -import -v \
#  -keystore keytool/client_keystore.p12 \
#  -storepass "$PW" \
#  -alias client \
#  -file keytool/client.crt;

echo;
echo "####################################################################################################";
echo "Inhalt Server-Keystore";
echo "####################################################################################################";
keytool -list \
  -keystore openssl/server_keystore.p12 \
  -storepass "$PW";

#echo;
#echo "####################################################################################################";
#echo "Inhalt Client-Keystore";
#echo "####################################################################################################";
#keytool -list \
#  -keystore keytool/client_keystore.p12 \
#  -storepass "$PW";

echo;
echo "####################################################################################################";
echo "Inhalt des Server TrustStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore openssl/server_truststore.p12 \
  -storepass "$PW";

#echo;
#echo "####################################################################################################";
#echo "Inhalt des Client TrustStore";
#echo "####################################################################################################";
#keytool -list -v \
#  -keystore keytool/client_truststore.p12 \
#  -storepass "$PW"
