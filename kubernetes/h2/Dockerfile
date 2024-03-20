# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> .
# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> -f <Dockerfile> .

# 2060 MB
#FROM openjdk:21

# 226 MB
#FROM openjdk:21-jdk-slim

# 97 MB
#FROM eclipse-temurin:21-jre-jammy

# 67 MB
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.authors="commercial@freese-home.de"

ENV VM_PARAMS="-Xms64m -Xmx128m"

RUN echo "Europe/Berlin" > /etc/timezone

WORKDIR /
VOLUME /tmp
EXPOSE 8080

ENTRYPOINT exec java \
 $VM_PARAMS \
 -Djava.security.egd=file:/dev/./urandom \
 -jar \
 /app/app.jar \
 $0 $@
