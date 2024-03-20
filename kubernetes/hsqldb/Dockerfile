# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> .
# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> -f <Dockerfile> .

# 2060 MB
FROM openjdk:21

# 226 MB
#FROM openjdk:21-jdk-slim

# 97 MB
#FROM eclipse-temurin:21-jre-jammy

# 67 MB
#FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.authors="commercial@freese-home.de"

ENV VM_PARAMS="-Xms64m -Xmx128m"

#apk update && \
    #apk add ca-certificates wget openssl && \
    #update-ca-certificates && \
    #wget -O /opt/hsqldb/hsqldb.jar https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.7.2/hsqldb-2.7.2.jar

RUN echo "Europe/Berlin" > /etc/timezone && \
    mkdir -p /opt/hsqldb/data && \
    curl -L "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.7.2/hsqldb-2.7.2.jar" -o /opt/hsqldb/hsqldb.jar


    #&& \
    #addgroup -S hsqldb && \
    #adduser -S -g hsqldb hsqldb && \
    #chown hsqldb:hsqldb -R /opt/hsqldb

# Clean caches and tmps
RUN rm -rf /tmp/* && \
    rm -rf /var/log/*

WORKDIR /
#USER hsqldb
#VOLUME /tmp
EXPOSE 9001

#ENTRYPOINT exec java \
# $VM_PARAMS \
# -Djava.security.egd=file:/dev/./urandom \
# -jar \
# /app/app.jar \
# $0 $@

CMD java \
   -cp /opt/hsqldb/hsqldb.jar \
   #-Djava.util.logging.config.file=/etc/opt/hsqldb/conf/logging.properties \
   org.hsqldb.server.Server \
   #--props /etc/opt/hsqldb/conf/server.properties
   -database.0 "file:/opt/hsqldb/data;user=sa;password=" -dbname.0 test
