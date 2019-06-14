# Sakai Docker Development Examples

A suite of tools for developing Sakai using Docker Swarm.

### Features
Features of the files you will find here:

* Staged development stack.
* All data persisted locally in ./DATA
* Shell-In-A-Box (browser console) maven image
* Dockerfile for building static image from within dev folder
* Swarm stacks designed to be layered
  * Proxy + Sakai (No Search) + Mysql + Maven
  * Proxy + Sakai (No Search) + Mysql + Maven + PhpMyAdmin + MailCatcher
  * Proxy + Sakai (Search Enabled) + Mysql + Maven + PhpMyAdmin + MailCatcher + Cerebro + Kibana
  * Proxy + Sakai (Search Enabled) + Mysql + Maven + PhpMyAdmin + MailCatcher + Cerebro + Kibana + Graylog (Aggregation Stack)

# Quick install Docker (Linux, new server/workstation)
Docker provides an installation script for most Linux distributions, With Ubuntu and RHEL/CentOS being the most used. 
This script is located at https://get.docker.com/ and has the following instructions at the top of the file:

    This script is meant for quick & easy install via:
    $ curl -fsSL https://get.docker.com -o get-docker.sh
    $ sh get-docker.sh

Swarm mode must be enabled to use this Docker Swarm Stack based development environment

    $ docker swarm init 

# Stage 1 (Sakai Checkout & Build)
The first stage compose file creates Sakai, Mysql, and Maven services. The maven service includes Shell In A Box at http://127.0.0.1:8080/console/ for easy access.

![Maven+Sakai](DATA/ROOT/images/stack_base.png?raw=true "Services")

 1. Deploy the stack using `docker stack deploy -c sakai_dev.yml SakaiStudio`
 1. Wait for startup, you can monitor with `docker service logs -f SakaiStudio_sakai`
 1. After startup connect to http://127.0.0.1:8080 and click the "Maven Console" tile.
 1. Login with user:root and password:toor
 1. You will be in the /source folder, if not `cd /source` (This folder is mapped to ./sakai/source)
 1. Clone the code repo `git clone https://github.com/sakaiproject/sakai.git`
 1. Enter the code folder `cd sakai`
 1. Build the code `mvn clean install sakai:deploy` (adding any other preferred build options like -T <threads> or -Dmaven.test.skip=true)
 1. After the build completes, tomcat will need to be restarted, from the host (not the maven console) run `docker service update --force SakaiStudio_sakai`
 1. Wait for tomcat to startup, it may take a long while while the DB scheme is being created.
 1. After startup connect to http://127.0.0.1:8080 and click the "Sakai LMS" tile.

# Stage 1.1 (Optional static image build)
Once the sakai source has been built, you can build a sakai:latest static docker image to be used with the examples in [../deploy](../deploy)

 1. Run `docker build -t sakai .` from this folder 

# Stage 2 (Above + Basic Dev Tools)
The second stage adds MailCatcher and PhpMyAdmin to the stack.

![Maven+Sakai+Mailcatcher+PhpMyAdmin](DATA/ROOT/images/stack_tools.png?raw=true "Services")

 1. Update the running stack using the stage 2 compose file `docker stack deploy -c sakai_dev_tools.yml SakaiStudio`
 1. Wait for startup, you can monitor with `docker service logs -f SakaiStudio_sakai`
 1. After startup connect to http://127.0.0.1:8080 to see the list of services

# Stage 3 (Above + ES Tools, Sakai Search enabled)
The third stage enables Sakai's built in search, and adds Cerebro and Kibana for working with elasticsearch.

![Maven+Sakai+Mailcatcher+PhpMyAdmin+Cerebro+Kibana](DATA/ROOT/images/stack_search.png?raw=true "Services")

 1. Update the running stack using the stage 2 compose file `docker stack deploy -c sakai_dev_tools_search.yml SakaiStudio`
 1. Wait for startup, you can monitor with `docker service logs -f SakaiStudio_sakai`
 1. After startup connect to http://127.0.0.1:8080 to see the list of services

# Stage 4 (Above + Log Aggregation)
The fourth stage adds full log aggregation via Graylog, and configures everything in the stack to send logs to it via UDP/GELF

![Maven+Sakai+Mailcatcher+PhpMyAdmin+Cerebro+Kibana+Graylog](DATA/ROOT/images/stack_full.png?raw=true "Services")

 1. Update the running stack using the stage 2 compose file `docker stack deploy -c sakai_dev_full.yml SakaiStudio`
 1. Wait for startup, you can monitor eith Graylog at http://127.0.0.1:8080/graylog/
 1. After startup connect to http://127.0.0.1:8080 to see the list of services


