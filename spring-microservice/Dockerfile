#FROM openjdk:8-jdk-alpine
# 118 MB, deprecated
 
FROM openjdk:8
# 743 MB

MAINTAINER Thomas Freese <thomas@freese-home.de>

COPY target/microservice-0.0.1-SNAPSHOT-jar-with-dependencies.jar /microservice.jar
WORKDIR /

EXPOSE 8081

#ENTRYPOINT exec java $JAVA_OPTS -jar /microservice.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=default", "-jar", "/microservice.jar"]
