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

minikube image load 192.168.155.100:5000/h2:latest;
minikube image load 192.168.155.100:5000/hsqldb:latest;
minikube image load 192.168.155.100:5000/backend:latest;

kubectl delete --ignore-not-found=true service backend-service;
kubectl delete --ignore-not-found=true deployment backend-deployment;

kubectl delete --ignore-not-found=true service db-service-web;
kubectl delete --ignore-not-found=true service db-service;
kubectl delete --ignore-not-found=true pod db-pod;

kubectl delete --ignore-not-found=true secret db-secret;

kubectl delete --ignore-not-found=true pvc pv-claim;
kubectl delete --ignore-not-found=true pv pv0001;

kubectl delete --ignore-not-found=true sc local-storage;
kubectl delete --ignore-not-found=true namespace demo;

echo "###########################################";
kubectl create secret generic db-secret --from-literal='username=sa' --from-literal='password=';

#kubectl apply -f 00_namespace.yml;
#kubectl apply -f 01_storage_class.yml;

kubectl apply -f 10_persistent_volume.yml;
kubectl apply -f 11_persistent_volume_claim.yml;
sleep 3;
kubectl apply -f 40_db_pod.yml;
kubectl apply -f 41_db_service.yml;
kubectl apply -f 42_db_service_web.yml;
sleep 3;
kubectl apply -f 50_backend_deployment.yml;
kubectl apply -f 51_backend_service.yml;

echo "###########################################";
kubectl get all --output=wide;
sleep 3;

echo "###########################################";
kubectl get all --output=wide;
sleep 3;

echo "###########################################";
kubectl get sc;
echo "###########################################";
kubectl get pv --output=wide;
echo "###########################################";
kubectl get pvc --output=wide;

echo "###########################################";
#kubectl logs -l app=backend;

for i in {1..10}; do curl $(minikube service backend-service --url); echo ""; sleep 0.1; done;
