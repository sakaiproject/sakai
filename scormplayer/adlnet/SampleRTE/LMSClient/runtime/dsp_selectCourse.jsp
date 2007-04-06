<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.sql.*, java.util.Vector, 
                  org.adl.samplerte.util.*,
                  org.adl.samplerte.server.CourseData,
                  org.adl.samplerte.server.ServletRequestTypes" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_seletCourse.jsp
   **
   ** File Description:     
   ** 
   ** This file shows a user which courses 
   ** they may register for and allows them to select ones to register for.
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

<SCRIPT language="javascript">
<!--
/****************************************************************************
**
** Function:  checkValues()
** Input:   none
** Output:  boolean
**
** Description:  This function ensures that a course is selected
**               before submitting.
**
***************************************************************************/
function checkValues()
{  
    var course_selected = "false";

    for (i=0; i<document.courseSelectForm.courseID.length; i++)
    {
    	if (document.courseSelectForm.courseID[i].checked)
    	{
    		course_selected = "true";
    	}
    }

   if ( course_selected == "false" )
   {
       alert('Please Select a Course.');
       return false;
   }

   return true;
}


/*************************************************************************
* Method: newWindow()
* Input: pageName
* Output: none
*
* Description: Opens the page input by 'pageName' in a new browser window.
*************************************************************************/
function newWindow(pageName)
{
   window.open(pageName, 'Help', "toolbar=no,location=no,directories=no," +
               "status=no,menubar=no,scrollbars=no,resizable=yes," + 
               "width=500,height=500");
}
// -->
</SCRIPT>


<%
    String formBody = "";
    String userID = (String)request.getAttribute( "userId" );
    Vector courses = (Vector)request.getAttribute("courses");
    String checked = "checked";
    String courseID = "";
    int i;
    for( i = 0; i < courses.size(); i++ )
    {
       CourseData cd = (CourseData)courses.elementAt(i);
       courseID = cd.mCourseID;
       String courseTitle = cd.mCourseTitle;
       String importDateTime = cd.mImportDateTime;
      

       formBody += "<tr><td size='5%'>" +
                   "<input type='radio' name='courseID' id='" + courseID + 
                   "' value='" + courseID +"'" + checked + 
                   "/></td><td><label for='" + courseID + "'><p>" +   
                   courseTitle + "<br>Imported on: " + importDateTime +
                   "</p></td></tr>";
       checked = "";

    }
        formBody += "<tr><td colspan=\"6\"><hr></td></tr>";
%>

<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 
   - Course Selection</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <META http-equiv="expires" content="Tue, 05 DEC 2000 01:00:00 GMT">
   <META http-equiv="Pragma" content="no-cache">
   <link href="includes/sampleRTE_style.css" rel="STYLESHEET" type="text/css">
</head>
   <body bgcolor="#FFFFFF">

   <p>
   <a href="runtime/LMSMenu.jsp">Go Back To Main Menu</a>
   </p>

   <p class="font_header">
   <b>Course Status Course Selection</b>
   </p>
   <form name="courseSelectForm" method="POST" action="/adl/LMSCourseAdmin">
      <input type="HIDDEN" name="type" 
       value="<%= ServletRequestTypes.VIEW_MY_STATUS %>">
      <input type="HIDDEN" name="userID" value="<%=userID%>">
      <table width="548" border="0">
         <tr>
            <td colspan="6">
               <hr>
            </td>
         </tr>
         <tr>
            <td colspan="6" bgcolor="#5E60BD" class="white_text"><b>
             &nbsp;Please select a 
            course whose status information you would like to view:</b></td>
         </tr>
      <%= formBody%>
   
      </table>
      <table width="547" border="0">
         <tr>
            <td width="45%">&nbsp;</td>
            <td width="55%">
               <input type="SUBMIT" name="submit" value="Submit">
            </td>
         </tr>
      </table>
   </form>
   </body>
</html>
