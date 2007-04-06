<%@page import = "java.util.*, java.io.*, org.adl.samplerte.server.*" %>
<!--
/*******************************************************************************
**
** Filename: selectAction.jsp
**
** File Description: This page provides links for user to select desired action.
**
** Author: ADL Technical Team
**
** Contract Number:
** Company Name: CTC
**
** Module/Package Name:
** Module/Package Description:
**
** Design Issues:
**
** Implementation Issues:
** Known Problems:
** Side Effects:
**
** References: ADL SCORM
**
/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
*******************************************************************************/
-->

<html>
<head>
<title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Global Objectives Action Selection</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<META http-equiv="expires" CONTENT="Tue, 05 DEC 2000 01:00:00 GMT">
<META http-equiv="Pragma" CONTENT="no-cache">
<link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
<script language="JavaScript">
/****************************************************************************
   **
   ** Function:  newWindow()
   ** Input:   pageName = Name of the window
   ** Output:  none
   **
   ** Description:  This method opens a window named <pageName>
   **
   ***************************************************************************/
   function newWindow(pageName)
   {
      window.open(pageName, 'Help',"toolbar=no,location=no,directories=no," +
                  "status=no,menubar=no,scrollbars=no,resizable=yes," +
                  "width=500,height=500");
   }
</script>
</head>


<body bgcolor="#FFFFFF">

<p><a href="/adl/runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>


<p class="font_header">
<b>
Global Objectives Administration Action Selection
</b>
</p>
<table width="450" border="0">
   <tr>
      <td colspan="2">
         <hr>
      </td>
   </tr>
   <tr>
      <td colspan="2" bgcolor="#5E60BD" CLASS="white_text">
         <b>
            &nbsp;Global Objectives Administration Actions:
         </b></font>
      </td>
   </tr>
   <tr>
      <td>
         <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.NEW_OBJ %>">
         			Create a new global objective
         </a>
      </td>
   </tr>
   <tr>
      <td>
         <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.USER_OBJ %>">
         			View or modify an existing global objective
         </a>
      </td>
   </tr>
</table>
<table width="450" border="0">
   <tr>
      <td COLSPAN="2">
         <hr>
      </td>
   </tr>
      <tr>
         <td>
            <a href="javascript:newWindow('/adl/help/globalObjectivesHelp.htm');">
            Help!</a>
         </td>
      </tr>
   </table>
</body>
</html>

