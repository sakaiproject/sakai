<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page errorPage="index_error.jsp" %>
<html:html>
<head><%= request.getAttribute("html.head") %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Question Pools</title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>" bgcolor="#ffffff">
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><h2>View Question Pools</h2>
    </td>
  </tr>
</table>

<table class="tblMain">
  <tr class="tdindexTop">
    <td>Pool Name</td>
    <td>Description</td>
    <td>Parent Pool ID</td>
  </tr>
  <logic:iterate name="questionpool" id="pool" property="pools" type="org.navigoproject.ui.web.form.questionpool.QuestionPoolBean" indexId="ctr">
  <tr class='<%= (ctr.intValue() % 2==0 ?"trEven":"trOdd") %>'>
    <td><bean:write name='pool' property='name' /></td>
    <td><bean:write name='pool' property='description' /></td>
    <td><bean:write name='pool' property='parentPoolId' /></td>
  </tr>
  </logic:iterate>
</table>

<h3><br>
</h3>
</body>
</html:html>
