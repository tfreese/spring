#! /bin/bash
#
# Thomas Freese
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



export PW="password"
readonly DNAME="OU=Development,O=MyCompany,L=MyCity,ST=MyState,C=DE";

rm -rf "keytool";
mkdir "keytool";



echo;
echo "####################################################################################################";
echo "Create a self signed key pair root CA Certificate.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/root_ca_1.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -dname "CN=root_ca_1,$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500 \
  -ext KeyUsage:critical="keyCertSign" \
  -ext BasicConstraints:critical="ca:true";



echo;
echo "####################################################################################################";
echo "Export of the CA public Certificate to use it in TrustStores.";
echo "####################################################################################################";
keytool -export -v \
  -keystore keytool/root_ca_1.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1.crt \
  -rfc;

echo;
echo "####################################################################################################";
echo "Create Certificates for server and client.";
echo "####################################################################################################";
keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -dname "CN=server,$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500;



keytool -genkey -v \
  -storetype PKCS12 \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias client \
  -dname "CN=client,$DNAME" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500;



echo;
echo "####################################################################################################";
echo "Create Certificate Signing Requests (CSR) for server and client.";
echo "####################################################################################################";
keytool -certreq -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -file keytool/server.csr;



keytool -certreq -v \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias client \
  -file keytool/client.csr;



echo;
echo "####################################################################################################";
echo "Signing the Certificates and the CSRs with the ROOT CA Certificate.";
echo "KeyEncipherment for RSA: DHE or ECDHE.";
echo "####################################################################################################";
keytool -gencert -v \
  -keystore keytool/root_ca_1.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -infile keytool/server.csr \
  -outfile keytool/server.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:localhost" \
  -rfc;



keytool -gencert -v \
  -keystore keytool/root_ca_1.p12 \
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
echo "Import the ROOT CA public Certificate into the TrustStores.";
echo "####################################################################################################";
keytool -import -v \
  -keystore keytool/server_truststore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1.crt << EOF
ja
EOF



keytool -import -v \
  -keystore keytool/client_truststore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1.crt << EOF
ja
EOF



echo;
echo "####################################################################################################";
echo "Create the KeyStores.";
echo "####################################################################################################";
# Root CA Public Certificate
keytool -import -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1.crt << EOF
ja
EOF

# Signed Server-Certificate
keytool -import -v \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW" \
  -alias server \
  -file keytool/server.crt;



# Root CA Public Certificate
keytool -import -v \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW" \
  -alias root_ca_1 \
  -file keytool/root_ca_1.crt << EOF
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
echo "Content of Server-Keystore";
echo "####################################################################################################";
keytool -list \
  -keystore keytool/server_keystore.p12 \
  -storepass "$PW";



echo;
echo "####################################################################################################";
echo "Content of Client-Keystore";
echo "####################################################################################################";
keytool -list \
  -keystore keytool/client_keystore.p12 \
  -storepass "$PW";



echo;
echo "####################################################################################################";
echo "Content of Server TrustStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore keytool/server_truststore.p12 \
  -storepass "$PW";



echo;
echo "####################################################################################################";
echo "Content of Client TrustStore";
echo "####################################################################################################";
keytool -list -v \
  -keystore keytool/client_truststore.p12 \
  -storepass "$PW"
