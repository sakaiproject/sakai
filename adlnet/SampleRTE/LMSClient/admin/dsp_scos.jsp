
<%
   String is_admin = (String)session.getAttribute( "AdminCheck" );

   // Condition statement to determine if the Session Variable has been
   // properly set.
   if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
   {
%>

<%@page import = "java.util.*, org.adl.samplerte.util.*,
                  org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_scos.jsp
   **
   ** File Description: This file allows an administrator to update Comments
   **                   from LMS for selected scos.
   **
   **
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

<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
   Comments From LMS Admin</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

   <link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

</head>

   <body bgcolor="#FFFFFF">

   <p>
   <a href="javascript:history.go(-1)">Back to Courses View</a>
   </p>

   <p class="font_header">
   <b>
      Item List
   </b>
   </p>

   <form method="post" action="/adl/LMSCourseAdmin" name="scoAdmin">
      <input type="hidden" name="type" value="3" />

      <table width="450">
         <tr>
            <td colspan="2">
               <hr>
            </td>
         </tr>
         <tr>
            <td bgcolor="#5E60BD" colspan="2" class="white_text"><b>Please
            select the item you would like to update the comments in:</b>
            </td>
         </tr>
      </table>

      <table width="450">
         <tr>
            <td colspan="3" align="left">
               <label for="scoID"><b>Item Title </b></label>
            </td>
         </tr>
         <tr >
            <td colspan="3" align="left">
               <select name="scoID" >
         <!-- Loop Through the Particular SCO data of a selected Course -->
         <%
           Vector scoList = new Vector();
           scoList = (Vector)request.getAttribute("scos");

           if (scoList != null)
           {
             for ( int i=0; i< scoList.size(); i++)
             {
                SCOData scoItem = (SCOData)scoList.elementAt(i);

          %>

                <option value='<%= scoItem.mActivityID %>'>
                   <%= scoItem.mItemTitle %>
                </option>

         <%
             } // end for
         %>

               </select>
            </td>
        <%
          } // end if
        %>
         <tr>
            <td colspan="3">
               <hr>
            </td>
         </tr>
         <tr>
            <td colspan="2"></td>
         </tr>
         <tr >
            <td  colspan="3" align="center">
               <input type="submit" name="submit" value="Submit" />
            </td>
         </tr>
      </table>

   </form>
   </body>
</html>

<%
   }
   else
   {
      // Redirect the user to the LMSMenu page.
      response.sendRedirect( "runtime/LMSMain.htm" );
   }
%>