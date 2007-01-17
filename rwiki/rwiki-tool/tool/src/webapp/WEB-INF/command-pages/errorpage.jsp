<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 FIXME: i18n
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" version="2.0"
 xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
><jsp:directive.page isErrorPage="true" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
/><jsp:scriptlet>{ response.resetBuffer(); }</jsp:scriptlet><jsp:text
><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
	</jsp:text>
	<c:set var="viewBean" value="${requestScope.rsacMap.viewBean}" />
	<c:set var="homeBean" value="${requestScope.rsacMap.homeBean}" />
	<c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
	
	<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Permission Denied</title>
	<jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
	</head>
	<jsp:element name="body">
		<jsp:attribute name="onload">
			<jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
		<div id="rwiki_container">
			<div class="portletBody">
				<div class="navIntraTool">
					<form action="?#" method="get" class="rwiki_searchForm">
						<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="false"
							useEditLink="false"
							useInfoLink="false"
							useHistoryLink="false"
							useWatchLink="false"
							homeBean="${homeBean}"  
							resourceLoaderBean="${rlb}"
							
						        />
					</form>
				</div>
				<jsp:directive.include file="breadcrumb.jsp"/>
				<jsp:scriptlet>
					if ( exception instanceof javax.servlet.jsp.el.ELException) {
					    Throwable t = ((javax.servlet.jsp.el.ELException) exception).getRootCause();
					    if (t instanceof Exception) {
					        exception = (Exception) t;
					    }
					}
				
					if ( exception instanceof uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException ) {
				</jsp:scriptlet>
	<h3>Permission Denied</h3>
	<p>You do not have the correct permissions.</p>
				<jsp:scriptlet>
					} else {
				</jsp:scriptlet>
				<h3>The Action encountered a problem.</h3>
				<p>Please report this problem to <a href="http://bugs.sakaiproject.org/jira/browse/SAK" >JIRA</a></p>
				<pre>
    					<jsp:scriptlet>
    					    
    						out.write(" URL:"+request.getRequestURL()+"?"+request.getQueryString()+"\n");
    						java.util.Enumeration e = request.getParameterNames();
    						while(  e.hasMoreElements() ) {
    							String name = (String)e.nextElement();
    							String[] vals = request.getParameterValues(name);
    							out.write(name+" : "+vals[0]+"\n");
    							for( int ie = 1; ie &lt; vals.length; ie++ ) {
    							  	out.write("    :"+vals[ie]+"\n"); 
    							}
    						}
    						if ( exception != null ) { 
    							exception.printStackTrace();
    						}
						out.write("Error: ");
						try { 
							out.write(exception.getClass().getName()); 
						} catch ( Exception ex ) { 
							out.write("Unknown Exception Class");
						}
					    	out.write("\n");
    						out.write("     : ");
						try { 
							out.write(exception.getMessage()); 
						} catch ( Exception ex ) { 
							out.write("Unknown Message"); 
						}
    						out.write("\n");
    						java.io.PrintWriter pw = new java.io.PrintWriter(out);
						try { 
							exception.printStackTrace(pw);
						} catch ( Exception ex ) { 
							out.write("Unknown Stack Trace");
						}
    						
					</jsp:scriptlet>						
					
				</pre>
				<jsp:scriptlet>
					}
				</jsp:scriptlet>
			</div>
</div>
	</jsp:element>
	</html>
</jsp:root>
