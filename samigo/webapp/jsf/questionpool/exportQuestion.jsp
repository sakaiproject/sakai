<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page errorPage="index_error.jsp" %>
<jsp:useBean id="subpoolTree" scope="session" class="org.navigoproject.business.entity.questionpool.model.QuestionPoolTreeImpl" />
<jsp:useBean id="questionpool" scope="session" class="org.navigoproject.ui.web.form.questionpool.QuestionPoolForm" />
<html:html>
<head><%= request.getAttribute("html.head") %>
<title>Export Question</title>
<link href="<%=request.getContextPath()%>/jsp/aam/stylesheets/main.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/css/main.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/jsp/aam/stylesheets/nav.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#ffffff" onload="collapseAllRows();flagRows();<%= request.getAttribute("html.body.onload") %>">
<div class="heading">Export Question(s)</div>  


<h2>Question Names  
</h2>
<br>

<logic:iterate id="qpool" collection='<%=session.getAttribute("selectedItems")%>'>
<bean:write name="qpool" property="itemText" />
</logic:iterate>
</logic:iterate>
<br>
</logic:iterate>

<br>


<html:form action="exportQuestion.do"  method="post" enctype="multipart/form-data">

<table>
<tr>
<td>
<span class="number">1</span>
</td>
<td valign="top">
<span class="instructionsSteps">
Select destinations:
</td>
</tr>

<tr>
<td></td>
<td class="instructionsSteps">
Export to: 

<html:file property="filename" />

</td>
</tr>
<tr>
<td>
<span class="number">2</span>
</td>
<td valign="top">
<span class="instructionsSteps">
Click "Export" to export pool(s) or "Cancel" to return to the previous page:
</td>
</tr>
</table>
  <center>
<html:submit>
     <bean:message key="button.export"/>
  </html:submit>

<html:reset onclick="history.go(-1)" value="Cancel"/>

  </center>
</html:form>
</body>
</html:html>
