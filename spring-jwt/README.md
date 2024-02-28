# Spring Security JWT

Spring Security has built-in support for JWTs using oAuth2 Resource Server.\
In this tutorial you are going to learn how to secure your APIs using JSON Web Tokens (JWT) with Spring Security.

- [Blog Post](https://www.danvega.dev/blog/2022/09/06/spring-security-jwt/)
- [YouTube](https://youtu.be/KYNR5js2cXE)

# create rsa key pair

openssl genrsa -out keypair.pem 2048

# extract public key

openssl rsa -in keypair.pem -pubout -out public.pem

# create private key in PKCS#8 format

openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem
