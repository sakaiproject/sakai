<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page errorPage="index_error.jsp" %>
<jsp:useBean id="subpoolTree" scope="session" class="org.navigoproject.business.entity.questionpool.model.QuestionPoolTreeImpl" />
<jsp:useBean id="questionpool" scope="session" class="org.navigoproject.ui.web.form.questionpool.QuestionPoolForm" />
<html:html>
<head><%= request.getAttribute("html.head") %>
<title>Export Pool</title>
<link href="<%=request.getContextPath()%>/css/main.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#ffffff" onload="collapseAllRows();flagRows();<%= request.getAttribute("html.body.onload") %>">
<div class="heading">Export Pool</div>  


<br/>
<logic:iterate id="qpool" collection='<%=session.getAttribute("selectedpools")%>'>
<bean:write name="qpool" property="displayName" />
<br>
</logic:iterate>
</div>

<br/>


<html:form action="exportPool.do"  method="post" enctype="multipart/form-data">

<table>
<tr>
<td>
<span class="number">1</span>
</td>
<td valign="top">
<span class="instructionsSteps">Select destinations:
</span>
</td>
</tr>

<tr>
<td></td>
<td class="instructionsSteps">
<br/>
Export to: 

<html:file property="filename" />

<br/>
<br/>
</td>
</tr>
<tr>
<span class="number">2</span>
</td>
<td valign="top">
<span class="instructionsSteps">
Click "Export" to export pool(s) or "Cancel" to return to the previous page:
</td>
</tr>
</table>
<br/>
<br/>
  <center>
<html:submit>
     <bean:message key="button.export"/>
  </html:submit>

<html:reset onclick="history.go(-1)" value="Cancel"/>
  </center>
</html:form>
</body>
</html:html>
