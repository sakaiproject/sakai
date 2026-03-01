# Sakai Docker Deployment Examples

A collection of tools and examples to demonstrate building and deploying Sakai using Docker and/or Docker Swarm.

### Features
Features of the files you will find here:

* Multiple build types: 
  * From source 

#### Table of Contents
- [Quick install Docker (Linux, new server/workstation)](#quick-install-docker--linux--new-server-workstation-)
- [Building the Image](#building-the-image)
  * [From source (github tag/branch)](#from-source--github-tag-branch-)


# Quick install Docker (Linux, new server/workstation)
Docker provides an installation script for most Linux distributions, With Ubuntu and RHEL/CentOS being the most used. 
This script is located at https://get.docker.com/ and has the following instructions at the top of the file:

    This script is meant for quick & easy install via:
    $ `curl -fsSL https://get.docker.com -o get-docker.sh`
    $ `sh get-docker.sh`

# Building the Image
There is currently one variant of this docker build, to build from source code.

## From source (github tag/branch)
The source build uses a multi-stage build, building an intermediate image with JDK and Maven in which to build Sakai, then building a Tomcat image using only the binary artifacts from the build container. This creates a smaller Sakai image that does not include Maven and all the build time libraries, source, etc.

To build from source under bash, use these steps:

```bash
    cd docker

    docker build --build-arg GIT_CACHEBUST=$(date +%s) \
    --build-arg release=master -t sakai \
    -f ./Dockerfile.source .
```

Under Windows PowerShell use:

```powershell
    cd docker

    docker build `
      --build-arg GIT_CACHEBUST=$(Get-Date -UFormat %s) `
      --build-arg release=master -t sakai `
      -f .\Dockerfile.source .
```

substituting "master" for the branch/tag you wish to build

 1. Upon completion you can execute `docker image ls sakai` to verify it's creation
    *     $ docker image ls sakai
          REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
          sakai        latest    ac16fc4db9dd   About a minute ago   1.86GB

You can choose a repository and branch as follows:

To build from source under bash, use these steps:

```bash
    docker build --build-arg GIT_CACHEBUST=$(date +%s) \
       --build-arg repository=https://github.com/csev/sakai.git \
       --build-arg release=SAK-52129 -t sak-52129 -f ./Dockerfile.source .
```

Under Windows PowerShell use:

```powershell
    docker build `
      --build-arg repository=https://github.com/csev/sakai.git `
      --build-arg release=SAK-52129 `
      --build-arg GIT_CACHEBUST=$(Get-Date -UFormat %s) `
      -t sak-52129 `
      -f .\Dockerfile.source .
```

List your images by

 1. Execute `docker images` to see your images


# Running this image

## Starting MariaDB/MySQL
You'll need to have MySQL/MariaDB started up first. The default config of the sakai.properties if for MariaDB on localhost.

Here's a sample to startup MySQL from Docker

```bash
	# May want to include an opt for docker rm sakai-mariadb
	# Start it if we've already created it, unless we want to re-create
	docker run -p 127.0.0.1:53306:3306 -d --name="sakai-mariadb" --pull always \
	    -e "MARIADB_ROOT_PASSWORD=sakairoot" \
	    -v "./mysql/scripts:/docker-entrypoint-initdb.d" \
	    -d mariadb:10 --lower-case-table-names=1 || docker start "sakai-mariadb"
```

```

```powershell
	# May want to include an opt for docker rm sakai-mariadb
	# Start it if we've already created it, unless we want to re-create
	docker run -p 127.0.0.1:53306:3306 -d --name="sakai-mariadb" --pull always `
	    -e "MARIADB_ROOT_PASSWORD=sakairoot" `
	    -v "./mysql/scripts:/docker-entrypoint-initdb.d" `
	    -d mariadb:10 --lower-case-table-names=1 || docker start "sakai-mariadb"
```


## Starting up Sakai and linking to Maria start the tage you built above
    `docker run --rm -p 8080:8080 --name sakai-tomcat --link sakai-mariadb sakai`

    `docker run --rm -p 8080:8080 --name sakai-tomcat --link sakai-mariadb sak-52129`

