<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <br/>
  <h:outputLabel value="#{questionPoolMessages.t_tags}"/>
  <jsp:text><![CDATA[</br>

]]></jsp:text>
  <t:dataList value="#{itemValue.itemTagSet.toArray()}" var="tag" layout="unorderedList">
    <f:verbatim>
      <jsp:text><![CDATA[<span>]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{tag.tagLabel}"/>
    <f:verbatim>
      <jsp:text><![CDATA[<span class="collection">]]></jsp:text>
    </f:verbatim>
    <jsp:text>
    (</jsp:text>
    <h:outputText value="#{tag.tagCollectionName}"/>
    <jsp:text>)
    </jsp:text>
    <f:verbatim>
      <jsp:text><![CDATA[</span></span></br>  ]]></jsp:text>
    </f:verbatim>
  </t:dataList>
</jsp:root>
