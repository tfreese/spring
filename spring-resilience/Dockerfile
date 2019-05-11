# docker build -t spring-resilience .;
# docker build -t spring-resilience -f ~/git/spring/spring-resiliance/Dockerfile;
# docker run [-d] --name spring-resilience -p 8080:8080 spring-resilience;
# docker start/stop spring-resilience;
#
#FROM openjdk:11-alpine
FROM openjdk:11

MAINTAINER Thomas Freese <thomas@freese-home.de>

COPY target/spring-resilience-2.2.0.BUILD-SNAPSHOT.jar /spring-resilience.jar
WORKDIR /

EXPOSE 8080

#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-resilience.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=default", "-jar", "/spring-resilience.jar"]
