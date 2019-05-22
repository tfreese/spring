# docker build -t spring-resilience .;
# docker build -t spring-resilience -f ~/git/spring/spring-resiliance/Dockerfile;
# docker run [-d] --name spring-resilience -p 8080:8080 spring-resilience;
# docker start/stop spring-resilience;
#
#FROM openjdk:11-alpine

FROM openjdk:11-jre-slim
# 302 MB

#FROM openjdk:11
# 844 MB

COPY target/kubernetes-microservice-0.0.1-SNAPSHOT-jar-with-dependencies.jar /kubernetes-microservice.jar
WORKDIR /

EXPOSE 8080

ENTRYPOINT ["java",\
 "-Xms256m",\
 "-Xmx256m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/kubernetes-microservice.jar"]
#ENTRYPOINT exec java $JAVA_OPTS -jar /spring-resilience.jar
#CMD /usr/bin/java -Xms300m -Xmx300m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /spring-resilience.jar
