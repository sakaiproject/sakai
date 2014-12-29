<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo"%>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<f:view>
	<html xmlns="http://www.w3.org/1999/xhtml">
<head><%=request.getAttribute("html.head")%>
<title><h:outputText
		value="#{authorMessages.example_emi_question}" escape="false" /></title>
<script type="text/javascript">
	$(document).ready(function() {
		$('#closeBut').click(function() {
			window.close();
		});
	});
</script>
</head>
<body onload="<%=request.getAttribute("html.body.onload")%>">
	<h3 align="center">
		<h:outputText value="#{authorMessages.example_emi_question}"
			escape="false" />
	</h3>
	<br />
	<table width="95%" align="center" border="0" cellspacing="5"
		class="act greyBox">
		<tr bgcolor="<%="point".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"%>">
			<td width="30%" align="center" valign="top"><b>
				<a name="point"><h:outputText value="#{authorMessages.emi_whats_answer_point_title}" escape="false" /></a>
			</b></td>
			<td>
				<h:outputText value="#{authorMessages.emi_whats_answer_point}" escape="false" />
			</td>
		</tr>
		<tr bgcolor="<%="theme".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"%>">
			<td align="center" valign="top"><b>
				<a name="theme"><h:outputText value="#{authorMessages.emi_whats_theme_title}" escape="false" /></a>
			</b></td>
			<td>
				<h:outputText value="#{authorMessages.emi_whats_theme}" escape="false" />
			</td>
		</tr>
		<tr bgcolor="<%="options".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"%>">
			<td align="center" valign="top"><b>
				<a name="options"><h:outputText value="#{authorMessages.emi_whats_options_title}" escape="false" /></a>
			</b></td>
			<td>
				<h:outputText value="#{authorMessages.emi_whats_options}" escape="false" />
			</td>
		</tr>
		<tr bgcolor="<%="leadin".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"%>">
			<td align="center" valign="top"><b>
				<a name="leadin"><h:outputText value="#{authorMessages.emi_whats_leadin_title}" escape="false" /></a>
			</b></td>
			<td>
				<h:outputText value="#{authorMessages.emi_whats_leadin}" escape="false" />
			</td>
		</tr>
		<tr bgcolor="<%="items".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"%>">
			<td align="center" valign="top"><b>
				<a name="items"><h:outputText value="#{authorMessages.emi_whats_items_title}" escape="false" /></a>
			</b></td>
			<td>
				<h:outputText value="#{authorMessages.emi_whats_items}" escape="false" />
			</td>
		</tr>
	</table>
	<br />
	<div align="center">
		<h:commandButton id="closeBut" value="#{authorMessages.button_close}"
			styleClass="active" />
	</div>
</body>
	</html>
</f:view>
