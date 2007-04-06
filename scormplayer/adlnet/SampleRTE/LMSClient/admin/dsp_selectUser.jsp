<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.Vector, 
                  org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_selectUser.jsp
   **
   ** File Description:     
   ** 
   ** This file defines the selectUser.jsp that shows an administrator which 
   ** users are available to administer global objectives on.
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
<SCRIPT LANGUAGE="javascript">
   <!--
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
   Vector users = (Vector)request.getAttribute("users");

   // Loops through the set of all users and the body of the table is  
   // formed by assigning it to the string 'formBody'.
   String formBody = "";
   String color = "#FFFFFF";
   String userID = "";
   String lastName = "";
   String firstName = "";
   String checked = "checked";
   UserProfile up = new UserProfile();

   for( int i = 0; i < users.size(); i++ )
   {
      up = (UserProfile)users.elementAt(i);
      userID = up.mUserID;
      lastName = up.mLastName;
      firstName = up.mFirstName;
   
      formBody += 
         "<tr bgcolor='" + color + "'><td width='10%'>" +
         "<input type='radio' name='user' id='" + userID + "' value='" + 
         userID +"'" + checked + "/>&nbsp;&nbsp;&nbsp;</td><td><p>" +
         "<label for='" + userID + 
         "'>" + firstName + " " + lastName + "</label></p></td></tr>";
   
      if(color.equals("#FFFFFF"))
      {
         color = "#CDCDCD";
      }
      else
      {
         color = "#FFFFFF";
      }
      checked = "";
   }
%>

<html>
<head>
    <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
        Global Objectives User Selection
    </title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta http-equiv="expires" CONTENT="Tue, 05 DEC 2000 01:00:00 GMT">
    <meta http-equiv="Pragma" CONTENT="no-cache">
    <link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
</head>

<body bgcolor="#FFFFFF">

    <p><a href="/adl/runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>


    <p class="font_header">
    <b>
    Global Objectives Administration User Selection
    </b>
    </p>
    <form name="UserSelectForm" method="POST" 
        action="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.COURSE_OBJ %>" accept-charset="utf-8">
        <table width="450" border="0">
            <tr> 
                <td colspan="2"> 
                    <hr>
                </td>
            </tr>
            <tr>
                <td colspan="2" bgcolor="#5E60BD" CLASS="white_text">
                    <b>
                        &nbsp;Available Users:
                    </b>
                    </font>
                </td>
            </tr>
                <%= formBody%>
        </table>
        <table width="450" border="0">
            <tr>
                <td COLSPAN="2">
                    <hr>
                </td>
            </tr>
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
                    Help!</A>
                </td>
            </tr>
        </table>
    </form>
</body>
</html>
