#!/bin/sh
#
# docker build -t microservice:1 .;
# docker build -t microservice:1 -f ~/kubernetes-microservice/Dockerfile;
# docker run [-d] --name microservice -p 8080:8080 mymicroservice:1;
# docker start/stop microservice;
#

mvn -f kubernetes-microservice clean package;

# Image bauen
docker build --tag=kubernetes-microservice:1	kubernetes-microservice;

# Version taggen
docker tag kubernetes-microservice:1	kubernetes-microservice:latest;

# Für lokale Registry taggen
docker tag kubernetes-microservice:latest	localhost:5000/kubernetes-microservice:latest;

# In die lokale Registry pushen
docker push localhost:5000/kubernetes-microservice:latest;

# Lokale Images löschen
docker image remove -f kubernetes-microservice	kubernetes-microservice:1	localhost:5000/kubernetes-microservice:latest;

# Image aus lokaler Registry neu laden
docker pull localhost:5000/kubernetes-microservice:latest;


