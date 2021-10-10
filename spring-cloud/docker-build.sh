#!/bin/sh
#
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> .;
# docker build --build-arg JAR_FILE=<JAR in target Folder> -t <IMAGE_NAME>:<VERSION> -f <Dockerfile>;
# docker run [-d] --name <CONTAINER_NAME> -p 8080:8080 <IMAGE_NAME>:<VERSION>;
# -e "JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>;
#
# multi-stage builds: https://docs.docker.com/develop/develop-images/multistage-build/

# mvn -f spring-cloud-api-gateway clean package & mvn -f spring-cloud-boot-admin clean package & mvn -f spring-cloud-eureka clean package & mvn -f spring-cloud-microservice clean package;

HOST=localhost
PORT=5000

mvn -f spring-cloud-api-gateway     clean package; 
mvn -f spring-cloud-boot-admin      clean package; 
mvn -f spring-cloud-eureka		    clean package; 
mvn -f spring-cloud-microservice	clean package; 

# Image bauen
docker build --tag=spring-cloud-api-gateway:1		spring-cloud-api-gateway;
docker build --tag=spring-cloud-boot-admin:1		spring-cloud-boot-admin;
docker build --tag=spring-cloud-eureka:1			spring-cloud-eureka;
docker build --tag=spring-cloud-microservice:1      spring-cloud-microservice;

# Version taggen
docker tag spring-cloud-api-gateway:1		spring-cloud-api-gateway:latest;
docker tag spring-cloud-boot-admin:1		spring-cloud-boot-admin:latest;
docker tag spring-cloud-eureka:1			spring-cloud-eureka:latest;
docker tag spring-cloud-microservice:1      spring-cloud-microservice:latest;

# Für eigene Registry taggen
docker tag spring-cloud-api-gateway:latest      $HOST:$PORT/spring-cloud-api-gateway:latest;
docker tag spring-cloud-boot-admin:latest		$HOST:$PORT/spring-cloud-boot-admin:latest;
docker tag spring-cloud-eureka:latest			$HOST:$PORT/spring-cloud-eureka:latest;
docker tag spring-cloud-microservice:latest     $HOST:$PORT/spring-cloud-microservice:latest;

# In die eigene Registry pushen
docker push $HOST:$PORT/spring-cloud-api-gateway:latest;
docker push $HOST:$PORT/spring-cloud-boot-admin:latest;
docker push $HOST:$PORT/spring-cloud-eureka:latest;
docker push $HOST:$PORT/spring-cloud-microservice:latest;

# Lokale Images löschen
docker image remove -f spring-cloud-api-gateway		spring-cloud-api-gateway:1     $HOST:$PORT/spring-cloud-api-gateway:latest;
docker image remove -f spring-cloud-boot-admin		spring-cloud-boot-admin:1      $HOST:$PORT/spring-cloud-boot-admin:latest;
docker image remove -f spring-cloud-eureka			spring-cloud-eureka:1          $HOST:$PORT/spring-cloud-eureka:latest;
docker image remove -f spring-cloud-microservice	spring-cloud-microservice:1    $HOST:$PORT/spring-cloud-microservice:latest;

# Image aus eigener Registry neu laden (Kontrolle)
docker pull $HOST:$PORT/spring-cloud-api-gateway:latest;
docker pull $HOST:$PORT/spring-cloud-boot-admin:latest;
docker pull $HOST:$PORT/spring-cloud-eureka:latest;
docker pull $HOST:$PORT/spring-cloud-microservice:latest;
