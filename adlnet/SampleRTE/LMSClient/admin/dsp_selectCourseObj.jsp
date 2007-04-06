<%@ page contentType="text/html;charset=utf-8" %>


<%@page import = "java.sql.*, org.adl.samplerte.util.*,
                  java.util.Vector,
                  org.adl.samplerte.server.*"%>
<%
   /***************************************************************************
   **
   ** Filename:  selectCourseObjectives.jsp
   **
   ** File Description:     
   ** 
   ** This file defines the courseRegister.jsp that shows a user which courses 
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

   String formBody = "";
   Vector courses = (Vector)request.getAttribute("courses");
   String user = (String)request.getAttribute("user");
   String checked = "";

      // Loops through the result set of all courses and if the current 
      // course is in the string 'userCourses', the string 'checked' is 
      // assigned the value "checked".  The body of the table is then 
      // formed by assigning it to the string 'formBody'.  The 'checked' 
      // string is output in the checkbox tag. If the course was in the 
      // 'userCourses' string, the checkbox will be checked.
      for( int i = 0; i < courses.size(); i++ )
      {
         CourseData cd = (CourseData)courses.elementAt(i);
         String courseID = cd.mCourseID;
         String courseTitle = cd.mCourseTitle;
      
         
         formBody += "<tr><td size='5%'>" +
                     "<input type='radio' name='course' id='" + courseID + 
                     "' value='" + courseID +"'/></td><td><p><label for='" + 
                     courseID + "'>" + courseTitle + 
                     "</label></p></td></tr>";
         
      }

      formBody += "<tr><td size='5%'>" +
                  "<input type='radio' name='course' id='notAssociated' " +
                  "value=''" + " checked /></td><td><p>" +
                  "<label for='notAssociated'>All Objectives not " +
                  "associated with a Course</label></p></td></tr>";
      formBody += "<tr><td colspan=2><hr></td></tr>";

%>

<html>
<head>
   <script language=javascript>

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
    <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
        Global Objectives Course Selection</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta http-equiv="expires" CONTENT="Tue, 05 DEC 2000 01:00:00 GMT">
    <meta http-equiv="Pragma" CONTENT="no-cache">
        <link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#FFFFFF">

    <p><a href="/adl/runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>

    <p class="font_header">
    <b>
        Global Objectives Administration Course Selection
    </b>
    </p>
    <form name="courseRegForm" 
    		 method="POST" 
    		 action="/adl/LMSCourseAdmin">
      <input type="HIDDEN" name="type" value="<%= ServletRequestTypes.OBJ_ADMIN %>">
      <input type="HIDDEN" name="user" value="<%=user%>">
        <table width="450" border="0">
            <tr> 
                <td colspan="3" height="71">
                    <p>
                        Please select a course whose 
                        Global Objectives information you
                        would like to view or modify.
                    </p>
                </td>
            </tr>
            <tr> 
                <td colspan="3"> 
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
        </table>
        <table width="450" border="0">
            <tr> 
                <td width="45%">&nbsp;</td>
                <td width="55%"> 
                    <input type="submit" name="submit" value="Submit">
                </td>
            </tr>
            <tr>
                <td>
                    <A HREF=
                    "javascript:newWindow('/adl/help/globalObjectivesHelp.htm');">
                    Help!
                    </A>
                </td>
            </tr>
        </table>
    </form>
</body>
</html>
