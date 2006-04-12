<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" version="2.0"
><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		/><jsp:text
		><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
	</jsp:text>
	<c:set var="viewBean" value="${requestScope.rsacMap.viewBean}" />
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
							<span class="rwiki_pageLinks">
								<!-- Home Link -->
								<jsp:element name="a"><jsp:attribute name="href"><c:out value="${homeBean.homeLinkUrl}"/></jsp:attribute><c:out value="${homeBean.homeLinkValue}"/></jsp:element>
							</span>
						</form>
					</div>
	<jsp:directive.include file="breadcrumb.jsp"/>
	<h3>Permission Denied</h3>
	<p>You do not have the correct permissions</p>
		
</div>
</div>
	</jsp:element>
	</html>
</jsp:root>
