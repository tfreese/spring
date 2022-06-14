# docker build -t <IMAGE_NAME>:<VERSION> .
# docker build -t <IMAGE_NAME>:<VERSION> -f <Dockerfile> .
# docker run [-d] --memory/-m 128m --cpus="2" --name <CONTAINER_NAME> --publish/-p 8080:8080 <IMAGE_NAME>:<VERSION> --spring.profiles.active=test
# -e "VM_PARAMS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>

# --network host: für Internetzugang

#FROM localhost:5083/openjdk:17
FROM openjdk:latest
 
MAINTAINER Thomas Freese <commercial@freese-home.de>
 
ARG JAR_FILE=target/*-jar-with-dependencies.jar
ENV VM_PARAMS="-Xms64m -Xmx256m"

COPY ${JAR_FILE} /app/app.jar

RUN echo "Europe/Berlin" > /etc/timezone
 
WORKDIR /
VOLUME /tmp
EXPOSE 8081

ENTRYPOINT exec java \
 $VM_PARAMS \
 -XX:TieredStopAtLevel=1 \
 -Djava.security.egd=file:/dev/./urandom \
 -jar \
 /app/app.jar \
 $0 $@
