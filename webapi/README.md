# Sakai Web API

This project hosts various web api endpoints for comsumption in client side tools such as curl, or
scripting languages such as Python, Ruby, etc. The endpoints mount under /api and have been designed
to be RESTful and predictable.

We're using [Spring REST docs](https://spring.io/projects/spring-restdocs) to generate a set of
[asciidocs](https://asciidoc.org/) that can be used to document the api. Each time a full build with
tests is made, the asciidocs are generated as part of the unit tests. If a unit test fails, no
asciidocs will be generated. The layout of the final documentation html file is authored in
src/docs/asciidoc/webapi.adoc using include directives, and each controller has its own adoc file
under src/docs/asciidoc/controllers/[controller name]. This approach allows us to combine manually
authored docs with auto-generated snippets as an output of our test process. If you are developing
some tests and asciidocs for a controller, you can copy the src/docs/asciidoc/controllers/\_example
directory and start from there.

Once you've run a successful build and deployed the webapi project, the docs will be available at
<http://localhost/api/docs/webapi.html>

This documentation approach is a work in progress and we will be adding
the rest endpoints one by one.

