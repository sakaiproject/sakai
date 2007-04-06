<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.sql.*, java.util.*, 
                 org.adl.util.*, org.adl.samplerte.util.*, 
                 org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_viewCourse.jsp
   **
   ** File Description:   This file displays a list of courses that a user 
   **                     is registered for.  The user can click on the link
   **                      and launch the course. 
   **
   ** Author: ADL Technical Team
   **
   ** Contract Number:
   ** Company Name: CTC
   **
   ** Module/Package Name:
   **  Module/Package Description:
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
   String formBody = "";
   Vector courses = (Vector)request.getAttribute("courses");
	
   
   String color = new String("#FFFFFF");
   for( int i = 0; i < courses.size(); i++ )
   {
      CourseData cd = (CourseData)courses.elementAt(i);
      String courseID = cd.mCourseID;
      String courseTitle = cd.mCourseTitle;
      String importDateTime = cd.mImportDateTime;
      boolean start = cd.mStart;
      boolean TOC = cd.mTOC;
      boolean resume = cd.mSuspend ;
      
      formBody += "<tr bgcolor=" + color + "><td>";
      
      String startCourse = (start) ? "<a href='runtime/sequencingEngine.jsp?courseID="
                  + courseID + "&courseTitle=" + courseTitle +"'>Start Course</a>" : "Start Course";
                  
      startCourse = (resume) ? "<a href='runtime/sequencingEngine.jsp?courseID="
                  + courseID + "&courseTitle=" + courseTitle +"'>Resume Course</a>" : startCourse;           
                  
      String toc = (TOC) ? "<a href='runtime/sequencingEngine.jsp?courseID="
                  + courseID + "&courseTitle=" + courseTitle + "&viewTOC=true" +"'>View Table Of Contents</a>" : "View Table Of Contents";

      // If its auto, launch in a new window. If not, launch in the
      // frameset
      if (false)
      {
          formBody = formBody
                  + "<a href='javascript:launchAutoWindow('"
                  + courseID + "')'>" + courseTitle + "</a>";
      } else
      {
          formBody = formBody
                  + courseTitle + "<br>Imported on: "
                  + importDateTime + "<br>"
                  + startCourse
                  + "&nbsp;&nbsp;|&nbsp;&nbsp;"
                  + toc;
                  
      }
      if (color.equals("#FFFFFF"))
      {
          color = "#CDCDCD";
      } else
      {
          color = "#FFFFFF";
      }
      formBody += "</td></tr>";
   }
%>
<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    View Courses</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <meta http-equiv="expires" content="Tue, 05 DEC 2000 01:00:00 GMT">
   <meta http-equiv="Pragma" content="no-cache">
   
   <link href="includes/sampleRTE_style.css" rel="stylesheet" 
         type="text/css">
 
<script language="javascript">

/****************************************************************************
**
** Function:  launchAutoWindow()
** Input:   courseID - String - The identifier of the course that is being
**                              launched in the new window.
** Output:  none
**
** Description:  Launches course content in a new window.
**
***************************************************************************/      
function launchAutoWindow( courseID )
{
   var theURL = "runtime/sequencingEngine.jsp?courseID=" + courseID;;
   window.document.location.href = "LMSMenu.jsp";
   window.top.contentWindow=window.open( theURL, 'displayWindow' ); 
}


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

<body bgcolor="#FFFFFF" >
   <p>
   <a href="runtime/LMSMenu.jsp">Back To Main Menu</a>
   </p>

<p class="font_header">
<b>
   Available Courses
</b>
</p>
<FORM METHOD="POST">

<table width="400">
   <tr>
      <td>
         <hr>
      </td>
   </tr>
   <tr>
      <td bgcolor="#5E60BD" class="white_text">
         <b>
            &nbsp;Please Select a Course:
         </b>
      </td>
   </tr>

      <tr>
      <td>
         <%= formBody%>
      </td>
   </tr>
   <tr>
      <td>
         <hr>
         <br><br>
         <a href="javascript:newWindow( 'help/launchCourseHelp.htm' );">
         Help!</a>
      </td>
   </tr>
</table>
</FORM>
</body>
</html>