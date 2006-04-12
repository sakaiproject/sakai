<?xml version="1.0" encoding="UTF-8" ?>
<jspi:root xmlns:jspi="http://java.sun.com/JSP/Page" xmlns:core="http://java.sun.com/jsp/jstl/core" version="2.0">
  <jspi:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
<!--
  This is a fragment so it shouldnt have this in it.
  <jspi:text>
    <![CDATA[ <?xml version="1.0" encoding="UTF-8" ?> ]]>
  </jspi:text>
  <jspi:text>
    <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jspi:text>
-->
  <core:set var="permissionsBeanRight" value="${requestScope.rsacMap.permissionsBean}"/>
  <core:set var="permissionsBeanObject" value="${permissionsBeanRight.rwikiObject}"/>
  <core:choose>
  <core:when test="${rightRenderBean.hasContent}" >
    <div id="rwiki_sidebar" >
      <div class="rwiki_renderedContent">
      <core:out value="${rightRenderBean.renderedPage}" escapeXml="false"/>
    </div>
  <core:set target="${permissionsBeanRight}" property="rwikiObject" value="${rightRenderBean.rwikiObject}"/>
  <core:if test="${permissionsBeanRight.updateAllowed}">
    <span class="instruction" ><jspi:element name="a"><jspi:attribute name="href"><core:out value="${rightRenderBean.editUrl}"/></jspi:attribute>Edit: <core:out value="${rightRenderBean.localisedPageName}"/></jspi:element></span> 
  </core:if>
  <core:set target="${permissionsBeanRight}" property="rwikiObject" value="${permissionsBeanObject}"/>
    </div>
  </core:when>
  <core:otherwise>
  <!--  
  <core:set target="${permissionsBeanRight}" property="rwikiObject" value="${rightRenderBean.rwikiObject}"/>
  <core:if test="${permissionsBeanRight.updateAllowed}">
    <span class="rwiki_create_sidebar" ><jspi:element name="a"><jspi:attribute name="href"><core:out value="${rightRenderBean.editUrl}"/></jspi:attribute>add sidebar</jspi:element></span> 
  </core:if>
  -->
    <core:set var="rwikiContentStyle"  value="rwiki_content_nosidebar" />
    
  </core:otherwise>
  </core:choose>
</jspi:root>
