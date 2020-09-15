#! /bin/bash
#
# Thomas Freese
# Erzeugt ein self-signed RootCA und signiert damit weitere Zertifikate.
#
export PW="password"

rm -f *.p12;
rm -f *.jks;
rm -f *.crt;
rm -f *.csr;



echo;
echo "####################################################################################################";
echo "Erzeugen eines self signed key pair root CA Zertifikats.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore tommy_ca.p12 \
  -storepass "$PW" \
  -alias tommy_ca_1 \
  -dname "CN=Thomas Freese: Root CA 1, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE" \
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
  -keystore tommy_ca.p12 \
  -storepass "$PW" \
  -alias tommy_ca_1 \
  -file tommy_ca_1_pub.crt \
  -rfc;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Server Zertifikats, z.B. localhost.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore server_keystore.p12 \
  -storepass "$PW" \
  -alias localhost \
  -dname "CN=Thomas Freese: localhost, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 36500;

echo;
echo "####################################################################################################";
echo "Erzeugen eines Certificate Signing Requests (CSR) für localhost.";
echo "####################################################################################################";
keytool -certreq -v \
  -keystore server_keystore.p12 \
  -storepass "$PW" \
  -alias localhost \
  -file localhost.csr;

echo;
echo "####################################################################################################";
echo "Signieren des localhost-Zertifikats und des -CSR mit dem ROOT CA Zertifikat.";
echo "KeyEncipherment for RSA: DHE or ECDHE.";
echo "####################################################################################################";
keytool -gencert -v \
  -keystore tommy_ca.p12 \
  -storepass "$PW" \
  -alias tommy_ca_1 \
  -infile localhost.csr \
  -outfile localhost.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:localhost" \
  -rfc;

echo;
echo "####################################################################################################";
echo "Import des ROOT CA public Zertifikats in den Server TrustStore.";
echo "####################################################################################################";
keytool -import -v \
  -storetype PKCS12 \
  -keystore server_truststore.p12 \
  -storepass "$PW" \
  -alias tommy_ca_1 \
  -file tommy_ca_1_pub.crt << EOF
ja
EOF

echo;
echo "####################################################################################################";
echo "Import des ROOT CA public Zertifikats in den Server KeyStore.";
echo "####################################################################################################";
keytool -import -v \
  -keystore server_keystore.p12 \
  -storepass "$PW" \
  -alias tommy_ca_1 \
  -file tommy_ca_1_pub.crt \
  -storetype JKS << EOF
ja
EOF

echo;
echo "####################################################################################################";
echo "Import des signierten localhost-Zertifikats in den Server KeyStore, das alte unsignierte wird dabei überschrieben.";
echo "####################################################################################################";
keytool -import -v \
  -keystore server_keystore.p12 \
  -storepass "$PW" \
  -alias localhost \
  -file localhost.crt \
  -storetype JKS;

echo;
echo "####################################################################################################";
keytool -list \
  -keystore server_keystore.p12 \
  -storepass "$PW";
  
echo;
echo "####################################################################################################";
echo "Inhalt des Server KeyStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore server_keystore.p12 \
  -storepass "$PW";


