# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> .;
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> -f <Dockerfile>;
# docker run [-d] --cpus="2" --name <CONTAINER_NAME> -e VM_PARAMS="-Xms64m -Xmx128m" -p 8081:8081 <IMAGE_NAME>:<VERSION> --spring.profiles.active=test;
# docker start/stop <CONTAINER_NAME>;

#FROM localhost:5083/openjdk:11-jre-slim
FROM localhost:5083/openjdk:12

MAINTAINER Thomas Freese <commercial@freese-home.de>

ARG JAR_FILE=target/*-jar-with-dependencies.jar
ENV VM_PARAMS="-Xms64m -Xmx256m"

COPY ${JAR_FILE} /app/app.jar

RUN echo "Europe/Berlin" > /etc/timezone
 
WORKDIR /
VOLUME /tmp
EXPOSE 9000

ENTRYPOINT exec java \
 $VM_PARAMS \
 -XX:TieredStopAtLevel=1 \
 -Djava.security.egd=file:/dev/./urandom \
 -jar \
 /app/app.jar \
 $0 $@
