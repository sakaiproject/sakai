# JSF 1.1_02 project migration to JSF 2.3
The following document explains all the steps taken to migrate the JSF project from the 1.1_02 version to 2.3, choosing a single implementation (Mojarra) and removing other implementations (like MyFaces).

## Provider decision
The old provider wasn’t updated since 2011:
https://mvnrepository.com/artifact/javax.faces/jsf-api

We decided to use [![N|Mojarra](https://javaserverfaces.github.io/images/jsf-logo-no-text-32.png)](https://javaserverfaces.github.io) Mojarra JavaServer Faces - Oracle's open source implementation of the JSF standard.

https://mvnrepository.com/artifact/javax.faces/javax.faces-api
https://mvnrepository.com/artifact/org.glassfish/javax.faces

because their version releases are usual, it has a good bug detection/solving ratio and you can find many usages/community work. Mojarra is already being used on Sakai on some other JSF components like Samigo or SignUp. Also, the impl and general structure seems to be closer to the previous specification and the transition seemed smoother code-wise.

We picked the 2.3.5 version of the implementation because it was recent, quite robust and had more usages, but also tried other minor versions (2.2.17) and the difference was not noticeable at this stage.

Anyway, this decision is still open for discussion and should not affect the next stages of the project.

To use the 2.3 specification (which is the most recent, wishlist point) would have to be different and requires extra code modifications.

## JSF project migration
The first changes were done under the "JSF" project, migrating from 1.1 to 2.3, this broke other JSF tools such as Forums, Samigo or Sign-UP. To keep the changes separate and independent, we decided to duplicate the "JSF" project, calling it "JSF2". Using the JSF2 dependencies for the chat tool kept the changes separated from other tools, not breaking anything.

The path to migrate other tools to JSF2 will be replace the JSF dependencies by the JSF2 dependencies. It was an update of the libraries and faces configuration files, implementing new methods from the abstract classes and removing other JSF implementations (MyFaces).

### Some examples of the code changes
#### Library versions
JSF project
```
<dependency>
    <groupId>javax.faces</groupId>
    <artifactId>jsf-api</artifactId>
    <version>1.1_02</version>
</dependency>
<dependency>
    <groupId>javax.faces</groupId>
    <artifactId>jsf-impl</artifactId>
    <version>1.1_02</version>
</dependency>
```
JSF2 project
```
<dependency>
    <groupId>javax.faces</groupId>
    <artifactId>javax.faces-api</artifactId>
    <version>2.3</version>
</dependency>
    <dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.faces</artifactId>
    <version>2.3.5</version>
</dependency>
```
#### Implement new methods from the abstract classes
```
	public String getWebsocketURL(FacesContext arg0, String arg1)
	{
		return m_wrapped.getWebsocketURL(arg0, arg1);
	}
```
#### Replace config files headers using the new DTDs
JSF
- jsf-app/src/META-INF/faces-config.xml
- jsf-app/src/jsf2/META-INF/faces-config.xml
- jsf/jsf-widgets/src/META-INF/faces-config.xml
```
<!DOCTYPE faces-config PUBLIC
  "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"
  "http://java.sun.com/dtd/web-facesconfig_1_1.dtd">
```
JSF2
- jsf2-app/src/META-INF/faces-config.xml
- jsf2-app/src/jsf2/META-INF/faces-config.xml
- jsf2/jsf2-widgets/src/META-INF/faces-config.xml
```
<faces-config xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
        http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_3.xsd"
    version="2.3">
```

JSF
- jsf/jsf-widgets/src/META-INF/sakai-jsf.taglib.xml
```
<!DOCTYPE facelet-taglib PUBLIC
  "-//Sun Microsystems, Inc.//DTD Facelet Taglib 1.0//EN"
  "facelet-taglib_1_0.dtd">
```
JSF2
- jsf2/jsf2-widgets/src/META-INF/sakai-jsf.taglib.xml
```
<facelet-taglib version="2.2"
    xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-facelettaglibrary_2_2.xsd">
```

JSF
- jsf/jsf-widgets/src/META-INF/sakai-jsf.tld
```
<!DOCTYPE taglib
  PUBLIC "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"
  "http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd">
```
JSF2
- jsf2/jsf2-widgets/src/META-INF/sakai-jsf.tld
```
<taglib
  xsi:schemaLocation="http://java.sun.com/xml/ns/javaee web-jsptaglibrary_2_1.xsd"
  xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  version="2.1">
```

JSF
- jsf/jsf-resource/src/webapp/WEB-INF/web.xml
```
<web-app id="WebApp_9" version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
```
JSF2
- jsf2/jsf2-resource/src/webapp/WEB-INF/web.xml
```
<web-app
    xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
    version="3.0">
```

#### Upgrade the JSTL library (Requires 1.2) and Tomahawk to Tomahawk21
Before:
```
 <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>1.0.2</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.myfaces.tomahawk</groupId>
    <artifactId>tomahawk</artifactId>
    <version>1.1.14</version>
</dependency>
```
After:
```
 <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>jstl</artifactId>
    <version>1.2</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.myfaces.tomahawk</groupId>
    <artifactId>tomahawk21</artifactId>
    <version>1.1.14</version>
</dependency>
```

