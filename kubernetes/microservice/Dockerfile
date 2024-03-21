# 2060 MB
#FROM openjdk:21

# 226 MB
#FROM openjdk:21-jdk-slim

# 97 MB
#FROM eclipse-temurin:21-jre-jammy

# 67 MB
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.authors="commercial@freese-home.de"

ARG JAR_FILE=build/libs/kubernetes-microservice-boot.jar
ENV VM_PARAMS="-Xms32m -Xmx64m"

COPY ${JAR_FILE} /app/app.jar

RUN echo "Europe/Berlin" > /etc/timezone

WORKDIR /
VOLUME /tmp
EXPOSE 8080

ENTRYPOINT exec java \
    ${VM_PARAMS} \
    -Djava.security.egd=file:/dev/./urandom \
    -jar \
    /app/app.jar \
    "$0" $@
