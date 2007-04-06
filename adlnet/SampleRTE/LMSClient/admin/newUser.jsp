<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.*, java.io.*, org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  newUser.jsp
   **
   ** File Description:   This file allows an admin to enter information to
   **                     create a new user account.
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
   String is_admin = (String)session.getAttribute( "AdminCheck" );

   // Condition statement to determine if the Session Variable has been
   // properly set.
   if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
   {
%>
<% 
   String bodyText = "";
   String newUserMsg = (String)request.getAttribute("reqOp");
   String userID = "";
   String firstName = "";
   String lastName = "";
   UserProfile userProfile = new UserProfile();
   
   if ( newUserMsg != null )
   {
      if ( newUserMsg.equals("duplicate_user"))
      {

          bodyText = "User ID already exists, please choose another ID";
          userProfile = (UserProfile)request.getAttribute("userProfile");
          userID = userProfile.mUserID;
          firstName = userProfile.mFirstName;
          lastName = userProfile.mLastName;


      }
%>

<%
   }
%>
<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Add New User</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   
   <link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

   <script language="JavaScript">
   /****************************************************************************
   **
   ** Function:  checkData()
   ** Input:   none
   ** Output:  boolean
   **
   ** Description:  This function ensures that there are values in each text
   **               box before submitting
   **
   ***************************************************************************/
   function checkData()
   {
      if ( newUser.userID.value == "" || newUser.firstName.value == "" ||
           newUser.lastName.value == "" || newUser.password.value == "" ||
           newUser.cPassword.value == "" )
      {
         alert ( "You must provide a value for all fields!!" );
         return false;
      }

      if ( newUser.password.value != newUser.cPassword.value)
      {
         alert ( "Password and confirmed password are not the same!!" );
         return false;
      }

   }

   /****************************************************************************
   **
   ** Function:  newWindow()
   ** Input:   pageName
   ** Output:  none
   **
   ** Description:  This function opens the help window
   **
   ***************************************************************************/
   function newWindow(pageName)
   {
      window.open(pageName, 'Help',
      "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=yes,width=500,height=500");
   }

   </script>
</head>

<body bgcolor="#FFFFFF">

<p><a href="runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>


<p class="font_header">
<b>
   Add a New User
</b>
</p>
   <b>
     <%= bodyText %>
   </b>   
<form method="post" action="/adl/LMSUserAdmin" name="newUser" 
                                                 onSubmit="return checkData()"
                                                 accept-charset="utf-8">

   <input type="hidden" name="type" value="8" />
   <table width="450" border="0" align="left">
      <tr>
         <td colspan="2">
            <hr>
         </td>
      </tr>
      <tr>
         <td bgcolor="#5E60BD" colspan="2" class="white_text">
            <b>
               &nbsp;Please provide the following new user information:
         </b>
         </td>
      </tr>
      <tr>
         <td width="37%">
            <label for="userID">User ID:</label>
         </td>
         <td width="63%">

<%
   if ( userID != null )
   {
%>
               <input type="text" name="userID" id="userID" value="<%= userID %>">
<%
   }
   else
   {
%>
               <input type="text" name="userID" id="userID"> 
<%
  }
%>

         </td>
      </tr>
      <tr>
         <td width="37%"><label for="firstName">First Name:</label></td>
            <td width="63%">

<%
  if ( firstName != null )
  {
%>

              <input type="text" name="firstName" id="firstName" value="<%= firstName %>">

<%
  }
  else
  {
%>

              <input type="text" name="firstName">

<%
  }
%>

           </td>
        </tr>
        <tr>
          <td width="37%"><label for="lastName">Last Name:</label></td>
             <td width="63%">

<%
  if ( lastName != null )
  {
%>

               <input type="text" name="lastName" id="lastName" value="<%= lastName %>">

<%
  }
  else
  {
%>

               <input type="text" name="lastName" id="lastName">

<%
   }
%>

             </td>
         </tr>
         <tr>
             <td width="37%">
                <label for="password">Password:</label>
             </td>
             <td width="63%">
                 <input type="password" name="password" id="password">
             </td>
         </tr>
         <tr>
             <td width="37%">
                <label for="cPassword">Password Confirmation:</label>
             </td>
             <td width="63%">
                 <input type="password" name="cPassword" id="cPassword">
             </td>
         </tr>
         <tr>
             <td width="37%">
                <label for="admin">Admin:</label>
             </td>
             <td width="63%">
                 <select name="admin" id="admin">
                     <option value="false">No</option> <option value="true">Yes</option>
                 </select>
             </td>
         </tr>
         <tr>
            <td colspan="2">
               <hr>
            </td>
         </tr>
         <tr>
             <td width="37%">
                &nbsp;
             </td>
             <td width="63%">
                &nbsp;
             </td>
         </tr>
         <tr>
            <td colspan="2" align="center">
               <input type="submit" name="Submit" value="Submit">
            </td>
         </tr>
         <tr>
            <td colspan="2">
               <br><br>
               <a href="javascript:newWindow('help/newUserHelp.htm');">Help!</a>
            </td>
         </tr>
     </table>
</form>
<p>
&nbsp;
</p>
</body>
</html>

<%
   }
   else
   {
      // Redirect the user to the LMSMenu page.
      response.sendRedirect( "runtime/LMSMenu.htm" ); 
   }
%>