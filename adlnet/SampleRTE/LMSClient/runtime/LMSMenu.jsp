<%@ page contentType="text/html;charset=utf-8" %>

<%
   /***************************************************************************
   **
   ** Filename:  LMSMenu.jsp
   **
   ** File Description: This file displays the main menu.
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
<%@page import = "java.sql.*, 
						java.util.*, 
						java.io.*, 
						org.adl.samplerte.util.*,
						org.adl.samplerte.server.*" %>
<%
   String theWebPath = 
          getServletConfig().getServletContext().getRealPath( "/" );
%>
<html>
<head>
<meta http-equiv="expires" content="Tue, 20 Aug 1999 01:00:00 GMT">
<meta http-equiv="Pragma" content="no-cache">
<title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Menu Page</title>

<link href="../includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

<script language=javascript>
/**************************************************************************
*  function to confirm that user really wants to clear the database
*
***************************************************************************/
function confirmClearDatabase()
{
    if( confirm("Do you really want to remove all the course information?") )
    {
       var path = document.getElementById("path").value;
       window.parent.frames['Content'].document.location.href = 
          "/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.CLEAR_DB %>&path=" + path;
    }
}

/**************************************************************************/


// Hide or display the relevant controls
if( document.all != null )
{
   window.parent.frames['LMSFrame'].document.forms['buttonform'].suspend.style.visibility="hidden";
   window.parent.frames['code'].document.location.href = "code.jsp";
   window.top.frames['LMSFrame'].document.forms['buttonform'].next.style.visibility = "hidden";
   window.top.frames['LMSFrame'].document.forms['buttonform'].previous.style.visibility = "hidden";
   window.top.frames['LMSFrame'].document.forms['buttonform'].quit.style.visibility = "hidden";
}
</script>
</head>

<body >
<input type=hidden id=path value=<%=theWebPath%> />
<%
   // Get the user's information and remove any course IDs
   String userid = (String)session.getAttribute( "USERID" );
   String admin = (String)session.getAttribute( "RTEADMIN" );
   String username = (String)session.getAttribute( "LOGINNAME" );
   session.removeAttribute( "COURSEID" );
   session.removeAttribute( "TOC" );
%>

    <p class="purple_text">
    <b>
    Welcome, <%=(String)session.
             getAttribute( "LOGINNAME" )%>!    </b>
    </p>

    <p class="font_header">
    <b>
    Please select one of the following options:
    </b>
    </p>

    <br>

    <table width="250">
    <tr>
        <td bgcolor="#5E60BD" class="white_text">
           <b>
              &nbsp;User Options:
           </b>
        </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.REG_COURSE %>&userId=<%=userid %>">
          	Register For a Course
          </a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.VIEW_REG_COURSE %>&userId=<%= userid %>">
          	View Registered Courses
          </a>
       </td>
    </tr>
        <tr>
       <td>
          <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.SELECT_MY_COURSE %>&userId=<%= userid %>">
         	 View Course Status
         </a>
       </td>
    </tr>

    <tr>
       <td>
          <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.GET_PREF %>&userId=<%= userid %>">
          	Change My Profile
          </a>
       </td>
    </tr>

	<tr>
       <td>
          <a href="../RTE_Readme/main.html" target="_blank">
		   View Readme
		  </a>
       </td>
    </tr>

        <tr>
       <td>
          <a href="logout.jsp">Logout</a>
       </td>
    </tr>

    </table>

<%
   if ( (! (admin == null)) && ( admin.equals("true")) )
   {
       // Sets a new Session Variable to Secure Admin pages
    session.putValue("AdminCheck", new String("true"));
%>
    <br>

    <table width="250">
    <tr>
        <td bgcolor="#5E60BD" class="white_text">
           <b>
              &nbsp;Administrator Options:
           </b>
        </td>
    </tr>
    <tr>
       <td>
          <a href="../import/importCourse.jsp">Import Course</a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.GET_COURSES %>&setProcess=manage">Manage Courses</a>
       </td>
    </tr>
    <tr>
       <td>
         <a href="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.GET_COURSES %>&setProcess=delete">Delete Course</a> 
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.GET_USERS %>&setProcess=allCourse">View All User's Course Status
          </a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.NEW_USER %>&setProcess=pref">Add Users</a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.GET_USERS %>&setProcess=pref">Manage Users</a>
       </td>
    </tr>
    <tr>
       <td>
         <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.GET_USERS %>&setProcess=delete">Delete Users</a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="../admin/selectAction.jsp">Global Objectives Administration
          </a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.GET_USERS %>&setProcess=buckets">Manage SSP Buckets</a>
       </td>
    </tr>
    <tr>
       <td>
          <a href="javascript:confirmClearDatabase()">Clear Database</a>
       </td>
    </tr>
    <table>

<%
   }
%>

</body>
</html>
