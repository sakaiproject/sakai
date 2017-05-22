# Full wsdl list (As of Sakai 11)

(Replace localhost:8080 with the URL of your server)
(Please see https://github.com/sakaiproject/sakai/blob/master/webservices/cxf/src/resources/applicationContext.xml for jaxws:endpoint 
 as new webservices may be added since this document was updated)

* http://localhost:8080/sakai-ws/soap/sakai?wsdl 
* http://localhost:8080/sakai-ws/soap/shortenedurl?wsdl
* http://localhost:8080/sakai-ws/soap/assignments?wsdl
* http://localhost:8080/sakai-ws/soap/contenthosting?wsdl
* http://localhost:8080/sakai-ws/soap/messageforums?wsdl
* http://localhost:8080/sakai-ws/soap/job?wsdl
* http://localhost:8080/sakai-ws/soap/login?wsdl
* http://localhost:8080/sakai-ws/soap/report?wsdl
* http://localhost:8080/sakai-ws/soap/testsandquizzes?wsdl
* http://localhost:8080/sakai-ws/soap/portallogin?wsdl

The RemoteHostFilter is still on all calls so this stuff is secured in the same was axis was via webservices.allow properties. To enable broadly for development:

```
webservices.allowlogin=true
webservices.allow = .*
```
