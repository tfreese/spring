FROM openjdk:11

MAINTAINER Thomas Freese <thomas@freese-home.de>

COPY target/spring-eureka-0.0.1-SNAPSHOT-jar-with-dependencies.jar /spring-eureka.jar
WORKDIR /

EXPOSE 9001

#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-eureka.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=default", "-jar", "/spring-eureka.jar"]
