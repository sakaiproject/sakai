Site Stats WebServices
============================================

1. About
2. Installation instructions
3. Call SiteStats webservice
4. Contact



1. About
============================================
Site Stats WebServices offer the ability to invoke some SiteStats API calls through webservices.


2. Installation instructions
============================================
From now on, we will assume that:
    - Tomcat folder is located at [TOMCAT_HOME]
    - Sakai source folder is located at [SAKAI_SRC]
    - SiteStats source folder is located at [SITESTATS_SRC]

2.1 Adding the *.jws files
    * cp [SITESTATS_SRC]/util/webservices/*.jws [SAKAI_SRC]/webservices/axis/src/webapp

2.2 Compile & deploy webservices
    Webservices compilation and deployment can be made *live* (without stopping Sakai tomcats),
    as long as there are no webservices running/processing data (these will be aborted).
    * go to [SAKAI_SRC]/webservices
    * run: mvn -Dmaven.tomcat.home=[TOMCAT_HOME] clean install sakai:deploy
		
2.3 Configure Sakai webservices (if not already done)
    You can read the following article from Steve Swinsburg about Sakai webservices:
    http://steve-on-sakai.blogspot.com/2009/05/enabling-web-services-in-sakai-and.html
    
    
3. Call SiteStats webservice
============================================
    You can use a custom SOAP client (java code, shell script, ...) to invoke 
    the SiteStats webservices calls. Alternativelly, you can use 3rd Party tools:
        - SOAP Client (Mac): http://ditchnet.org/soapclient/
        - SoapUI (Windows): http://www.soapui.org/
        - WebServiceStudio (Windows): http://webservicestudio.codeplex.com/ 


4. Contact
============================================
SiteStats is written by Nuno Fernandes at Universidade Fernando Pessoa.
If you wish, feel free to submit patches or any other contributions.
You may contact us at ufpuv-suporte@ufp.pt.
