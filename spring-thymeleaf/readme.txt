- https://developer.okta.com/blog/2018/07/30/10-ways-to-secure-spring-boot



#######################################################################################################################
# https://www.novatec-gmbh.de/en/blog/spring-boot-applications-tls-http2/

Certificate for Root CA
Before you start please create the following subdirectories first:

root-ca  (You will store all artifacts required for setting up a certificate authority here)
server (You will store all artifacts required for your signed server certificate here)
In the first step you need to generate private/public keys and the corresponding certificate for the root CA. Later you will use this root certificate in section for signing your server certificate.


keytool -genkeypair -keyalg RSA -keysize 3072 -alias root-ca -dname "CN=My Root CA,OU=Development,O=My Organization,C=DE" -ext BC:c=ca:true -ext KU=keyCertSign -validity 3650 -keystore ./root-ca/ca.jks -storepass secret -keypass secret


This command creates a new java keystore ca.jks in folder root-ca containing the private and public keys.
The certificate uses the RSA algorithm with a bit length of 3072 and is valid for 10 years.
This includes also the distinguished name CN=My CA,OU=Development,O=My Organization,C=DE.



Now you export the certificate to file ca.pem in the subdirectory root-ca using this command:


keytool -exportcert -keystore ./root-ca/ca.jks -storepass secret -alias root-ca -rfc -file ./root-ca/ca.pem


Signed Server Certificate
In the next step you create another new java key store file containing the private/public keys for the server certificate.
The private key is required to generate the certificate signing request. The CA uses the public key for validating the certificate signing request.


keytool -genkeypair -keyalg RSA -keysize 3072 -alias localhost -dname "CN=localhost,OU=Development,O=My Organization,C=DE" -ext BC:c=ca:false -ext EKU:c=serverAuth -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore ./server/server.jks -storepass secret -keypass secret


You can find the new java key store server.jks in subdirectory server. Again we use the RSA algorithm with bit length of 3072 and set it valid for 10 years.

Now you will continue with generation of the signing request for your server certificate. This creates the file server.csr in sub directory server.


keytool -certreq -keystore ./server/server.jks -storepass secret -alias localhost -keypass secret -file ./server/server.csr


With the next command you will now sign and export your server certificate using the file server.csr from the previous step.


keytool -gencert -keystore ./root-ca/ca.jks -storepass secret -infile ./server/server.csr -alias root-ca -keypass secret -ext BC:c=ca:false -ext EKU:c=serverAuth -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -rfc -outfile ./server/server.pem


To achieve the required valid chain of trust between the root ca and the signed server certificate you have to perform the following last step.


keytool -importcert -noprompt -keystore ./server/server.jks -storepass secret -alias root-ca -keypass secret -file ./root-ca/ca.pem
keytool -importcert -noprompt -keystore ./server/server.jks -storepass secret -alias localhost -keypass secret -file ./server/server.pem


This imports the certificate for the root ca and updates the existing (unsigned) server certificate with the signed one.

Finally, we have a java key store containing the full chain of certificates is ready to be used in our spring boot application.

Import Root CA Certificate into web browser
Let’s continue with enabling trust in your web browser for our private certificate authority.
We will use the Chrome browser here to demonstrate this. Just open the settings in chrome, expand the “Advanced” section and then go to "Manage certificates".
Here you import the root ca certificate from file ./root-ca/ca.pem into the browser as new authority.





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

# Certifikat des Servers in TrustStore des Clients importieren:
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
