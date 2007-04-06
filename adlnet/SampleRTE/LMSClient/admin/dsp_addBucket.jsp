<%@ page contentType="text/html;charset=utf-8" %>

<%
String is_admin = (String)session.getAttribute( "AdminCheck" );

// Condition statement to determine if the Session Variable has been
// properly set.
if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
{
   %>


<%@page import = "org.adl.samplerte.server.ServletRequestTypes" %>

<%
   /***********************************************************************
   **
   ** Filename:  ssp_modifyAddBucket.jsp
   **
   ** File Description:  This file creates or updates a bucket for the admin.
   **
   **
   ** Author: ADLI Project
   **
   ** Company Name: Concurrent Technologies Corporation
   **
   ** Module/Package Name:  none
   ** Module/Package Description: none
   **
   ** Design Issues:
   **
   ** Implementation Issues:
   ** Known Problems:
   ** Side Effects:
   **
   ** References: ADL SCORM
   **
   ***************************************************************************
   **
   ** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you
   ** ("Licensee") a non-exclusive, royalty free, license to use, and
   ** redistribute this software, provided that i) this copyright notice and
   ** license appear on all copies of the software; and ii) Licensee does not
   ** utilize the software in a manner which is disparaging to ADL Co-Lab Hub.
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
   *************************************************************************/
   %>

   <html>
   <head>
   <title>SSP Admin - Add/Delete Bucket</title>
   <link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

   <script language=javascript>

   /*********************************************************************
   ** Function: checkValues()
   **
   ** Description:
   **    Checks to be sure that required fields are filled in.
   **
   **********************************************************************/
   function checkValues()
   {
      var alertString = "";
      levelFlag = "0";

      // Check for whitespace within the bucketID field
      var isNotValid_bucketID = true;
      var bucketIDVal = document.addBucketForm.bucketID.value;

      for ( var i = 0; i < bucketIDVal.length; i++ )
      {
         if ( bucketIDVal.charAt(i) == " " )
         {
            isNotValid_bucketID = true;
         }
         else
         {
            isNotValid_bucketID = false;
            break;
         }
      }

      if ( isNotValid_bucketID )
      {
         alertString = "ERROR: bucketID cannot be all whitespace.\n";
         levelFlag = "1";
      }

      // Check for whitespace within the courseID field
      var isNotValid_courseID = true;
      var courseIDVal = document.addBucketForm.courseID.value;

      for ( var i = 0; i < courseIDVal.length; i++ )
      {
         if ( courseIDVal.charAt(i) == " " )
         {
            isNotValid_courseID = true;
         }
         else
         {
            isNotValid_courseID = false;
            break;
         }
      }

      if ( isNotValid_courseID )
      {
         alertString += "ERROR: courseID cannot be all whitespace.\n";
         levelFlag = "1";
      }

      // Check for whitespace within the scoID field
      var isNotValid_scoID = true;
      var scoIDVal = document.addBucketForm.scoID.value;

      for ( var i = 0; i < scoIDVal.length; i++ )
      {
         if ( scoIDVal.charAt(i) == " " )
         {
            isNotValid_scoID = true;
         }
         else
         {
            isNotValid_scoID = false;
            break;
         }
      }

      if ( isNotValid_scoID )
      {
         alertString += "ERROR: scoID cannot be all whitespace.\n";
         levelFlag = "1";
      }

      // Check the bucketID Field
      if ( document.addBucketForm.bucketID.value == "" )
      {
         alertString += "ERROR: bucketID is a Required field.\n";
         levelFlag = "1";
      }

      // Check the requested Field
      var reqField = document.addBucketForm.requested.value
      if ( reqField == "" )
      {
         alertString += "ERROR: requested is a Required field.\n";
         levelFlag = "1";
      }
      else if ( isNaN(reqField) )
      {
         alertString += "ERROR: requested is not a number.\n";
         levelFlag = "1";
      }
      else if ( reqField % 2 != 0 )
      {
         alertString += "ERROR: requested is not even.\n";
         levelFlag = "1";
      }

      // Check the minimum field
      var minField = document.addBucketForm.minimum.value
      if ( minField != "" )
      {
         if ( isNaN(minField) )
         {
            alertString += "ERROR: minimum is not a number.\n";
            levelFlag = "1";
         }
         else if ( minField % 2 != 0 )
         {
            alertString += "ERROR: minimum is not even.\n";
            levelFlag = "1";
         }
      }

      // Check the courseID Field
      if ( document.addBucketForm.courseID.value == "" )
      {
         alertString += "ERROR: courseID is a Required field.\n";
         levelFlag = "1";
      }

      // Check the scoID Field
      if ( document.addBucketForm.scoID.value == "" )
      {
         alertString += "ERROR: scoID is a Required field.\n";
         levelFlag = "1";
      }

      // SUBMIT CONDITIONAL STATEMENTS
      if ( alertString == "" )
      {
         document.addBucketForm.submit();
      }
      else if ( levelFlag != "1" )
      {
         alert(alertString);
         document.addBucketForm.submit();
      }
      else
      {
         alert(alertString);
      }
   }

   </script>

   </head>

   <body>

   <%
   // Get attribute Value
   String userID = (String)request.getAttribute( "userId" );
   %>


      <p><a href="runtime/LMSMenu.jsp">Back To Main Menu</a></p>
      <p class="font_header">
      <b>Add Bucket Window</b>
      </p>

      <form method="post" name="addBucketForm" id="addBucketForm" 
      action ="/adl/LMSUserAdmin?type=<%= ServletRequestTypes.ADD_BUCKET %>"
      accept-charset="utf-8">

      <div id="addBucketForm">

      <!--  Hidden Values -->
      <input type="hidden" name="userID" id="userID" value="<%=userID%>">
                  
      <table border="0">
      <tr>
      <td bgcolor="#5E60BD" colspan="2" class="white_text">
      <b>Add Bucket Information</b></td>
      </tr>

      <!--BucketID -->
      <tr>
      <td>
      <label class="products">
      *&nbsp;bucketID:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="bucketID" name="bucketID">
      </td>
      </tr>

      <!--requested -->
      <tr>
      <td>
      <label class="products">
      *&nbsp;requested:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="requested" name="requested">
      </td>
      </tr>

      <!-- minimum -->
      <tr>
      <td>
      <label class="products">
      &nbsp;&nbsp;minimum:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="minimum" name="minimum">
      </td>
      </tr>

      <!--reducible -->
      <tr>
      <td>
      <label class="products">
      &nbsp;&nbsp;reducible:
      </label>
      </td>
      <td>
      <select id="reducible" name="reducible">
      <option value="false" selected>false</option>
      <option value="true">true</option>
      </select>
      </td>
      </tr>

      <!-- persistence -->
      <tr>
      <td>
      <label class="products">
      &nbsp;&nbsp;persistence:
      </label>
      </td>
      <td>
      <select id="persistence" name="persistence">
      <option value="0" selected>learner</option>
      <option value="1">session</option>
      <option value="2">course</option>
      </select>
      </td>
      </tr>

      <!--type -->
      <tr>
      <td>
      <label class="products">
      &nbsp;&nbsp;type:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="bucketType" name="bucketType">
      </td>
      </tr>
      
      <tr>
      <td bgcolor="#5E60BD" colspan="3" class="white_text">
      <b>Course Information</b></td>
      </tr>

      <!--courseID -->
      <tr>
      <td>
      <label class="products">
      *&nbsp;courseID:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="courseID" name="courseID">
      </td>
      </tr>

      <!--scoID -->
      <tr>
      <td>
      <label class="products">
      *&nbsp;scoID:
      </label>
      </td>
      <td>
      <input type="text" size="75" id="scoID" name="scoID">
      </td>
      </tr>
      
      <tr>
      <td colspan="2" align="left">
      <label class="products">
      * required
      </label>
      </td>
      </tr>
      
      <tr>
      <td colspan="2" align="left">
      <input type="button" id="createNewBucket"
      name="createNewBucket"
      value="Create Bucket"
      onClick="checkValues()"/>
      </td>
      </tr>
      
      </table>
      
      </div>
      </form>

      <br />

   </body>
   </html>

   <%
}
else
{
   // Redirect the user to the LMSMenu page.
   response.sendRedirect( "../runtime/LMSMain.htm" );
}
%>                                                       
