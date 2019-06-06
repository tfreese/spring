FROM openjdk:11-jre-slim
 
MAINTAINER Thomas Freese <commercial@freese-home.de>
 
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} /app/app.jar
 
WORKDIR /
VOLUME /tmp
EXPOSE 9001

ENTRYPOINT ["java",\
 "-Xms256m",\
 "-Xmx256m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/app/app.jar"]
