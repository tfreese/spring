#!/bin/sh

# Für lokales Images: 
# eval $(minikube docker-env);

kubectl run microservice --image=kubernetes-microservice:latest --port=8080 --image-pull-policy=Never
kubectl expose deployment/microservice --type="LoadBalancer" --port 8080

#kubectl run hello-minikube --image=k8s.gcr.io/echoserver:1.10 --port=8080;
#kubectl expose deployment hello-minikube --type=NodePort;

curl $(minikube service microservice --url)/greet;

#kubectl delete service microservice ... ;
#kubectl delete deployments microservice ...; 
