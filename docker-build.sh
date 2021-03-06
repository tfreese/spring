#!/bin/sh
#
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> .;
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> -f <Dockerfile>;
# docker run [-d] --name <CONTAINER_NAME> -p 8080:8080 <IMAGE_NAME>:<VERSION>;
# -e "JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>;
#
# multi-stage builds: https://docs.docker.com/develop/develop-images/multistage-build/

# mvn -f spring-api-gateway clean package & mvn -f spring-boot-admin clean package & mvn -f spring-eureka clean package & mvn -f spring-microservice clean package;

HOST=localhost
PORT=5000

mvn -f spring-api-gateway	clean package; 
mvn -f spring-boot-admin	clean package; 
mvn -f spring-eureka		clean package; 
mvn -f spring-microservice	clean package; 
mvn -f spring-resilience	clean package;

# Image bauen
docker build --tag=spring-api-gateway:1		spring-api-gateway;
docker build --tag=spring-boot-admin:1		spring-boot-admin;
docker build --tag=spring-eureka:1			spring-eureka;
docker build --tag=spring-microservice:1	spring-microservice;
docker build --tag=spring-resilience:1		spring-resilience;

# Version taggen
docker tag spring-api-gateway:1		spring-api-gateway:latest;
docker tag spring-boot-admin:1		spring-boot-admin:latest;
docker tag spring-eureka:1			spring-eureka:latest;
docker tag spring-microservice:1	spring-microservice:latest;
docker tag spring-resilience:1		spring-resilience:latest;

# Für eigene Registry taggen
docker tag spring-api-gateway:latest	$HOST:$PORT/spring-api-gateway:latest;
docker tag spring-boot-admin:latest		$HOST:$PORT/spring-boot-admin:latest;
docker tag spring-eureka:latest			$HOST:$PORT/spring-eureka:latest;
docker tag spring-microservice:latest	$HOST:$PORT/spring-microservice:latest;
docker tag spring-resilience:latest		$HOST:$PORT/spring-resilience:latest;

# In die eigene Registry pushen
docker push $HOST:$PORT/spring-api-gateway:latest;
docker push $HOST:$PORT/spring-boot-admin:latest;
docker push $HOST:$PORT/spring-eureka:latest;
docker push $HOST:$PORT/spring-microservice:latest;
docker push $HOST:$PORT/spring-resilience:latest;

# Lokale Images löschen
docker image remove -f spring-api-gateway		spring-api-gateway:1	$HOST:$PORT/spring-api-gateway:latest;
docker image remove -f spring-boot-admin		spring-boot-admin:1		$HOST:$PORT/spring-boot-admin:latest;
docker image remove -f spring-eureka			spring-eureka:1			$HOST:$PORT/spring-eureka:latest;
docker image remove -f spring-microservice		spring-microservice:1	$HOST:$PORT/spring-microservice:latest;
docker image remove -f spring-resilience		spring-resilience:1		$HOST:$PORT/spring-resilience:latest;

# Image aus eigener Registry neu laden (Kontrolle)
docker pull $HOST:$PORT/spring-api-gateway:latest;
docker pull $HOST:$PORT/spring-boot-admin:latest;
docker pull $HOST:$PORT/spring-eureka:latest;
docker pull $HOST:$PORT/spring-microservice:latest;
docker pull $HOST:$PORT/spring-resilience:latest;
