#FROM openjdk:11-alpine

FROM openjdk:11-jre-slim
# 302 MB

#FROM openjdk:11
# 844 MB

MAINTAINER Thomas Freese <commercial@freese-home.de>

COPY target/spring-boot-admin-0.0.1-SNAPSHOT-jar-with-dependencies.jar /spring-boot-admin.jar
WORKDIR /

EXPOSE 9000

ENTRYPOINT ["java",\
 "-Xms128m",\
 "-Xmx128m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/spring-boot-admin.jar"]
#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-boot-admin.jar
#CMD /usr/bin/java -Xms300m -Xmx300m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /spring-boot-admin.jar
