FROM openjdk:11

MAINTAINER Thomas Freese <thomas@freese-home.de>

COPY target/spring-boot-admin-0.0.1-SNAPSHOT-jar-with-dependencies.jar /spring-boot-admin.jar
WORKDIR /

EXPOSE 9000

#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-boot-admin.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=default", "-jar", "/spring-boot-admin.jar"]
