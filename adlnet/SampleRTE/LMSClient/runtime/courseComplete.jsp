
<%
   /***************************************************************************
   **
   ** Filename:  courseComplete.jsp
   **
   ** File Description:     
   **
   ** This file displays a Course Complete message when a user completes
   ** a course or when a user selects a course which they have 
   ** already completed.
   **
   **
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
   /***************************************************************************
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
   ***************************************************************************/
%>

<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Course Complete</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <link href="../includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
   <script language="JavaScript">
   
   
   /****************************************************************************
   **
   ** Function:  handleAuto()
   ** Input:   none
   ** Output:  none
   **
   ** Description: Handles navigation back to the main menu page. 
   **
   ** Issues:  
   **
   ***************************************************************************/  
   function handleAuto()
   {
      window.opener.top.frames[2].location.href = "LMSMenu.jsp";
      window.close();
   }
   </script>
</head>

<body bgcolor="#FFFFFF">

<%
   String userid = (String)session.getAttribute( "USERID" );
   String admin = (String)session.getAttribute( "RTEADMIN" );
   String contrl = (String)session.getAttribute( "CONTROL" );
 %>
 
<script language="JavaScript">
var scoWinType = typeof(window.opener)

if ( scoWinType != "undefined" && scoWinType != "unknown" ) 
{  
   ctrl = window.opener.top.frames['LMSFrame'].document.forms['buttonform'].control.value;
}
else
{
   ctrl = window.top.frames['LMSFrame'].document.forms['buttonform'].control.value;
}

if (ctrl == "auto")
{
   document.writeln("<p><a href='javascript:handleAuto();'>Go Back To Main Menu</a></p>");
}
else
{
   document.writeln("<p><a href='LMSMenu.jsp'>Go Back To Main Menu</a></p>");
}
window.top.frames['LMSFrame'].document.forms['buttonform'].next.style.visibility = "hidden";
window.top.frames['LMSFrame'].document.forms['buttonform'].previous.style.visibility = "hidden";
window.top.frames['LMSFrame'].document.forms['buttonform'].suspend.disabled = false;
window.top.frames['LMSFrame'].document.forms['buttonform'].suspend.style.visibility = "hidden";
window.parent.frames['code'].document.location.href = "code.jsp";
window.top.frames['LMSFrame'].document.forms['buttonform'].quit.disabled = false;
window.top.frames['LMSFrame'].document.forms['buttonform'].quit.style.visibility = "hidden";

</script>



    
<DIV id=step_1>
   <font face="tahoma" size="3"><b>You Have Completed This Course</b><font>
</DIV>
<p>
&nbsp;
</p>
<br />
<br />
<table width="400" border="1" cellpadding="25">
<tr>
<td>
   NOTES:<br />
   This page indicates that the ADL Sequencer has
   determined that you have fulfilled all the requirements 
   necessary to complete this course.
</td>
</tr>
</table>

</body>
</html>
