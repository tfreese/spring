#!/bin/sh
#
# docker build --force-rm=true --no-cache=true --tag=<IMAGE_NAME>:<VERSION> -f <Dockerfile> .
# docker run [-d] [--rm] --name <CONTAINER_NAME> --publish/-p 8080:8080 --memory/-m 128m --cpus="2" <IMAGE_NAME>:<VERSION> --spring.profiles.active=test
# -e "VM_PARAMS=-agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=n"
# docker start/stop <CONTAINER_NAME>

# --network host: für Internetzugang

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

gradle -p microservice clean build;

# Build Image
docker build --force-rm=true --no-cache=true --tag=microservice:1 microservice;
docker build --force-rm=true --no-cache=true --network=host --tag=hsqldb:1 hsqldb;
docker build --force-rm=true --no-cache=true --network=host --tag=h2:1 h2;

# Tag Version
docker tag microservice:1 microservice:latest;
docker tag hsqldb:1 hsqldb:latest;
docker tag h2:1 h2:latest;

# Tag for local Registry
docker tag microservice:latest localhost:5000/microservice:latest;
docker tag hsqldb:latest localhost:5000/hsqldb:latest;
docker tag h2:latest localhost:5000/h2:latest;

# Push into local Registry
docker push localhost:5000/microservice:latest;
docker push localhost:5000/hsqldb:latest;
docker push localhost:5000/h2:latest;

# Delete local Image
docker image remove -f microservice microservice:1 localhost:5000/microservice:latest;
docker image remove -f hsqldb hsqldb:1 localhost:5000/hsqldb:latest;
docker image remove -f h2 h2:1 localhost:5000/h2:latest;

# Load Image from local Registry (for testing)
# docker pull localhost:5000/microservice:latest;
# docker pull localhost:5000/hsqldb:latest;
# docker pull localhost:5000/h2:latest;
