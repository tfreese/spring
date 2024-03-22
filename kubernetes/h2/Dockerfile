# jdbc:h2:tcp://localhost:9082/fileDb
# call now();
# call LOCALTIMESTAMP;
# CREATE TABLE person (id BIGINT PRIMARY KEY, name VARCHAR(50) NOT NULL);
# INSERT into person (id, name) values (-1, 'Tommy');
# COMMIT;
# SELECT * from person;
# CHECKPOINT; (compact)
# SHUTDOWN SQL;
# SHUTDOWN COMPACT;

# 2060 MB
#FROM openjdk:21

# 226 MB
#FROM openjdk:21-jdk-slim

# 97 MB
#FROM eclipse-temurin:21-jre-jammy

# 67 MB
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.authors="commercial@freese-home.de"

ENV H2_VERSION=2.2.224 \
    H2_PORT_TCP=9082 \
    VM_PARAMS="-Xms32m -Xmx64m"


# && wget -O /opt/h2/h2.jar "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" \
# && curl -L "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" -o /opt/h2/h2.jar \

RUN apk update \
    && apk add ca-certificates wget openssl \
    && update-ca-certificates \
    && echo "Europe/Berlin" > /etc/timezone \
    && mkdir -p /opt/h2/data \
    && wget -O /opt/h2/h2.jar "https://repo1.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar" \
    && addgroup -S h2 \
    && adduser -S -g h2 h2 \
    && chown h2:h2 -R /opt/h2 \
    && apk cache clean

# Clean caches and tmps
RUN rm -rf /tmp/* \
    && rm -rf /var/log/*

USER h2

WORKDIR /
VOLUME /opt/h2/data
EXPOSE ${H2_PORT_TCP}

#CMD java \
ENTRYPOINT exec java \
    ${VM_PARAMS} \
    -cp /opt/h2/h2.jar \
    org.h2.tools.Server \
    #-properties "${H2CONF}"
    -tcp -tcpAllowOthers -tcpPort ${H2_PORT_TCP} \
    #-web -webAllowOthers -webPort 8082 \
    -ifNotExists \
    -baseDir /opt/h2/data
