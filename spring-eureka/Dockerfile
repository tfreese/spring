#FROM openjdk:11-alpine

FROM openjdk:11-jre-slim
# 302 MB

#FROM openjdk:11
# 844 MB

MAINTAINER Thomas Freese <commercial@freese-home.de>

COPY target/spring-eureka-0.0.1-SNAPSHOT-jar-with-dependencies.jar /spring-eureka.jar
WORKDIR /

EXPOSE 9001

ENTRYPOINT ["java",\
 "-Xms128m",\
 "-Xmx128m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/spring-eureka.jar"]
#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-eureka.jar
#CMD /usr/bin/java -Xms300m -Xmx300m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /spring-eureka.jar
#RUN [“java”, “...”]
