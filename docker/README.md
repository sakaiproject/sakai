# Sakai Docker Deployment Examples

A collection of tools and examples to demonstrate developing, building and deploying Sakai using Docker Swarm.

### Features
Features of the files you will find here:

* Development stack examples
* Docker image building examples
* Docker Swarm deployment examples

# Quick install Docker (Linux, new server/workstation)
Docker provides an installation script for most Linux distributions, With Ubuntu and RHEL/CentOS being the most used. 
This script is located at https://get.docker.com/ and has the following instructions at the top of the file:

    This script is meant for quick & easy install via:
    $ curl -fsSL https://get.docker.com -o get-docker.sh
    $ sh get-docker.sh

This will install and prepare everything needed to build images and run them in standalone mode.
Additionally, docker has a built-in container orchestration platform called "Docker Swarm". Swarm needs to be enabled to use any of the deployment example scripts in `deploy/`, this can be enabled by simply running:

     $ docker swarm init 

# Development
In the `dev/` folder you will find an example staged Swarm stack that included everything needed to build and test sakai in development.

GOTO [Development README](dev/)

# Building
In the `build/` folder you will find examples for building a docker image for Sakai. Before deployment, you need to build the docker image you will deploy, examples are provided for building from source or a binary release.

GOTO [Building README](build/)

# Deployment
Deploying Sakai in Docker Swarm is easy, even with a full stack of supporting services.

GOTO [Deployment README](deploy/)
