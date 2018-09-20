Code:
<!-- link rel="stylesheet" type="text/css" href="webjars/bootstrap/4.1.3/css/bootstrap.min.css"/-->
<!--link rel="stylesheet" type="text/css" th:href="@{/css/main.css}" href="../../css/main.css"/-->
<!--link rel="icon" type="image/png" th:href="@{/images/favicon.png}" sizes="32x32"-->
<!--link rel="icon" type="image/svg+xml" href="favicon.svg" sizes="any"-->

# SSL
# Server Keystore anlegen:
keytool -genkey -v -storetype PKCS12 -keystore server_keystore.p12 -alias localhost -keyalg rsa -keysize 4096 -validity 36500 \
 -storepass storepass -keypass keypass \
 -dname "CN=Thomas Freese, OU=Development, O=Thomas Freese, L=Braunschweig, ST=Niedersachsen, C=DE";

# Öffentliches Certifikat für Clients exportieren:
keytool -export -storetype PKCS12 -keystore server_keystore.p12 -alias localhost -file localhost-public.crt

# Certifikat des Servers in TrtustStore des Clients importieren:
keytool -import -storetype PKCS12 -keystore client_truststore.p12 -alias localhost -file localhost-public.crt


# Im Keystore liegen die eigenen Zertifikate (Public-/Private-Key).
# Im Truststore liegen fremde Public-Keys.



keytool -list -v -keystore keystore.p12
keytool -list -v -storetype PKCS12 -keystore keystore.p12

# Migrate JKS -> PKCS12
keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore.p12 -deststoretype PKCS12

# Import Certificate
keytool -import -keystore keystore.p12 -alias localhost -file myCertificate.crt -storepass storepass


# Von Apache HttpComponents
# Use the following sequence of actions to generate a key-store file

# Use JDK keytool utility to generate a new key
keytool -genkey -v -alias "my client key" -validity 365 -keystore my.keystore

# For simplicity use the same password for the key as that of the key-store
# Issue a certificate signing request (CSR)
keytool -certreq -alias "my client key" -file mycertreq.csr -keystore my.keystore

# Send the certificate request to the trusted Certificate Authority for signature.
# One may choose to act as her own CA and sign the certificate request using a PKI tool, such as OpenSSL.
# Import the trusted CA root certificate
keytool -import -alias "my trusted ca" -file caroot.crt -keystore my.keystore

# Import the PKCS#7 file containing the complete certificate chain
keytool -import -alias "my client key" -file mycert.p7 -keystore my.keystore

# Verify the content of the resultant keystore file
keytool -list -v -keystore my.keystore