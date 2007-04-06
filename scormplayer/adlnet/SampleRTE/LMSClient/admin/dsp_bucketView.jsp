<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.*, java.io.*, org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_viewBucket.jsp
   **
   ** File Description: This file allows an admin to view current buckets.
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
      // Get the user's information and remove any course IDs
   String userID = (String)request.getAttribute( "userId" );

   String userDir = File.separator + "SCORM3rdSampleRTE10Files" +
      File.separator + userID;

   File theRTESCODataDir = new File( userDir );

   // The course directory should not exist yet
   if ( !theRTESCODataDir.isDirectory() )
   {
       theRTESCODataDir.mkdirs();
   }
     
   
   List bucketList = (ArrayList)request.getAttribute( "bucketList" );

   String bucketSelect = 
      "<form id=\"bucketForm\" action=\"/adl/LMSUserAdmin\"" + 
            "method=\"post\">" + 
      "<table cellpadding=\"8\">" + 
         "<thead><tr bgcolor=\"#5E60BD\" class=\"white_text\">" + 
            "<td align=\"center\"></td>" + 
            "<td class=\"white_text\" align=\"center\">Bucket ID</td>" +
            "<td class=\"white_text\" align=\"center\">Course ID</td>" + 
            "<td class=\"white_text\" align=\"center\">SCO ID</td>" + 
            "<td class=\"white_text\" align=\"center\">Attempt ID</td>" + 
            "<td class=\"white_text\" align=\"center\">Minimum</td>" +
            "<td class=\"white_text\" align=\"center\">Requested</td>" + 
            "<td class=\"white_text\" align=\"center\">Persistence</td>" + 
            "<td class=\"white_text\" align=\"center\">Reallocation Failure</td>" +
         "</tr></thead>";

   if ( bucketList == null )
   {
      bucketSelect = 
         "<form id=\"bucketForm\" action=\"/adl/LMSUserAdmin\"" + 
         "method=\"post\">";
      bucketSelect += "No buckets were found. <BR>";
      bucketSelect += 
         "<input type=\"submit\" name=\"submit\" value=\"" + ServletRequestTypes.ADD_BUCKET_REQ + "\">";
      bucketSelect += 
         "<input type=\"hidden\" name=\"userID\" value=\"" + userID + "\">";
      bucketSelect += "</form>";
   }
   else
   {
      BucketProfile bucketProfile;
      String buckID = "";
      String min = "";
      String req = "";
      String courseID = "";
      String actID = "";
      String persistence = "";
      String scoID = "";
      String attemptID = "";
      boolean reallocate = false;

      for ( int i = 0; i < bucketList.size(); i++ )
      {
         bucketProfile = (BucketProfile)bucketList.get(i);

         buckID = bucketProfile.mBucketID;
         min = bucketProfile.mMinimum;
         req = bucketProfile.mRequested;
         courseID = bucketProfile.mCourseID;
         scoID = bucketProfile.mSCOID;
         actID = bucketProfile.mActivityID;
         switch ( bucketProfile.mPersistence )
         {
            case 0:
            {
               persistence = "learner";
               break;
            }
            case 1:
            {
               persistence = "course";
               break;
            }
            case 2:
            {
               persistence = "session";
               break;
            }
            default:
            {
               persistence = "";
               break;
            }
         }
         attemptID = bucketProfile.mAttemptID;
         reallocate = bucketProfile.mReallocationFailure;
   
         bucketSelect += "<tr>";
   
         bucketSelect += 
            "<td align=\"center\">" +
               "<input type=\"radio\" id=\"bID\" name=\"bID\" value=\"" + 
                  actID + "\"></td>" + 
            "<td align=\"center\">" + buckID + "</td>" + 
            "<td align=\"center\">" + courseID + "</td>" + 
            "<td align=\"center\">" + scoID + "</td>" + 
            "<td align=\"center\">" + attemptID + "</td>" + 
            "<td align=\"center\">" + min + "</td>" + 
            "<td align=\"center\">" + req + "</td>" +
            "<td align=\"center\">" + persistence + "</td>" +
            "<td align=\"center\">" + reallocate + "</td>";
   
   
         bucketSelect += "</tr>";
      }
   
      bucketSelect += "</table>";
      bucketSelect += "<input type=\"button\" name=\"add\" value=\"Add\" onClick=\"javascript:sendForm('" + ServletRequestTypes.ADD_BUCKET_REQ + "')\">";
      bucketSelect += "<input type=\"button\" name=\"delete\" value=\"Delete\" onClick=\"sendForm('" + ServletRequestTypes.DELETE_BUCKET + "')\">";
      bucketSelect += "<input type=\"hidden\" name=\"type\" value=\"\">";
      bucketSelect += "<input type=\"hidden\" name=\"userID\" value=\"" + userID + "\">";
      bucketSelect += "</form>";
   }
%>

<html>
<head>
<meta http-equiv="expires" content="Tue, 20 Aug 1999 01:00:00 GMT">
<meta http-equiv="Pragma" content="no-cache">
<title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 -
      SSP View Bucket</title>

<link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

<script language=javascript>
// Hide or display the relevant controls
if( document.all != null )
{
   window.parent.frames[0].document.forms[0].suspend.style.visibility="hidden";
   window.parent.frames[1].document.location.href = "code.jsp";
   window.top.frames[0].document.forms[0].next.style.visibility = "hidden";
   window.top.frames[0].document.forms[0].previous.style.visibility = "hidden";
   window.top.frames[0].document.forms[0].quit.style.visibility = "hidden";
}
function sendForm( action )
{
    // can't use ServletRequestTypes here 
    // ADD_BUCKET_REQ = 26
    // DELETE_BUCKET = 28
    if ( action == 26 )
	{
		document.forms[0].type.value = action;
	}
	else if ( action == 28 )
	{
		document.forms[0].type.value = action;
	}
	
	document.forms[0].submit();
}
</script>
</head>

<body >

<%
   // Get the user's information and remove any course IDs
   String userid = (String)session.getAttribute( "USERID" );
   String admin = (String)session.getAttribute( "RTEADMIN" );
   session.removeAttribute( "COURSEID" );
   session.removeAttribute( "TOC" );
%>

    <p><a href="runtime/LMSMenu.jsp">Back To Main Menu</a></p> 

    <p class="font_header">
    <b>
    Sharable State Persistence Administrator options:
    </b>
    </p>

<%
   if ( (! (admin == null)) && ( admin.equals("true")) )
   {
       // Sets a new Session Variable to Secure Admin pages
    session.putValue("AdminCheck", new String("true"));
%>
      <p><%= bucketSelect %></p>
<%
   }
%>

</body>
</html>
