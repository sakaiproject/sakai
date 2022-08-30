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

### Design Principles

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

### Technology Stack

Rubrics is developed using modern technologies to leverage the emerging
next generation of web development:

* Java (Java 8+)
    * [Spring Data JPA](http://projects.spring.io/spring-data-jpa/)
    * [Spring HATEOAS](http://projects.spring.io/spring-hateoas/)
* JavaScript, HTML5, CSS
    * [Web Components](https://www.webcomponents.org/)
    * [Lit](https://www.lit.dev)
