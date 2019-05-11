<!-- runtime scope -->
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-registry-prometheus</artifactId>
	<scope>runtime</scope>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
	<scope>runtime</scope>
</dependency>	
		
management:
  endpoints:
    enabled-by-default: true
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: ALWAYS 	
      
prometheus.yml

docker pull prom/prometheus;
docker run -d --name=prometheus -p 9090:9090 -v prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus --config.file=/etc/prometheus/prometheus.yml;
docker start/stop prometheus;

docker run -d --name=grafana -p 3000:3000 grafana/grafana;
docker start/stop grafana;

username admin and password admin




First get the container ID:

docker ps

(First column is for container ID)

Use the container ID to run:

docker inspect <container ID>

At the bottom,under "NetworkSettings", you can find "IPAddress"

Or Just do:

docker inspect <container id> | grep "IPAddress"


      	