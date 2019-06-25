# Sakai Docker Deployment Examples

A collection of tools and examples to demonstrate building and deploying Sakai using Docker and/or Docker Swarm.

### Features
Features of the files you will find here:

* Multiple build types: 
  * From source 
  * From binary release.
  * Automated via DockerHub

#### Table of Contents
- [Quick install Docker (Linux, new server/workstation)](#quick-install-docker--linux--new-server-workstation-)
- [Building the Image](#building-the-image)
  * [From a binary release](#from-a-binary-release)
  * [From source (github tag/branch)](#from-source--github-tag-branch-)


# Quick install Docker (Linux, new server/workstation)
Docker provides an installation script for most Linux distributions, With Ubuntu and RHEL/CentOS being the most used. 
This script is located at https://get.docker.com/ and has the following instructions at the top of the file:

    This script is meant for quick & easy install via:
    $ curl -fsSL https://get.docker.com -o get-docker.sh
    $ sh get-docker.sh

# Building the Image
There are two variants of this docker build, depending on your preference to build from source code or from a binary release. As well there is an example automated build to be used with docker hub.

## From a binary release
To build from a binary release use these steps:
 1. From this folder
 1. Execute `docker build --build-arg release=19.1 -t sakai -f ./Dockerfile.binary .` substituting "19.1" for the release you wish to build
 1. Upon completion you can execute `docker image ls sakai` to verify it's creation
    *     $ docker image ls sakai
          REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
          sakai               latest              369fde564591        5 seconds ago       2.55GB

## From source (github tag/branch)
The source build uses a multi-stage build, building an intermediate image with JDK and Maven in which to build Sakai, then building a Tomcat image using only the binary artifacts from the build container. This creates a smaller Sakai image that does not include Maven and all the build time libraries, source, etc.

To build from source use these steps:
 1. From this folder
 1. Execute `docker build --build-arg release=master -t sakai -f ./Dockerfile.source .` substituting "master" for the branch/tag you wish to build
 1. Upon completion you can execute `docker image ls sakai` to verify it's creation
    *     $ docker image ls sakai
          REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
          sakai               latest              78b32fe87fda        8 seconds ago       2.55GB

## Automated from DockerHub
The Dockerfiles here are configured to work with the build hook in the hooks folder.

See https://docs.docker.com/docker-hub/builds/ for detailed information on setting up automated builds
