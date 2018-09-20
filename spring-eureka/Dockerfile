FROM openjdk:8

MAINTAINER Thomas Freese <thomas@freese-home.de>

COPY target/eureka-0.0.1-SNAPSHOT-jar-with-dependencies.jar /eureka.jar
WORKDIR /

EXPOSE 9001

#ENTRYPOINT exec java $JAVA_OPTS -jar /eureka.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=default", "-jar", "/eureka.jar"]
