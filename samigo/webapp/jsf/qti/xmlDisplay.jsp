<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" language="java" %><%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %><%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %><%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %><%-- $Id: xmlDisplay.jsp,v 1.5 2005/02/03 03:48:44 esmiley.stanford.edu Exp $
--%><f:view><h:outputText value="#{xml.xml}" escape="false" /></f:view>
<%-- do not add extra lines into this before the output, as it will casue problems in Mozilla --%>
