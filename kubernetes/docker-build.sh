#!/bin/sh
#
# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> .
# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> -f <Dockerfile> .
# docker run [-d] [--rm] --name <CONTAINER_NAME> --publish/-p 8080:8080 --memory/-m 128m --cpus="2" <IMAGE_NAME>:<VERSION> --spring.profiles.active=test
# -e "VM_PARAMS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>

# --network host: für Internetzugang

# for i in {1..1000}; do curl localhost:8080; echo ""; sleep 1; done;

# wget -O /opt/hsqldb/hsqldb.jar https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.7.2/hsqldb-2.7.2.jar
# curl -L "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.7.2/hsqldb-2.7.2.jar" -o /opt/hsqldb/hsqldb.jar

# docker run --rm --name hsqldb -p 9001:9001 -v /tmp/hsqldb:/opt/hsqldb/data hsqldb:1
# --mount type=bind,source=/tmp/hsqldb,target=/opt/hsqldb/data
# docker run --rm --name h2 -p 9082:9082 -v /tmp/h2:/opt/h2/data h2:1
# --mount type=bind,source=/tmp/h2,target=/opt/h2/data

# Exit, if one Command fails.
set -e

trap bashtrap SIGINT SIGTERM

bashtrap()
{
	echo "Exit"
	exit 1;
}

# docker run -d -p 5000:5000 --restart=always -v /mnt/ssd850/docker-registry:/var/lib/registry --name registry registry:latest
# /ets/hosts: 127.0.0.1 docker.local
# systemctl stop docker
# /etc/docker/daemon.json: "insecure-registries" : [ "docker.local:5000", "192.168.155.100:5000" ]
# systemctl daemon-reload
# systemctl start docker
# minikube start --insecure-registry="docker.local:5000"
# minikube image load 192.168.155.100:5000/microservice:latest
# minikube image ls --format table
# kubectl run microservice --image=192.168.155.100:5000/microservice:latest --image-pull-policy=Never --restart=Never --port=8989
# kubectl describe pod microservice
# kubectl delete pod microservice
# kubectl get all
# kubectl logs microservice-6c79dbcd6f-bncxp
# kubectl port-forward microservice-6c79dbcd6f-bncxp 8080:8080
# curl https://localhost:8080

# https://minikube.sigs.k8s.io/docs/handbook/pushing/
# https://minikube.sigs.k8s.io/docs/handbook/vpn_and_proxy/

gradle -p microservice clean build;

# Build Image
# eval $(minikube -p minikube docker-env); # Set do Minikube Docker Daemon
# eval $(minikube docker-env -u); # Set do local Docker Daemon.
# docker build --tag=microservice:1 microservice;
# docker images ls -a;
# docker tag microservice:1 $(minikube ip):5000/microservice:latest;
# docker push $(minikube ip):5000/microservice:latest
docker build --force-rm=true --no-cache=true --tag=microservice:1 microservice;
docker build --force-rm=true --no-cache=true --network=host --tag=hsqldb:1 hsqldb;
docker build --force-rm=true --no-cache=true --network=host --tag=h2:1 h2;

# Tag Version
docker tag microservice:1 microservice:latest;
docker tag hsqldb:1 hsqldb:latest;
docker tag h2:1 h2:latest;

# Tag for local Registry
# docker tag microservice:latest localhost:5000/microservice:latest;

# Push into local Registry
# docker push localhost:5000/microservice:latest;

# Delete local Image
# docker image remove -f microservice microservice:1 localhost:5000/microservice:latest;

# Load Image from local Registry
# docker pull localhost:5000/microservice:latest;
