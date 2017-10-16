# Rubrics

Rubrics is a project which adds native rubrics support to Sakai.

## Background

This was developed as part of the [Apereo FARM - Sakai Native Rubrics](http://farm.apereo.org/home/current-projects/in-progress/sakai-native-rubrics)
initiative.

This was a collaborative effort between [Longsight, Inc.](https://www.longsight.com/) and [Unicon, Inc.](https://www.unicon.net/)
Longsight worked on the community requirements gathering and initial
designs with [New York University](https://www.nyu.edu/) and others, and then Unicon
developed the initial release on behalf of and supported by the
[Uniformed Services University (USU)](https://www.usuhs.edu/) as part of the first development phase.

The initial designs focused primarily on rubrics support within
Gradebook NG, however, after some rounds of designs and prototypes put
forth by Unicon and driven by USU requirements shifted the focus of the
first phase of development on the core Rubrics service and Assignment
Tool integration.

The framework is in place for integration into Gradebook and potentially
other tools such as Forums and Samigo.

## Architecture / Technology

The technology proposal put forth by Unicon and reviewed by Longsight
significantly departed from traditional Sakai design patterns. The focus
of this solution was to develop a fully encapsulated, lightweight and
reusable solution based on a micro-service architectural pattern with
client view components so that it can be directly leveraged within
multiple tools with minimal development required for each tool
integration.

### Design Principles

#### Separation of Concerns
The Rubrics service for Sakai is built as a separate service (not added
to GradebookNG or any other existing tool) in order to be able to work
with any Sakai configuration and not be tightly coupled to any other
specific tools (for example where legacy Gradebook is used or no
Gradebook is used at all). Tools should interface with Rubrics only
through either the client view layer components which encapsulate the
necessary web services calls to deliver various use cases, or sparingly
via Restful web service API access (directly or via the provided Service
Client API).

#### Single Responsibility Principle
The sole focus of the Rubrics service should be on supporting APIs and a
set of views to deliver the functional use cases for rubrics:

* Managing rubrics (create, edit, delete, copy, sharing/access control)
* Searching for, selecting and associating rubrics with external entities
(gradebook entries, assignments, short answer assessment questions, forums,
etc.)
* Delivery of the rubric for evaluation
* Delivery of the rubric report/outcome for review

#### Reusability & Consistency
The rubrics service centralizes all rubrics persistence and provides
a set of views to be embedded and used by all tools within Sakai.

The Rubrics service provides a set of APIs to persist the mapping of
external tool items so that every integrating tool does not have to
modify their persistence model and maintain those relationships. The
Tools may store certain rubrics resource identifiers internally, but
that is not required, as long as the tool can produce unique tool item
association IDs within its own namespace that it can use for lookup.

#### Security / Authorization
The responsibility for establishing user authentication and determining
roles and permissions authorization is left to the core of Sakai and
each tool to pass along the necessary site context and user information
including roles via the [JSON Web Token (JWT)](https://jwt.io/) standard.

Rubrics are scoped to a context, which is site based, but there is
flexibility to allow for other contexts like personal buckets to be added
as enhancements. This means that rubrics are created within the context
of a site and visible only within a site.

There is currently a global public sharing capability which allows for
a temporary or permanent share and copy in another site by the same
instructor or another instructor via the public list. Targeted sharing
to specific sites or users is not yet supported.

There are four new permissions added to the Sakai Realms to be mapped
appropriately:

* **rbcs.editor** - Authorized to create, edit and share rubrics.
* **rbcs.associator** - Authorized to associate rubrics with integrating tool items.
* **rbcs.evaluator** - Authorized to use a rubric and submit an evaluation with comments based on the rubric.
* **rbcs.evaluee** - Target of a rubric evaluation, only able to view rubrics and evaluations for their tool item submissions.

Typically an instructor will have all four permissions granted for a site,
and students will only have evaluee. Those details are left to the Sakai
Administrators to set up the appropriate mappings. Note that support for
peer review rubrics has not yet been implemented.

Use of JWT supports the decoupling goal which allows Rubrics to
optionally run in a separate JVM.

Example of JWT required payload with standard and custom claims for
Rubrics.
```
{
  "iss": "sakai",
  "aud": "rubrics",
  "sub": "cda94cae-83b8-4d44-9df7-0c0fe9269500",
  "toolId": "sakai.assignment",
  "roles": [
    "rbcs.editor",
    "rbcs.associator",
    "rbcs.evaluator",
    "rbcs.evaluee"
  ],
  "contextId": "50100776-7540-4d70-8840-9579d09a5408",
  "contextType": "site",
  "sessionId": "0d00c1f0-c5b6-4b1b-b51b-2c13595554ff",
  "exp": 1486169472,
  "iat": 1486155072
}
```
sub is the principal/user ID this token is representing.

The Rubrics Service Client API provides a method to generate a JWT with
only a tool ID, and it gathers all of the other information from Sakai.

Tools should leverage the Rubrics Service Client API to easily generate
JWTs to communicate with Rubrics REST services directly, and also use
the client API methods to simplify integration for those few places
where embedding a rubrics web component does not fulfill the need.

### Technology Stack

Rubrics is developed using modern technologies to leverage the emerging
next generation of web development:

* Java (Java 8+)
    * [Spring Boot](http://projects.spring.io/spring-boot/)
    * [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
    * [Spring Data REST](http://projects.spring.io/spring-data-rest/)
    * [Spring HATEOAS](http://projects.spring.io/spring-hateoas/)
    * [Spring Security](http://projects.spring.io/spring-security/)
* JavaScript, HTML5, CSS
    * [Web Components](https://www.webcomponents.org/)
    * [Polymer](https://www.polymer-project.org/1.0/)
* [JSON Web Tokens (JWT)](https://jwt.io/) see also [RFC 7519](https://tools.ietf.org/html/rfc7519)

The services module is intended to be completely standalone and only
communicate with core Sakai via Sakai Web Services. As a result, this
decouples the web application and allows it to leverage unique versions
of Spring and libraries like Spring Boot and Spring Data REST without
impacting the whole of Sakai.

Web Components and Polymer were selected to develop fully encapsulated
reusable components which can essentially be dropped into any tool markup
with only a thin JavaScript bridge library to support it.

The Tool and API/Impl modules are more traditional Sakai backend
technologies using Spring and Wicket, however, the API/Impl does leverage
Spring HATEOAS and its Traverson client to navigate the
[Level 3 Hypermedia Restful APIs](https://martinfowler.com/articles/richardsonMaturityModel.html) provided.

### Vision
The architecture for Rubrics provides a step towards decoupling Sakai
tools from the core/kernel, which in theory will allow for a more
modular deployment architecture where tools can be deployed and scaled
independently from the core/kernel to address load and performance needs.

The Rubrics Services Spring Boot application needs a bit more work to
support the separation, but it will be able to be run in a separate JVM
or JVM cluster, ideally as a dedicated Docker container.

Splitting out tools into separate micro-services able to be packaged as
Docker containers will allow for the full power of containerization,
auto-scaling and clustering technologies such as [Docker Swarm](https://www.docker.com/products/docker-swarm) or
[Kubernetes](https://kubernetes.io/) to eventually be leveraged by Sakai as an evolutionary step
beyond the current monolith [Sakai Docker support](https://github.com/sakaicontrib/docker-sakai).

### Modules
The Rubrics module consists of three key artifacts:

#### Rubrics Service Client API
Contained in the 3 sub-modules:

  * **api**  - Contains the Rubrics Service Client API and DTO model
  * **impl** - Contains the Rubrics Service Client implementation
  * **pack** - Standard Sakai Component package module

Provides Rubrics Service client code and simple data transfer model for
use by tools.

#### Rubrics Tool
Contained in the sub-module: **tool**

Provides the Rubrics Management Tool for creating, editing and sharing
rubrics within a site. This is a very lightweight tool which itself is
an example of the view component integration. The actual management
capabilities are provided by the web components and rubrics web services
directly, this tool is just a shell for embedding the rubric manager
component.

#### Rubrics Web Services & Web Components
Contained in the sub-module: **services**

Provides the core rubrics [Level 3 Hypermedia Restful APIs](https://martinfowler.com/articles/richardsonMaturityModel.html), model persistence
and embeddable web components.

## Deployment / Configuration Requirements

The Rubrics service Spring Boot has its own application.properties with
much of its default configuration. Spring Boot is highly configurable
with [common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html). 

In order to manage all configuration
within sakai.properties, add the following to the Tomcat startup environment
(via JAVA_OPTS):
```
-Dspring.config.location=/path/to/sakai.properties
```
This loads sakai.properties into the Rubrics services layer and allows
for any properties in
rubrics/services/src/main/resources/application.properties to be
overridden.
Sakai properties are ignored, only [Spring Boot common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
and rubrics.* specific properties are used.

The following properties should be overridden:
```
spring.datasource.url=jdbc:mysql://server/sakaidb
spring.datasource.username=sakai
spring.datasource.password=sakai

rubrics.integration.sakai-rest-url=http://sakai-core:8080/sakai-ws/rest/

rubrics.integration.token-secret=<Unique secret to support JWT signing>
```

Rubrics can be loaded into the same database as Sakai core, or a
separate database. The spring.datasource settings above must be
configured either way.

NOTE: The Sakai web services requires access, so the configuration
should allow at least localhost to access
```
webservices.allowlogin=true
webservices.allow=localhost
```

Rubrics uses [Spring Boot type-safe configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-typesafe-configuration-properties) to load a very complex
configuration set, including defaults for the rubrics data model for the
instance, which supports multiple languages.
