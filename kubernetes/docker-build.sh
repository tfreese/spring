#!/bin/sh
#
# docker build -t microservice:1 .;
# docker build -t microservice:1 -f ~/kubernetes-microservice/Dockerfile;
# docker run [-d] --name microservice -p 8080:8080 mymicroservice:1;
# docker start/stop microservice;
#
mvn -f kubernetes-microservice clean package;

docker build --tag=mymicroservice:1	kubernetes-microservice;
docker tag mymicroservice:1 mymicroservice:latest;

