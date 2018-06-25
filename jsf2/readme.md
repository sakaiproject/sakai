# Migration guide of Sakai tools to JSF 2.3
Sakai tools use a legacy unsupported version of JSF (1.2) and the goal is to get to a more recently released version (2.3).

This migration guide has been built under the SVCfunds 2017 project, inside the Chat JSF Upgrade (Phase 1), which is the first tool that has been migrated to JSF 2.3 from 1.1_02.

## First steps
The first steps in the migration process was to find the dependencies between the Chat tool and Sakai’s JSF component, check the versions and predict the consequences. The plan was to modify as little code from the Chat tool as possible, but also it was important to save efforts in making changes that could be overridden in future migrations.

The modification flow we followed could be something like this:
```
pom.xml files (JSF&CHAT) 
-> taglib (xml and tld files at JSF)
   -> faces-config.xml files (JSF&CHAT)  
        -> web.xml files (JSF&CHAT) 
            -> jsp files (CHAT)
```
This process was re-evaluated several times as we were finding and solving issues and detecting compatibility problems.

See the **README-JSF2.md** file to get more details about the JSF2 project migration, project which this guide relies on.

## Chat tool migration example
First of all, open all the **pom.xml** files from the VIEW layer (Usually called **tool** or **app**).
### Replace all the JSF project dependencies.

Replace all the JSF project dependencies, under the groupId **org.sakaiproject.jsf**, to use the JSF2 dependencies.
Before:
```
<dependency>
    <groupId>org.sakaiproject.jsf</groupId>
    <artifactId>jsf-tool</artifactId>
</dependency>
<dependency>
    <groupId>org.sakaiproject.jsf</groupId>
    <artifactId>jsf-widgets-sun-depend</artifactId>
    <type>pom</type>
</dependency>
```
After:
```
<dependency>
    <groupId>org.sakaiproject.jsf2</groupId>
    <artifactId>jsf2-tool</artifactId>
</dependency>
<dependency>
    <groupId>org.sakaiproject.jsf2</groupId>
    <artifactId>jsf2-widgets-sun-depend</artifactId>
    <type>pom</type>
</dependency>
```
### Upgrade Tomahawk to Tomahawk21, remove taglibs:standard and add the new JSF2.3 dependencies
Before:
```
<dependency>
    <groupId>org.apache.myfaces.tomahawk</groupId>
    <artifactId>tomahawk</artifactId>
    <version>1.1.14</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>taglibs</groupId>
    <artifactId>standard</artifactId>
    <version>1.0.4</version>
</dependency>
```
After:
```
<!--JSF 2.3 dependencies-->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>${sakai.jstl.version}</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>org.jboss</groupId>
    <artifactId>jandex</artifactId>
    <version>2.0.5.Final</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>org.jboss.weld.servlet</groupId>
    <artifactId>weld-servlet-shaded</artifactId>
    <version>3.0.4.Final</version>
</dependency>
```

### Create two new files under the WEBAPPS folder.
New file **webapp/META-INF/context.xml**
```
<Context>
    <Resource name="BeanManager" 
        auth="Container"
        type="javax.enterprise.inject.spi.BeanManager"
        factory="org.jboss.weld.resources.ManagerObjectFactory" />
</Context>
```
New file **webapp/WEB-INF/beans.xml**
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"
   bean-discovery-mode="all" version="2.0">
</beans>
```

### Replace the faces-config.xml header (Usually under webapp/WEB-INF/faces-config.xml)
Before:
```
<!DOCTYPE faces-config PUBLIC
  "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"
  "dtd/web-facesconfig_1_0.dtd">

<faces-config>
```

After:
```
<faces-config xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
        http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_3.xsd"
    version="2.3">
```

### Update the web.xml header (Usually under webapp/WEB-INF/web.xml)
Before:
```
<web-app id="WebApp_9" version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
```

After:
```
<web-app
    xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
    version="3.0">
```

### Remove the verifyObjects configuration
```
<context-param>
    <param-name>com.sun.faces.verifyObjects</param-name>
    <param-value>true</param-value>
</context-param>
```

### Replace some Sakai custom tags to JSF standard tags in the JSP files

- Replace "sakai:messages" by "h:messages"
- Replace "sakai:button_bar_item" by "h:commandButton"

### Replace the core_rt taglib now we're using JSTL 1.2
Before:
```
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
```

After:
```
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
```

### Replace all the packages and components from org.sakaiproject.jsf to org.sakaiproject.jsf2
Before:
```
org.sakaiproject.jsf.util.HelperAwareJsfTool
```

After:
```
org.sakaiproject.jsf2.util.HelperAwareJsfTool
```

### Replace all the namespaces in the JSP files
Before:
```
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
```

After:
```
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
```

## Compile, Deploy & Run
Compile and deploy the JSF2 project first, then compile the migrated tool. Startup the application server (Tomcat), you'll notice some lines like this that reflects that Mojarra JSF has been loaded succesfully:
```
org.apache.catalina.startup.HostConfig.deployWAR Deploying web application archive [D:\opt\sakai\webapps\sakai-chat-tool.war]
com.sun.faces.config.ConfigureListener.contextInitialized Initializing Mojarra 2.3.5 ( 20180516-1910 bf35b0f6c540c69e80e6da962a2b62756838ac41) for context '/sakai-chat-tool'
com.sun.faces.spi.InjectionProviderFactory.createInstance JSF1048: PostConstruct/PreDestroy annotations present.  ManagedBeans methods marked with these annotations will have said annotations processed.
org.apache.catalina.startup.HostConfig.deployWAR Deployment of web application archive [D:\opt\sakai\webapps\sakai-chat-tool.war] has finished in [17,674] ms
```
Test, if possible, all the options and features of the migrated tool, checking every screen and every tag.

You're welcome to receive help about the migration in the [Sakai DEV list](mailto:sakai-dev@apereo.org)

