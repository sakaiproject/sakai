TEST AND QUIZZES (SAMIGO) DESIGN DOCUMENT
-----------------------------------------
Ed Smiley, October 21, 2005

A. Module design.
 1. There are four subprojects in Samigo.
  a. tool.  This has the webapps and application/ui-related code. (samigo.war)
  b. api.  This has the service interfaces. (sakai-samigo-api-{VERSION}.jar)
  c. component.  This has all the implementation logic components to support the
     service interfaces, and all the spring configurations.
     It is included in samigo.war lib.  (sakai-samigo-component-{VERSION}.jar)
  d. audio. Code for audio recorder applet. (sakai-samigo-audio-{VERSION}.jar)
 2. Java primary package layout.
  a. Samigo Application packages (samigo.war)
    org.sakaiproject.tool.assessment:
     api       (new package, factory for getting the Samigo api impls)
     bundle    (properties for i18n)
     jsf       (JSF tags and renderers)
     settings  (internal setup stuff)
     ui        (listeners and backing beans etc.)
     ws        (web services)
  b. Samigo API (sakai-samigo-api)
    org.sakaiproject.tool.assessment:
     data.ifc     (abstract representation of the data)
     data.model   (data modeling)
     shared.api   (the external service apis)
  c. Samigo Components (sakai-samigo-component)
    org.sakaiproject.tool.assessment:
     business    (some model.business helper classes)
     data.dao    (data implementations)
     facade      (data behavioral objects)
     integration (overlays for standalone and integrated)
     osid        (osid implementations --inside the facade covers)
     qti         (QTI import export logic)
     services    (internal services)
     shared.impl (external api impls)
     util        (misc utils)
    org.sakaiproject.spring (spring configuration files and utilities)

B. Service Design.
 see: service_api_design.txt

C. Internationalization Localization of text
 Text resources are located in org.sakaiproject.tool.assessment.bundle.

D. Import/Export in QTI format
 see: README.import_export.txt

E. Facade architecture
 1. see: facade_design_decison.txt:
  explains the design on
    org.sakaiproject.tool.assessment.facade classes,
    org.sakaiproject.tool.assessment.ifc interfaces
  and the classes that implement it in
    org.sakaiproject.tool.assessment.data.dao
 2. see: facade_architecture_design.gif:
  diagram explaining the functioning of facades.

F. Standalone and Sakai-integrated integration context
  1. The internal facade queries apis are made integration context sensitive
(integrated in Sakai vs standalone) through Spring injection of the context-
appropriate implementation, as detailed in the BeanDefinitions.xml, or
BeanDefinitionsStandalone.xml, and are supplied by the PersistenceService
factory class which delivers the appropriate implementation.  This is packaged
in the component jar.
  2. The internal facades (see E., above) delegate their context-driven behavior
to context-specific helper classes, as defined in integrationContext.xml, and
integrationContextStandalone.xml) which are supplied to the application
through the IntegrationContextFactory.   This is packaged
in the component jar.
  3.  The external service implementations are Spring-injected but provide a
context-independent level of abstraction, and use the same samigoApi.xml
regardless of integration context.  This is possible because of the injection of
the context-specific behaviors detailed in F.1,2, above.
