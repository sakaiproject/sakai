<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.sql.*, 
                  java.util.*, 
                  java.io.*, 
                  org.adl.samplerte.util.*,
                  org.adl.samplerte.server.CourseData" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_viewStatus.jsp 
   **
   ** File Description: This file displays Course Status information.
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
   ** Design Issues: None
   **
   ** Implementation Issues: None
   ** Known Problems: None
   ** Side Effects: None
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
   String name = (String)request.getAttribute("name");
   CourseData cd = (CourseData)request.getAttribute("status");
   String satisfied = cd.mSatisfied;
   String measure = cd.mMeasure;
   String completed = cd.mCompleted;
%>

<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Course Status
   </title> 
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <link href="includes/sampleRTE_style.css" rel="stylesheet" 
   type="text/css">
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
</head>
   
<body bgcolor="#FFFFFF">
  
   <p><a href="runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>
   <p class="font_header">
   <b>
      Course Status
   </b>
   </p>
   <form method="post"  name="viewStatus">
      <table WIDTH="458">
         <tr>
            <td>
               <hr>
            </td>
         </tr>
      </table>
      <table width="458" border="1" >
         <tr bgcolor="gray"  class="white_text">
           <th scope="col">
             <b> Learner </b>
           </th>
           <th scope="col">
             <b> Satisfied </b>
           </th>
           <th scope="col">
             <b> Measure </b>            
           </th>
           <th scope="col">
             <b> Completed </b>            
           </th>
         </tr>

         <tr>
            <td>
               <%= name %> 
            </td>
            <td>
               <%= satisfied %>
            </td>
            <td>
               <%= measure %>
            </td>
             <td>
               <%= completed %>
            </td>

         </tr>

      </table>
      
   </form>
</body>
</html>
     
