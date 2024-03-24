#!/bin/sh
#
# docker build -t microservice:1 .;
# docker build -t microservice:1 -f ~/kubernetes-microservice/Dockerfile;
# docker run [-d] --name microservice -p 8080:8080 microservice:1;
# docker start/stop microservice;
#

mvn -f microservice clean package;

# Image bauen
docker build --tag=microservice:1 microservice;

# Version taggen
docker tag microservice:1 microservice:latest;

# Für lokale Registry taggen
#docker tag microservice:latest localhost:5000/microservice:latest;

# In die lokale Registry pushen
#docker push localhost:5000/microservice:latest;

# Lokale Images löschen
#docker image remove -f microservice microservice:1 localhost:5000/microservice:latest;

# Image aus lokaler Registry neu laden (Kontrolle)
#docker pull localhost:5000/microservice:latest;
