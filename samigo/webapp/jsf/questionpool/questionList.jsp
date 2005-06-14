<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page errorPage="index_error.jsp" %>
<jsp:useBean id="subpoolTree" scope="session" class="org.navigoproject.business.entity.questionpool.model.QuestionPoolTreeImpl" />
<jsp:useBean id="questionpool" scope="session" class="org.navigoproject.ui.web.form.questionpool.QuestionPoolForm" />

<html:html>
<head><%= request.getAttribute("html.head") %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/treeJavascript.js" %>
//-->
</script>

<title>Question List</title>
<link href="<%=request.getContextPath()%>/css/main.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/jsp/aam/stylesheets/nav.css" rel="stylesheet" type="text/css">

</head>
<body bgcolor="#ffffff" onload="collapseAllRows();flagRows();document.forms[0].reset();<%= request.getAttribute("html.body.onload") %>">

<div class="heading">Sakai Assessment Manager</div>


<logic:equal name="questionpool" property="fromAuthoring" value="1">
<br>
<div class="messageConfirm">
Open a question pool and select a question using a checkbox, 
then click "Add to Assessment"
</div>
<br>
</logic:equal>


<h1>
  <table width="100%">
    <tr>
      <td class="h1text">Question Pools: 
        <input type="button" name="badd" value="Add" onclick="document.location='<%=request.getContextPath()%>/startCreatePool.do?use=create'"/>
	<input type="button" name="bimport" value="Import" onclick="document.location='<%=request.getContextPath()%>/startImportPool.do'"/>

<% 
// settings stored in SAM.properties
if (!org.navigoproject.settings.ApplicationSettings.isPoolingUserAdminDisabled())
{ %>

<input type="button" name="buserAdmin" value="User Admin" onclick="document.location='<%=request.getContextPath()%>/userAdmin.do'"/>
<% } %>
</td>
      <td class="alignRight"> 
        <input type="button" name="badd" value="My Assessments" onclick="document.location='<%=request.getContextPath()%>/asi/author/selectAuthoredAssessment.do'"/>
    </td>
  </tr>
</table>

</h1>

<%@ include file="/jsp/aam/questionpool/poolTreeTable.jsp" %>

</body>
</html:html>
