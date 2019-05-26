#!/bin/sh
#
# docker build -t spring-microservice:1 .;
# docker build -t spring-microservice:1 -f ~/git/spring/spring-microservice/Dockerfile;
# docker run [-d] --name spring-microservice -p 8080:8080 spring-microservice:1;
# docker start/stop spring-microservice;
#

mvn -f spring-boot-admin	clean package;
mvn -f spring-eureka		clean package;
mvn -f spring-microservice	clean package;
mvn -f spring-resilience	clean package;

docker build --tag=spring-boot-admin:1		spring-boot-admin;
docker build --tag=spring-eureka:1			spring-eureka;
docker build --tag=spring-microservice:1	spring-microservice;
docker build --tag=spring-resilience:1		spring-resilience;

docker tag spring-boot-admin:1		spring-boot-admin:latest;
docker tag spring-eureka:1			spring-eureka:latest;
docker tag spring-microservice:1	spring-microservice:latest;
docker tag spring-resilience:1		spring-resilience:latest;
