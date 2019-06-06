# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> .;
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> -f <Dockerfile>;
# docker run [-d] --name <CONTAINER_NAME> -e "SPRING_PROFILES_ACTIVE=prod" -p 8080:8080 <IMAGE_NAME>:<VERSION>;
# -e "JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>;
#
#FROM openjdk:11-alpine
 
FROM openjdk:11-jre-slim
# 302 MB
 
#FROM openjdk:11
# 844 MB1
 
MAINTAINER Thomas Freese <commercial@freese-home.de>
 
ARG JAR_FILE=target/*-jar-with-dependencies.jar
COPY ${JAR_FILE} /app/app.jar
 
#ARG DEPENDENCY=target/dependency
#COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY ${DEPENDENCY}/META-INF /app/META-INF
#COPY ${DEPENDENCY}/BOOT-INF/classes /app
#ENTRYPOINT ["java","-cp","app:app/lib/*","hello.Application"]
 
WORKDIR /
VOLUME /tmp
EXPOSE 8081

#ENV JAVA_OPTS="-Xms256m -Xmx256m"

ENTRYPOINT ["java",\
 "-Xms256m",\
 "-Xmx256m",\
 "-XX:TieredStopAtLevel=1",\
 "-noverify",\
 "-Djava.security.egd=file:/dev/./urandom",\
 "-Dspring.profiles.active=default",\
 "-jar",\
 "/app/app.jar"]
 
#ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar
#CMD /usr/bin/java -Xms256m -Xmx256m -XX:TieredStopAtLevel=1 -noverify -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=default -jar /app/app.jar
#RUN [“java”, “...”]