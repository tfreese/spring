#!/bin/sh
#
# Exit, if one Command fails.
set -e

trap bashtrap SIGINT SIGTERM

bashtrap()
{
	echo "Exit"
	exit 1;
}

minikube image load 192.168.155.100:5000/hsqldb:latest;
minikube image load 192.168.155.100:5000/microservice:latest;

kubectl apply -f 01_pod_hsqldb.yml;
kubectl apply -f 02_deployment_microservice.yml;
kubectl apply -f 03_service_microservice.yml;

kubectl get all;
