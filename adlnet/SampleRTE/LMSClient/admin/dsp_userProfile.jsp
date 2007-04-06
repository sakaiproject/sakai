<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.*, java.io.*, org.adl.samplerte.server.*"%>

<%
   /***************************************************************************
   **
   ** Filename:  dsp_userProfile.jsp
   **
   ** File Description:  This file provides an interface for the admin to select
   **					 a user profile to update/modify/view.
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

   UserProfile user = new UserProfile();
   
   request.setCharacterEncoding("utf-8");
   
   user = (UserProfile)request.getAttribute("userProfile");
   String userID = user.mUserID;
   String firstName = user.mFirstName;
   String lastName = user.mLastName;
   String password = user.mPassword;
   String audioLevel = user.mAudioLevel;
   String language = "";
   boolean isAdmin = user.mAdmin;
   String isUserAdmin = (String)session.getAttribute( "RTEADMIN" );


   if (  !(user.mLanguage == null) && !(user.mLanguage.length() == 0) )
   {
      language = user.mLanguage;
   }

   String deliverySpeed = user.mDeliverySpeed;
   String audioCaptioning = user.mAudioCaptioning;

   String errorHeader = (String)request.getAttribute("errorHeader");
   String errorString = (String)request.getAttribute("errorMsg");
%>
<html>
<head>
   <title>
      SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - Learner
       Preferences Aministration - Edit Learner Profile
   </title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <link href='includes/sampleRTE_style.css' rel='stylesheet'
   type='text/css'>

   <script language="JavaScript">
     function openHelp()
     {
        window.open("help/changeProfileHelp.htm","CommentsLMS",
                                "HEIGHT=550,WIDTH=600 status=no location=no");
     }
   </script>


</head>
<body bgcolor='FFFFFF'>

<p><a href="javascript:history.go(-1)">Back</a></p>

<p class="font_header">
<b>
Learner Preferences Administration - Edit Learner Profile
</b>
</p>
<%
 if ( (errorString != null) && (errorHeader != null) )
   {
%>
<p class="font_two">
<b>
<%=errorHeader%>
</b>
<%=errorString%>
<br>
</p>

<%
   }
%>
<form method="post" action="/adl/LMSUserAdmin" name="userProfile" accept-charset="utf-8">
   <input type="hidden" name="type" value="<%= ServletRequestTypes.UPDATE_PREF %>" />
   <input type="hidden" name="userID" value="<%=userID%>" />
   <input type="hidden" name="firstName" value="<%=firstName%>" />
   <input type="hidden" name="lastName" value="<%=lastName%>" />
   <table width="450">
      <tr>
         <td colspan="2">
            <hr>
         </td>
      </tr>
      <tr>
         <td bgcolor="#5E60BD" colspan="2" class="white_text">
            <b>
               &nbsp;Please edit any user information you would like to change:
         </b>
         </td>
      </tr>
      <tr>
         <td>
            Username:
         </td>
         <td>
            <%=firstName%>&nbsp;<%=lastName%>
         </td>
      </tr>
      <tr>
         <td>
            <label for="password">Password:</label>  
         </td>
         <td>
            <input type="text" name="password" id="password" 
               value="<%=password%>" />
         </td>
      </tr>
      <tr>
         <td>
            <label for="audioLevel">cmi.learner_preference.audio_level:</label> 
         </td>
         <td>
            <input type="text" name="audioLevel" id="audioLevel" 
               value="<%=audioLevel%>" />
         </td>
      </tr>
      <tr>
         <td>
            <label for="language">cmi.learner_preference.language:</label>
         </td>
         <td>
            <input type="text" name="language" id="language" 
               value="<%=language%>" />
         </td>
      </tr>
      <tr>
         <td>
            <label for="deliverySpeed">cmi.learner_preference.delivery_speed:
            </label>
         </td>
         <td>
            <input type="text" name="deliverySpeed" id="deliverySpeed" 
               value="<%=deliverySpeed%>" />
         </td>
      </tr>
      <tr>
         <td>
            <label for="audioCaptioning">
               cmi.learner_preference.audio_captioning:</label>
         </td>
         <td>
            <input type="text" name="audioCaptioning" id="audioCaptioning"
             value="<%=audioCaptioning%>" />
         </td>
      </tr>
      <%
         if( isUserAdmin.equals( "true" ) )
         {
      %>
      <tr>
         <td>
            <label for="admin">User Rights:</label>
         </td>
         <td>
            <select name="admin" id="admin">
               <option value="true" <%if( isAdmin ){%>SELECTED<%}%>>Admin
               </option>
               <option value="false" <%if( !isAdmin ){%>SELECTED<%}%>>User
               </option>
            </select>
         </td>
      </tr>
      <%
         }
         else
         { %>
            <input type="hidden" name="admin" value="<%=isUserAdmin%>" />
      <% }

      %>
      <tr>
         <td colspan="2">
            <hr>
         </td>
      </tr>
      <tr>
         <td>
            &nbsp;
         </td>
      </tr>
      <tr>
         <td colspan=2 align="center">
            <input type="submit" name="Submit" value="Submit" />
         </td>
      </tr>
      <tr>
         <td>
            <A HREF="JavaScript: openHelp()">Help!</A>
         </td>
      </tr>
   </table>
</form>
</body>
</html>

