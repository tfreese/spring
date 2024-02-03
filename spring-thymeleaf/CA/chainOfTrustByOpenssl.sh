#! /bin/bash
#
# Thomas Freese
# Erzeugt ein self-signed RootCA und signiert damit weitere Zertifikate.
#
# CN: CommonName
# OU: OrganizationalUnit
# O: Organization
# L: Locality
# S: StateOrProvinceName
# C: CountryName

# Exit, if one Command fails.
set -e

trap bashtrap SIGINT SIGTERM

bashtrap()
{
	echo "Exit"
	exit 1;
}

readonly PW="password"
readonly DNAME="OU=Development,O=MyCompany,L=MyCity,ST=MyState,C=DE";

rm -rf "openssl";
mkdir "openssl";


#echo;
#echo "####################################################################################################";
#echo "A Self Signed certificate is easy to generate with one command.";
#echo "####################################################################################################";
# openssl req -x509 -newkey rsa:4096 -keyout openssl/root_ca_1.key -out openssl/root_ca_1.crt -days 36500 -passout pass:"$PW" -sha512 -subj "/CN=root_ca_1,$DNAME";

echo;
echo "####################################################################################################";
echo "Create a self signed key pair root CA Certificate.";
echo "####################################################################################################";
openssl genrsa -aes256 -passout pass:"$PW" -out openssl/root_ca_1.key 4096;
openssl rsa -in openssl/root_ca_1.key -check -noout -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Export of the CA public Certificate to use it in TrustStores.";
echo "####################################################################################################";
openssl req -x509 -new -key openssl/root_ca_1.key -passin pass:"$PW" -out openssl/root_ca_1.crt -days 36500 -sha512 -subj "/CN=root_ca_1,$DNAME";

# root_ca_2: Create self Signed certificate
#openssl req -x509 -newkey rsa:4096 -keyout openssl/root_ca_2.key -out openssl/root_ca_2.crt -days 36500 -passout pass:"$PW" -sha512 -subj "/CN=root_ca_2,$DNAME";
# root_ca_2: CSR
#openssl req -new -key openssl/root_ca_2.key -sha512 -out openssl/root_ca_2.csr -subj "/CN=root_ca_2,$DNAME" -passin pass:"$PW";
# root_ca_2: Signing with root_ca_1
#openssl x509 -req -CA openssl/root_ca_1.crt -CAkey openssl/root_ca_1.key -in openssl/root_ca_2.csr -out openssl/root_ca_2.crt -days 36500 -CAcreateserial -sha512 -passin pass:"$PW"



echo;
echo "####################################################################################################";
echo "Create Certificates for server and client.";
echo "####################################################################################################";
openssl genrsa -aes256 -passout pass:"$PW" -out openssl/server.key 4096;
openssl rsa -in openssl/server.key -check -noout -passin pass:"$PW";



openssl genrsa -aes256 -passout pass:"$PW" -out openssl/client.key 4096;
openssl rsa -in openssl/client.key -check -noout -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Create Certificate Signing Requests (CSR) for server and client.";
echo "####################################################################################################";
openssl req -new -key openssl/server.key -sha512 -out openssl/server.csr -subj "/CN=server,$DNAME" -passin pass:"$PW";
openssl req -verify -noout -in openssl/server.csr;



openssl req -new -key openssl/client.key -sha512 -out openssl/client.csr -subj "/CN=client,$DNAME" -passin pass:"$PW";
openssl req -verify -noout -in openssl/client.csr;



echo;
echo "####################################################################################################";
echo "Signing the Certificates and the CSRs with the ROOT CA Certificate.";
echo "####################################################################################################";
openssl x509 -req -CA openssl/root_ca_1.crt -CAkey openssl/root_ca_1.key -in openssl/server.csr -out openssl/server.crt -days 36500 -CAcreateserial -sha512 -passin pass:"$PW";



openssl x509 -req -CA openssl/root_ca_1.crt -CAkey openssl/root_ca_1.key -in openssl/client.csr -out openssl/client.crt -days 36500 -CAcreateserial -sha512 -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Create the KeyStores.";
echo "####################################################################################################";
#cat openssl/root_ca_1.crt openssl/root_ca_2.crt openssl/server.crt > openssl/server-all.crts;
cat openssl/root_ca_1.crt openssl/server.crt > openssl/server-all.crts;

openssl pkcs12 -export -in openssl/server-all.crts -inkey openssl/server.key -name localhost -passin pass:"$PW" -passout pass:"$PW" > openssl/server_keystore.p12;

#keytool -importkeystore -srckeystore KEYSTORE-FILENAME.jks -srcstoretype JKS -srcstorepass somepassword \
# -destkeystore KEYSTORE-FILENAME.p12 -deststoretype PKCS12 -deststorepass somepassword



cat openssl/root_ca_1.crt openssl/client.crt > openssl/client-all.crts;
openssl pkcs12 -export -in openssl/client-all.crts -inkey openssl/client.key -name localhost -passin pass:"$PW" -passout pass:"$PW" > openssl/client_keystore.p12;



echo;
echo "####################################################################################################";
echo "Create the TrustStores.";
echo "####################################################################################################";
#cat openssl/root_ca_1.crt openssl/client.crt > openssl/server-all.trust;
#openssl pkcs12 -export -in openssl/server-all.trust -inkey openssl/root_ca_1.key -name root_ca_1 -passin pass:"$PW" -passout pass:"$PW" > openssl/server_truststore.p12
openssl pkcs12 -export -in openssl/root_ca_1.crt -inkey openssl/root_ca_1.key -name root_ca_1 -passin pass:"$PW" -passout pass:"$PW" > openssl/server_truststore.p12



openssl pkcs12 -export -in openssl/root_ca_1.crt -inkey openssl/root_ca_1.key -name root_ca_1 -passin pass:"$PW" -passout pass:"$PW" > openssl/client_truststore.p12



echo;
echo "####################################################################################################";
echo "Content of Server-Keystore";
echo "####################################################################################################";
openssl pkcs12 -nokeys -info \
    -in openssl/server_keystore.p12 \
    -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Content of Client-Keystore";
echo "####################################################################################################";
openssl pkcs12 -nokeys -info \
    -in openssl/client_keystore.p12 \
    -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Content of Server TrustStore";
echo "####################################################################################################";
openssl pkcs12 -nokeys -info \
    -in openssl/server_truststore.p12 \
    -passin pass:"$PW";



echo;
echo "####################################################################################################";
echo "Content of Client TrustStore";
echo "####################################################################################################";
openssl pkcs12 -nokeys -info \
    -in openssl/client_truststore.p12 \
    -passin pass:"$PW";
