<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.sql.*, java.util.*, org.adl.samplerte.util.*,
    org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_regCourse.jsp
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

<%
   String theWebPath = getServletConfig().getServletContext().
                       getRealPath( "/" );
   String formBody = "";
   String userCourses = (String)request.getAttribute("userCourses");
   Vector courses = (Vector)request.getAttribute("courses");
   String userID = (String)session.getAttribute( "USERID" );

   String color = "#FFFFFF";
   for( int i = 0; i < courses.size(); i++ )
   {
      CourseData cd = (CourseData)courses.elementAt(i);
      String courseID = cd.mCourseID;
      String courseTitle = cd.mCourseTitle;
            
      String importDateTime = cd.mImportDateTime;
      String checked = "";

      if(userCourses.indexOf("|"+courseID+"|") != -1)
      {
         checked = "checked";
      }

      formBody += "<tr bgcolor='" + color + "'><td width='10%'>" +
            "<input type='checkbox' name='" + courseID + "' id='" + courseID + 
            "' value='1'" + checked +"/></td><td><label for='" + 
            courseID+ "'>" + courseTitle + "<br>Imported on: " + 
            importDateTime + "</label></td></tr>";

      if(color.equals("#FFFFFF"))
      {
         color = "#CDCDCD";
      }
      else
      {
         color = "#FFFFFF";
      } 

   }

   formBody += "<tr><td colspan=3><hr></td></tr>";

%>

<html>
<head>
<title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
Course Register</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<META http-equiv="expires" CONTENT="Tue, 05 DEC 2000 01:00:00 GMT">
<META http-equiv="Pragma" CONTENT="no-cache">
<link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

<script LANGUAGE="javascript">
/****************************************************************************
**
** Function:   newWindow()
** Input:   pageName - String - The page that will be launched in the new
**                              window.  At this time, only the help page.
** Output:  none
**
** Description:  Launches a new window with additional user help
**
***************************************************************************/  
function newWindow(pageName)
{
   window.open(pageName, 'Help', 
   "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no," +
   "resizable=yes,width=500,height=500");
}

</script>
</head>
<body bgcolor="#FFFFFF">

   <p>
   <a href="runtime/LMSMenu.jsp">Back To Main Menu</a>
   </p>

<p class="font_header">
<b>
Course Registration
</b>
</p>
<form name="courseRegForm" method="POST" action="/adl/LMSCourseAdmin">
   <input type="hidden" name="type" 
      value="<%= ServletRequestTypes.PROC_REG_COURSE %>">
   <input type="hidden" name="path" value="<%=theWebPath%>">
   <input type="hidden" name="userID" value="<%=userID%>">
   <table width="400" border="0">
      <tr> 
         <td colspan="3" height="71">
               Please select all of the courses you wish to register in by 
               checking the checkbox or select the courses you would like to be
               removed from by unchecking the checkbox.  Note that 
               unregistering for a course will remove all associated saved data 
               for the course.
         </td>
      </tr>
      <tr>
         <td COLSPAN="6">
            <hr>    
         </td>
      </tr>
      <tr>
         <td colspan="6" bgcolor="#5E60BD" CLASS="white_text">
            <b>
               &nbsp;Available Courses:
            </b>
         </td>
      </tr>
      <%= formBody%>
      <tr>
         <td>
            &nbsp;
         </td>
      </tr>
      <tr> 
         <td colspan="2" align="center"> 
            <input type="submit" name="submit" value="Submit">
         </td>
      </tr>
      <tr>
         <td>
            &nbsp;
         </td>
      </tr>
      <tr>
         <td>
            <a href="javascript:newWindow('help/courseRegisterHelp.htm');">
               Help!
            </a>
         </td>
      </tr>
   </table>
</form>
</body>
</html>
