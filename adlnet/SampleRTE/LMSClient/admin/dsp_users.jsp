<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.*, java.io.*, org.adl.samplerte.server.*" %>

<%
   /***************************************************************************
   **
   ** Filename:  dsp_users.jsp
   **
   ** File Description:  This file provides an interface for an admin to select
   **                     a user.
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
   String setProcess = (String)request.getAttribute("setProcess");
   Vector userList = new Vector();
   userList = (Vector)request.getAttribute("userProfiles");
   int size = userList.size();
   int valueInt = 0;
   String action = "";
   if( setProcess.equals("pref") )
   {
      valueInt = ServletRequestTypes.GET_PREF;
      action = "/adl/LMSUserAdmin";
   }
   else if ( setProcess.equals("delete") )
   {
      valueInt = ServletRequestTypes.DELETE_USERS;
      action = "/adl/LMSUserAdmin";
   }
   else if ( setProcess.equals("buckets") )
   {
      valueInt = ServletRequestTypes.LIST_BUCKETS;
      action = "/adl/LMSUserAdmin";
   }
   else
   {
      valueInt = ServletRequestTypes.SELECT_MY_COURSE;
      action = "/adl/LMSCourseAdmin";
   }
   String bodyText = "<td><label for='userId'>Users: </label>" + 
                     "<select name='userId' id='userId'>";                
   UserProfile user = null;
	for( int i = 0; i < size; i++ )
   {
   	user = (UserProfile)userList.elementAt(i); 
		bodyText += "<option>" + user.mUserID + "</option>";
	}
	bodyText += "</select></td>";
%>


<html>
<head>
    <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - User 
    Aministration - Select User</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <link href='includes/sampleRTE_style.css' rel='stylesheet' type='text/css'>
</head>
    <body bgcolor='FFFFFF'>

    <p>
    <a href="runtime/LMSMenu.jsp">Back To Main Menu</a>
    </p>

    <p class="font_header">
    <b>User Administration - Select User</b>
    </p>
    <form method="post" action="<%=action%>" name="userSelection">
        <input type="hidden" name="type" value="<%=valueInt%>">
        <table width="450">
            <tr>
                <td>
                    <hr>
                </td>
            </tr>
            <tr>
                <td bgcolor="#5E60BD" class="white_text"><b>&nbsp;Please select a user:</b></td>
            </tr>
            <tr>
				<%= bodyText %>
            </tr>
            <tr>
                <td>
                    <hr>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td align="center">
                    <input type="submit" name="Submit" value="Submit">
                </td>
            </tr>
        </table>
    </form>

</html>

