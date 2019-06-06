#!/bin/sh
#
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> .;
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> -f <Dockerfile>;
# docker run [-d] --name <CONTAINER_NAME> -p 8080:8080 <IMAGE_NAME>:<VERSION>;
# -e "JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>;
#
# multi-stage builds: https://docs.docker.com/develop/develop-images/multistage-build/

mvn -f spring-boot-admin	clean package;
mvn -f spring-eureka		clean package;
mvn -f spring-microservice	clean package;
mvn -f spring-resilience	clean package;

# Image bauen
docker build --tag=spring-boot-admin:1		spring-boot-admin;
docker build --tag=spring-eureka:1			spring-eureka;
docker build --tag=spring-microservice:1	spring-microservice;
docker build --tag=spring-resilience:1		spring-resilience;

# Version taggen
docker tag spring-boot-admin:1			spring-boot-admin:latest;
docker tag spring-eureka:1				spring-eureka:latest;
docker tag spring-microservice:1		spring-microservice:latest;
docker tag spring-resilience:1			spring-resilience:latest;

# Für lokale Registry taggen
docker tag spring-boot-admin:latest		localhost:5000/spring-boot-admin:latest;
docker tag spring-eureka:latest			localhost:5000/spring-eureka:latest;
docker tag spring-microservice:latest	localhost:5000/spring-microservice:latest;
docker tag spring-resilience:latest		localhost:5000/spring-resilience:latest;

# In die lokale Registry pushen
docker push localhost:5000/spring-boot-admin:latest;
docker push localhost:5000/spring-eureka:latest;
docker push localhost:5000/spring-microservice:latest;
docker push localhost:5000/spring-resilience:latest;

# Lokale Images löschen
docker image remove -f spring-boot-admin	spring-boot-admin:1		localhost:5000/spring-boot-admin:latest;
docker image remove -f spring-eureka		spring-eureka:1			localhost:5000/spring-eureka:latest;
docker image remove -f spring-microservice	spring-microservice:1	localhost:5000/spring-microservice:latest;
docker image remove -f spring-resilience	spring-resilience:1		localhost:5000/spring-resilience:latest;

# Image aus lokaler Registry neu laden (Kontrolle)
docker pull localhost:5000/spring-boot-admin:latest;
docker pull localhost:5000/spring-eureka:latest;
docker pull localhost:5000/spring-microservice:latest;
docker pull localhost:5000/spring-resilience:latest;



