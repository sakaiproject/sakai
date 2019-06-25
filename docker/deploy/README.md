# Sakai Docker Deployment Examples

A collection of tools and examples to demonstrate building and deploying Sakai using Docker and/or Docker Swarm.

### Features
Features of the files you will find here:

* Example Swarm stacks
  * Sakai + Mysql 
  * Sakai + Mysql + Elasticsearch + Cerebro + Mailcatcher

#### Table of Contents
- [Quick install Docker (Linux, new server/workstation)](#quick-install-docker--linux--new-server-workstation-)
- [Running Sakai](#running-sakai)
  * [Swarm enabled Docker](#swarm-enabled-docker)
    + [Sakai+Mysql](#sakai-mysql)
    + [Sakai+Mysql+Elasticsearch+Mailcatcher](#sakai-mysql-elasticsearch-mailcatcher)
    + [Monitoring Examples](#monitoring-examples)
    + [Management Examples](#management-examples)
  * [Standalone Docker](#standalone-docker)
    + [Monitoring Examples](#monitoring-examples-1)
    + [Management Examples](#management-examples-1)


# Quick install Docker (Linux, new server/workstation)
Docker provides an installation script for most Linux distributions, With Ubuntu and RHEL/CentOS being the most used. 
This script is located at https://get.docker.com/ and has the following instructions at the top of the file:

    This script is meant for quick & easy install via:
    $ curl -fsSL https://get.docker.com -o get-docker.sh
    $ sh get-docker.sh

This will install and prepare everything needed to build images and run them in standalone mode.
Additionally, docker has a built-in container orchestration platform called "Docker Swarm". Swarm needs to be enabled to use any of these deployment example scripts, this can be enabled by simply running:

     $ docker swarm init 

# Running Sakai
You will need to provide at a minimum a `sakai.properties` configuration containing a minimal database configuration. In the examples here, a MySQL configuration is provided.

## Swarm enabled Docker
These are basic examples, that do not include mounting storage volumes to persist data.

### Sakai+Mysql
To start the example Sakai and Mysql stack follow these steps:
 1. From this folder.
 1. Execute `docker stack deploy -c sakai_docker.yml sakai`
 	* This creates a stack named `sakai` using the compose file `sakai_docker.yml`
 	* Services in the stack are named \<StackName\>_\<ServiceName\> (e.g. sakai_mysql and sakai_sakai)
 	* The configuration file `conf/sakai.properties` is mounted inside the container in /usr/local/sakai/properties
 	* Sakai will be located at `http://<dockerhost>:8080/portal`

### Sakai+Mysql+Elasticsearch+Cerebro+Mailcatcher
To start the example Sakai, Mysql, Elasticsearch, Cerebro, and Mailcatcher stack follow these steps:
 1. From this folder.
 1. Execute `docker stack deploy -c sakai_es_docker.yml sakai`
 	* This creates a stack named `sakai` using the compose file `sakai_es_docker.yml`
 	* Services in the stack are named \<StackName\>_\<ServiceName\> (e.g. sakai_mysql and sakai_sakai)
 	* The configuration file `conf/sakai.properties` is mounted inside the container in /usr/local/sakai/properties
    * The secrets file `secrets/security.properties` is deployed using Docker Secrets and placed in /usr/local/tomcat/sakai/
    * Sakai will be located at `http://<dockerhost>:8080/portal`
    * Mailcatcher will be located at `http://<dockerhost>:8081/`
    * Cerebro (Elasticsearch Management) will be located at `http://<dockerhost>:8082/`
    * Use ES Node address: `http://elasticsearch:9200/`


### Monitoring Examples
Now that the stack has started you can monitor with the Swarm commands:
 1. List running stacks with `docker stack ls`
 1. List services running in the sakai stack with `docker stack services sakai`
 1. List containers running in the sakai service with `docker service ps sakai_sakai`
 1. View (tail) the logs from the Sakai container with `docker service logs -f sakai_sakai`

### Management Examples
You can manage using Swarm commands:
 1. Restart the sakai service with `docker service update --force sakai_sakai`
 1. Stop and remove the stack with `docker stack rm sakai`

## Standalone Docker 
In this example you will start a standalone MySQL container and a linked standalone sakai container.
 1. From this folder.
 1. Start a MySQL 5.5 container with:
    *     docker run -d \
          --name mysql \
          -e MYSQL_ROOT_PASSWORD=examplerootpassword \
          -e MYSQL_DATABASE=sakai -e MYSQL_USER=sakai \
          -e MYSQL_PASSWORD=examplepassword \
          mysql:5.5 \
          --character-set-server=utf8 \
          --collation-server=utf8_general_ci
 1. Monitor MySQL startup with `docker logs -f mysql` and hit Ctrl-C once MySQL has finished starting.
 1. Start a Sakai Container with the MySQL container linked to it:
    *     docker run -d \
          --name sakai \
          -p 8080:8080 \
          -v $(pwd)/config/sakai.properties:/usr/local/sakai/properties/sakai.properties \
          --link mysql:mysql \         
          sakai
 1. Monitor Sakai startup with `docker logs -f sakai` and hit Ctrl-C once Sakai has finished starting.
 1. Sakai should be available on port 8080 of the Dockerhost.

### Monitoring Examples
You may need to monitor the containers
 1. View logs of the sakai container with `docker logs -f sakai`
 1. Inspect the sakai container configuration with `docker inspect sakai`

### Management Examples
You may need to manage the containers
 1. Stop the running sakai container with `docker kill sakai`
 2. Remove the sakai container with `docker rm sakai`


