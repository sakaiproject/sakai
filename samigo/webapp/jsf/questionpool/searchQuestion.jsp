<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page errorPage="index_error.jsp" %>
<jsp:useBean id="subpoolTree" scope="session" class="org.navigoproject.business.entity.questionpool.model.QuestionPoolTreeImpl" />
<jsp:useBean id="questionpool" scope="session" class="org.navigoproject.ui.web.form.questionpool.QuestionPoolForm" />
<html:html>
<head><%= request.getAttribute("html.head") %>
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/treeJavascript.js" %>
//-->
</script>
<title>Question List</title>
<link href="<%=request.getContextPath()%>/css/main.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/jsp/aam/stylesheets/main.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/jsp/aam/stylesheets/nav.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#ffffff" onload="collapseAllRows();flagRows();<%= request.getAttribute("html.body.onload") %>">

<!-- mock up pg 7 -->
<div class="heading">Search for Question(s)</div>

<html:form action="searchQuestion.do" method="post">

<span class="number">1</span>
<span class="h2">Select search criteria:</span>
<table class="tblMain">
  <tr>
    <td valign="top">
	<span class="h2"> By Question Type: </span>
    </td>
    <td>

<table>
  <tr>
    <td>
	<html:multibox property="searchByTypes" value="All"/>All<br>
	<html:multibox property="searchByTypes" value="Multiple Choice"/>Multiple Choice
<br>
	<html:multibox property="searchByTypes" value="Multiple Correct Answer"/>Multiple Choice Multiple Correct
<br>
	<html:multibox property="searchByTypes" value="Fill In the Blank"/>Fill in the Blank
<br>
	<html:multibox property="searchByTypes" value="Matching"/>Matching
<br>
    </td>
    <td>
	<html:multibox property="searchByTypes" value="Audio Recording"/>Audio Response
<br>
	<html:multibox property="searchByTypes" value="File Upload"/>File Upload
<br>
	<html:multibox property="searchByTypes" value="Multiple Choice Survey"/>Multiple Choice Survey
<br>
	<html:multibox property="searchByTypes" value="Essay"/>Short Answer/Essay
<br>
	<html:multibox property="searchByTypes" value="True False"/>True/False
<br>
    </td>

  </tr>
</table>




    </td>
  </tr>
  <tr>
    <td valign="top">
	<div class="h2"> In Pool(s):</div>
    </td>
    <td>
<%@ include file="/jsp/aam/questionpool/poolTree.jsp" %>
    </td>
  </tr>

  <tr>
    <td width="30%"> Question text:<br/>(optional)</td>
    <td width="70%"> <html:text property = "searchQtext"/></td>
  </tr>
  <tr>
    <td> Keywords:<br/>(optional)</td>
    <td> <html:text property = "searchQkeywords"/></td>
  </tr>
  <tr>
    <td> Objectives:<br/>(optional)</td>
    <td> <html:text property = "searchQobj"/></td>
  </tr>
  <tr>
    <td> Rubrics:<br/></td>
    <td> <html:text property = "searchQrubrics"/></td>
  </tr>
</table>

<br>
<br>
<br>
<span class="number">2</span>
<span class="h2">
Click "Search" to find question(s) or "Cancel" to return to previous page:
</span>

<br>
<br>
<center>
  <html:submit>
     <bean:message key="button.search"/>
  </html:submit>

<html:reset onclick="history.go(-1)" value="Cancel"/>
</center>
</html:form>


</body>
</html:html>
