<% 
   String is_admin = (String)session.getAttribute( "AdminCheck" );
   
   // Condition statement to determine if the Session Variable has been
   // properly set.
   if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
   {
      ValidationResults vr = (ValidationResults)request.getAttribute("result");
%>
<% 
   /***************************************************************************
   **
   ** Filename:  invalidImport.jsp
   **
   ** File Description: This page displays a message saying that the course  
   **                   intended for import was invalid and gives a  
   **                   summary of the validator results.  It also contains a 
   **                   link to the main menu.
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

<%@page import = "org.adl.util.*, java.util.*,
						org.adl.logging.*,
                  org.adl.samplerte.server.*"%>
<%@ include file="importUtil.jsp" %>
<%
	String validate = vr.getValidation();
   String manifestExists = vr.getManifestExists();
   String wellFormed = vr.getWellFormed();
   String validRoot = vr.getValidRoot();
   String validToSchema = vr.getValidToSchema();
   String validToProfile = vr.getValidToProfile();
   String requiredFiles = vr.getRequiredFiles();
   String bodyString = "";
   bodyString = "<p><a href='/adl/runtime/LMSMenu.jsp'>Go Back To Main Menu</a>" +
                "<div class=font_header><p>The Course Was Invalid" +
                "</p></div><p>&nbsp;</p></p><table><tr>" +
                "<td class='red_text'><b>Validation Trouble:</br></td></tr>";
   if ( manifestExists.equals("false") )
   {
      bodyString = bodyString + "<tr><td class='red_text'><b>" +
                   "* imsmanifest.xml file is not located at the root of the " +
                   "package.</b></td></tr>";
   }
   else if ( wellFormed.equals("false") )
   {
      bodyString = bodyString + "<tr><td class='red_text'><b>" +
                   "*  The imsmanifest.xml file is not well-formed." +
                   "</b></td></tr>";
   }
   else if ( validRoot.equals("false") )
   {
      bodyString = bodyString + "<tr><td class='red_text'><b>" +
                   "*  The root element does not belong to the expected " +
                   "namespace.</b></td></tr>";
   }
   else
   {
      if ( validate.equals("true") )
      {
         if ( requiredFiles.equals("false") )
         {
            bodyString = bodyString + "<tr><td class='red_text'>*  Control documents are not " +
                         "located at the root of the package.</b></td></tr>";
         }
         if ( validToSchema.equals("false") )
         {
            bodyString = bodyString + "<tr><td class='red_text'><b>" +
                       "*  The imsmanifest.xml file is not valid against the " +
                       "schemas.</b></td></tr>";
         }
         if ( validToProfile.equals("false") )
         {
            bodyString = bodyString + "<tr><td class='red_text'><b>" +
                       "*  The imsmanifest.xml file is not valid to the " +
                       "requirements defined in SCORM 2004 3rd Edition.</b></td></tr>";
         }
      }
   }
   
      

   if ( (manifestExists.equals("false") || wellFormed.equals("false") || 
         validRoot.equals("false")) || 
       (validate.equals("true") && (requiredFiles.equals("false") || 
        validToSchema.equals("false") || validToProfile.equals("false"))) )
   {
      bodyString = bodyString + "<P>";
		DetailedLogMessageCollection dlmc = DetailedLogMessageCollection.getInstance();
      LogMessage currentMessage;
      String messageText = "";
      String fileSeperator = "\\\\";
      String backSlash = "\\\\\\\\";
      int collSize = dlmc.getSize();
      
      for(int i = 0; i < collSize; i++)
      {
			currentMessage = dlmc.getMessage();
         messageText = makeReadyForPrint(currentMessage.getMessageText());
         messageText = messageText.replaceAll(backSlash, fileSeperator );

         if ( currentMessage.getMessageType() == MessageType.FAILED )
         {
            bodyString = bodyString + "<TR><TD class='red_text'>" + 
                          "&nbsp;&nbsp;&nbsp;ERROR: " + messageText
                           +
                          "</TD></TR>";
         }
         else if ( currentMessage.getMessageType() == MessageType.WARNING )
         {
				bodyString = bodyString + "<TR><TD class='orange_text'>" +
                          "&nbsp;&nbsp;&nbsp;WARNING: "+
                          makeReadyForPrint(currentMessage.getMessageText()) +
                          "</TD></TR>";
         }
         else if ( currentMessage.getMessageType() == MessageType.OTHER )
         {
            bodyString = bodyString + "<tr><td>" + makeReadyForPrint(currentMessage.
                         getMessageText()) + "</td></tr>";
         }
      }
      //out.close();

      bodyString = bodyString + "</P>";
   }
   bodyString = bodyString + "</table>";
%>


<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Invalid Course</title>
   <link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" 
                                                type="text/css">
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
         
   
<%= bodyString %>

</body>

</table>
</html>

<%
   }
   else
   {
      // Redirect the user to the LMSMenu page.
      response.sendRedirect( "/adl/runtime/LMSMain.htm" ); 
   }   
%>


