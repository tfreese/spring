# jdbc:hsqldb:hsql://localhost:9001/[fileDb, memDb}
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

ENV HSQLDB_VERSION=2.7.2 \
    HSQLDB_PORT=9001 \
    HSQLDB_TRACE=false \
    HSQLDB_SILENT=true \
    HSQLDB_REMOTE_OPEN=true \
    VM_PARAMS="-Xms32m -Xmx64m"

# && wget -O /opt/hsqldb/hsqldb.jar "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/${HSQLDB_VERSION}/hsqldb-${HSQLDB_VERSION}.jar" \
# && curl -L "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/${HSQLDB_VERSION}/hsqldb-${HSQLDB_VERSION}.jar" -o /opt/hsqldb/hsqldb.jar \

RUN apk update \
    && apk add ca-certificates wget openssl \
    && update-ca-certificates \
    && echo "Europe/Berlin" > /etc/timezone \
    && mkdir -p /opt/hsqldb/data \
    && wget -O /opt/hsqldb/hsqldb.jar "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/${HSQLDB_VERSION}/hsqldb-${HSQLDB_VERSION}.jar" \
    && addgroup -S hsqldb \
    && adduser -S -g hsqldb hsqldb \
    && chown hsqldb:hsqldb -R /opt/hsqldb \
    && apk cache clean

# Clean caches and tmps
RUN rm -rf /tmp/* \
    && rm -rf /var/log/*

USER hsqldb

WORKDIR /
VOLUME /opt/hsqldb/data
EXPOSE ${HSQLDB_PORT}

#CMD java \
ENTRYPOINT exec java \
    ${VM_PARAMS} \
    -Djava.security.egd=file:/dev/./urandom \
    -cp /opt/hsqldb/hsqldb.jar \
    #-Djava.util.logging.config.file=/etc/opt/hsqldb/conf/logging.properties \
    org.hsqldb.server.Server \
    --port ${HSQLDB_PORT} \
    # display all queries
    --silent ${HSQLDB_SILENT} \
    # display JDBC trace messages
    --trace ${HSQLDB_TRACE} \
    # can open databases remotely
    --remote_open ${HSQLDB_REMOTE_OPEN} \
    #--props /opt/hsqldb/hsqldb.properties
    -database.0 "file:/opt/hsqldb/data/fileDb;user=sa;password=;shutdown=false;create=true" -dbname.0 fileDb \
    -database.1 "mem:/memDb;user=sa;password=;shutdown=false;create=true" -dbname.1 memDb
