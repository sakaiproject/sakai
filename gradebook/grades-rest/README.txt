This is project which creates a REST endpoint for Sakai to interact with grades and related data (like course listings and students)

Building the project is simple:
mvn clean install

Deploy:
mvn sakai:deploy 
OR copy the built artifact directly into tomcat (remove the version number from the end of the war file).

This will create a new endpoint at /direct/grades which allows for retrieval and imports of gradebook data for one or many students.

WARNING: This is a proof of concept and while the parts have been generally tested (even under load) it should be used with caution.

Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)