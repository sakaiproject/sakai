<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: selectAssessment.jsp,v 1.1 2004/09/16 01:46:37 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>Assessment Item Delivery</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
  <!-- content... -->
<body>
<table background="/sam-dev/images/header_background.gif" width="100%" height="103" cellspacing="1" cellpadding="0" border="0">
<tr>
<td height="69"><font color="#FFFFFF"></font><font size="6" face="Times New Roman" color="#FFFFFF"></font></td>
</tr>
<tr>
<td height="34" class="bold"> &nbsp;&nbsp;<a href="/sam-dev/Login.do"></a></td><td width="162" height="34"></td>
</tr>
</table>
</TD>
</TR>
<TR>
<TD bgcolor="ffffff" align="left" valign="top"><script type="text/javascript" src="/sam-dev/js/tigra_tables.js"></script>
<h3>
    &nbsp;&nbsp;Assessments
  </h3>
<p align="center">
<!-- THIS WILL BE A DATA TABLE FOR ASSESSMENTS TO TAKE -->
<table bgcolor="ccccff" class="border" width="90%">
<tr>
<td><b class="navigo_border_text_font">Take an assessment</b></td>
</tr>
</table>
<table width="80%" id="test_survey_table">
<tr>
<td>
 The assessments listed below are currently available for you to take.  To begin, click on the assessment title.</td>
</tr>
<tr>
<td></td>
</tr>
</table>
<br>
<table width="70%" border="0" id="select_table">
<tr>
<td><b><a href="/sam-dev/asi/delivery/xmlSelectAction.do?assessmentId=a57d6f5d-97b2-497a-005e-cb4ef38e2911">American History Final Exam</a></b>
<br>
  &nbsp; &nbsp; &nbsp; &nbsp; Due: Jun 16, 2005, 17:51</td>
</tr>
</table>

<!-- THIS WILL BE A DATA TABLE FOR ASSESSMENTS TO REVIEW -->
<table bgcolor="ccccff" class="border" width="90%">
<tr>
<td><b class="navigo_border_text_font">Review an assessment</b></td>
</tr>
</table>
<table width="90%">
<tr>
<td>You have completed the assessments listed below and they are currently available for you to review.  Click on the assessment title to review your responses (and instructor feedback if  available).</td>
</tr>
</table>
<table border="1" width="80%" id="review_table">
<tr>
<th align="center">
  Assessment Title</th><th align="center">
  Statistics</th><th align="center">

  Time </th><th align="center">
  Submitted</th>
</tr>
<tr>
<td><b><a href="/sam-dev/asi/review/xmlSelectAction.do?assessmentId=5627fec6-d7a2-4e64-0076-4e8dc017294c">lydia2</a></b></td><td>
  n/a</td><td>0days 0hrs 0mins 18secs </td><td>06/16/2004 17:25</td>
</tr>
</table>
<!-- end content -->

      </body>
    </html>
  </f:view>
