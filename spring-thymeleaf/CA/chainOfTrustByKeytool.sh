#! /bin/bash
#
# Thomas Freese
# Erzeugt ein self-signed RootCA und signiert damit weitere Zertifikate.
#
export PW="password"
readonly DNAME="CN=Thomas Freese: Root CA 1, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE";

rm -rf "keytool";
mkdir "keytool";

#rm -f *.p12;
#rm -f *.jks;
#rm -f *.crt;
#rm -f *.csr;



echo;
echo "####################################################################################################";
echo "Erzeugen eines self signed key pair root CA Zertifikats.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/root_ca.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -dname "$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500 \
  -ext KeyUsage:critical="keyCertSign" \
  -ext BasicConstraints:critical="ca:true";

echo;
echo "####################################################################################################";
echo "Exportieren des CA public Zertifikats als *.crt so das es in TrustStores verwendet werden kann.";
echo "####################################################################################################";
keytool -export -v \
  -keystore keytool/root_ca.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1_pub.crt \
  -rfc;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Server Zertifikats, z.B. server.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -dname "$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500;
  
echo;
echo "####################################################################################################";
echo "Erzeugen eines Client Zertifikats, z.B. client.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias client \
  -dname "$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Certificate Signing Requests (CSR) für server.";
echo "####################################################################################################";
keytool -certreq -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -file keytool/server.csr;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Certificate Signing Requests (CSR) für client.";
echo "####################################################################################################";
keytool -certreq -v \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias client \
  -file keytool/client.csr;

echo;
echo "####################################################################################################";
echo "Signieren des server-Zertifikats und des -CSR mit dem ROOT CA Zertifikat.";
echo "KeyEncipherment for RSA: DHE or ECDHE.";
echo "####################################################################################################";
keytool -gencert -v \
  -keystore keytool/root_ca.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -infile keytool/server.csr \
  -outfile keytool/server.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:localhost" \
  -rfc;

echo;
echo "####################################################################################################";
echo "Signieren des client-Zertifikats und des -CSR mit dem ROOT CA Zertifikat.";
echo "KeyEncipherment for RSA: DHE or ECDHE.";
echo "####################################################################################################";
keytool -gencert -v \
  -keystore keytool/root_ca.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -infile keytool/client.csr \
  -outfile keytool/client.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:localhost" \
  -rfc;

echo;
echo "####################################################################################################";
echo "Import des ROOT CA public Zertifikats in den Server TrustStore.";
echo "####################################################################################################";
keytool -import -v \
  -keystore keytool/server_truststore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1_pub.crt << EOF
ja
EOF

echo;
echo "####################################################################################################";
echo "Import des ROOT CA public Zertifikats in den Client TrustStore.";
echo "####################################################################################################";
keytool -import -v \
  -keystore keytool/client_truststore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1_pub.crt << EOF
ja
EOF

echo;
echo "####################################################################################################";
echo "Create Server KeyStore.";
echo "####################################################################################################";
# Root CA Public Certificate
keytool -import -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1_pub.crt << EOF
ja
EOF

# Signed Server-Certificate
keytool -import -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -file keytool/server.crt;

echo;
echo "####################################################################################################";
echo "Create Client KeyStore.";
echo "####################################################################################################";
# Root CA Public Certificate
keytool -import -v \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1_pub.crt << EOF
ja
EOF

# Signed Client-Certificate.
keytool -import -v \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias client \
  -file keytool/client.crt;

echo;
echo "####################################################################################################";
echo "Inhalt Server-Keystore";
echo "####################################################################################################";
keytool -list \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW";

echo;
echo "####################################################################################################";
echo "Inhalt Client-Keystore";
echo "####################################################################################################";
keytool -list \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW";

echo;
echo "####################################################################################################";
echo "Inhalt des Server TrustStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore keytool/server_truststore.p12 \
  -storepass "$PW";

echo;
echo "####################################################################################################";
echo "Inhalt des Client TrustStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore keytool/client_truststore.p12 \
  -storepass "$PW"
