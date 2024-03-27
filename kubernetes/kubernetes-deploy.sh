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
minikube image load 192.168.155.100:5000/microservice:latest;

kubectl delete --ignore-not-found=true service microservice;
kubectl delete --ignore-not-found=true deployment microservice;

kubectl delete --ignore-not-found=true service db;
kubectl delete --ignore-not-found=true pod db;

kubectl delete --ignore-not-found=true secret db-secret;

kubectl delete --ignore-not-found=true pvc pv-claim;
kubectl delete --ignore-not-found=true pv pv0001;

kubectl create secret generic db-secret --from-literal='username=sa' --from-literal='password=';
#kubectl set env deployment/example-deployment STORAGE_DIR=/local_storage

# minikube mount <source directory>:<target directory>
# Make is accessible in PersistetnVolume.
# minikube mount /mnt/ssd850/kubernetes-pv0001:/mnt/ssd850/kubernetes-pv0001;

kubectl apply -f 00_persistent_volume.yml;
kubectl apply -f 01_persistent_volume_claim.yml;

kubectl apply -f 10_pod_db.yml;
kubectl apply -f 11_service_db.yml;

kubectl apply -f 20_deployment_microservice.yml;
kubectl apply -f 21_service_microservice.yml;

kubectl get all;
sleep 3;

kubectl get all;
sleep 3;

kubectl get pv;
kubectl get pvc;

#kubectl logs -l app=microservice;

for i in {1..10}; do curl $(minikube service microservice --url); echo ""; done;
