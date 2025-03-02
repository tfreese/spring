#!/bin/sh
# Exit, if one Command fails.
set -e

trap bashtrap SIGINT SIGTERM

bashtrap()
{
	echo "Exit"
	exit 1;
}

gradle build;

docker-compose build;
docker-compose up;

#for i in {1..10}; do curl localhost:8080/greet?name=World; echo ""; sleep 0.1; done;
