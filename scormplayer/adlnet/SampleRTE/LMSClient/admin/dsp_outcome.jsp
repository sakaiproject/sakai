<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.util.*, org.adl.samplerte.util.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  dsp_outcome.jsp 
   **              
   ** File Description: This file displays a message to the user to indicate 
   **                   that they have successfully updated a user's 
   **                   preferences.
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
<%
 String reqOp = (String)request.getAttribute("reqOp");
 String bodyText = "";
 String bodyText2 = "";
 int errorCode = 0;

 if ( ((String)request.getAttribute("result")).equals("true") )
 {
   	bodyText = reqOp + " was successful.";
 }
 else
 {
    Integer tempInt = ((Integer)request.getAttribute("errorCode"));

    if ( tempInt != null )
    {
       errorCode = tempInt.intValue();
    }

    bodyText = reqOp + " was not successful.";
    if ( errorCode != 0 )
    {
       bodyText2  = "<br><br><b>SSP Error Code:<b> " 
                 + errorCode;
       bodyText2 += "<br><b>SSP Error Description:<b> " 
                 + (String)request.getAttribute("errorDesc");
       bodyText2 += "<br><b>SSP Error Diagnostic:<b> "
                 + (String)request.getAttribute("errorDiag");
    }
 }
%>
<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
   Outcome</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

   <link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

</head>

   <body bgcolor="#FFFFFF">

   <p>
   <a href="runtime/LMSMenu.jsp">Go Back To Main Menu</a>
   </p>

   <table width="458" border="0">
      <tr>
         <td class="font_header">
            <b>
               <%= bodyText %>
            </b>
         </td>
      </tr>
      <tr>
         <td>
            <%= bodyText2 %>
         </td>
      </tr>
   </table>
   </body>
</html>

