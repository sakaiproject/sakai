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
<title>Move Question</title>
</head>
<body bgcolor="#ffffff" onload="collapseAllRows();flagRows();<%= request.getAttribute("html.body.onload") %>">
<div class="heading">Move Question</div>

<html:form action="moveQuestion.do" method="post">

<br>
<h2>Question Text</h2>
<br>
<logic:iterate id="qpool" collection='<%=session.getAttribute("selectedItems")%>'>
<bean:write name="qpool" property="itemText" />
<br>
</logic:iterate>

<br>
<table width="100%" border="0">

<tr>
<td>
<span class="number">1</span>
</td>
<td valign="top">
<span class="instructionsSteps">
Move the above pool(s) to:
</span>
</td>
</tr>

<tr>
<td></td>
<td>
<%@ include file="/jsp/aam/questionpool/movePoolTree.jsp" %>
</td></tr>

<tr>
<td>
<span class="number">2</span>
</td>
<td valign="top">
<span class="instructionsSteps">
Click "Move" to continue or "Cancel" to return to the previous page:</span>
</td></tr>
</table>

<br>
<br>
  <center>
	<html:submit>
                <bean:message key="button.move"/>
        </html:submit>

<html:reset onclick="history.go(-1)" value="Cancel"/>

  </center>
</html:form>
</body>
</html:html>
