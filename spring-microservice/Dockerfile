# docker build -t spring-microservice .;
# docker build -t spring-microservice -f ~/git/spring/spring-microservice/Dockerfile;
# docker run [-d] --name spring-microservice -p 8080:8080 spring-microservice;
# docker start/stop spring-microservice;
#
#FROM openjdk:11-alpine

FROM openjdk:11-jre-slim
# 302 MB

#FROM openjdk:11
# 844 MB1

MAINTAINER Thomas Freese <commercial@freese-home.de>

COPY target/spring-microservice-0.0.1-SNAPSHOT-jar-with-dependencies.jar /spring-microservice.jar
WORKDIR /

EXPOSE 8081

ENTRYPOINT ["java",\
 "-Xms256m",\
 "-Xmx256m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/spring-microservice.jar"]
#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-microservice.jar
#CMD /usr/bin/java -Xms300m -Xmx300m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /spring-microservice.jar
#RUN [“java”, “...”]
