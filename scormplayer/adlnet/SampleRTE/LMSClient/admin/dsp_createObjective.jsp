<%@ page contentType="text/html;charset=utf-8" %>

<%
   String is_admin = (String)session.getAttribute( "AdminCheck" );
   
   // Condition statement to determine if the Session Variable has been
   // properly set.
   if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
   {
%>
<%@page import = "java.sql.*, java.util.Vector,
                  org.adl.samplerte.util.*,
                  org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  createObjective.jsp 
   **
   ** File Description:   This file allows an admin to enter information to
   **                     create a new global objective.
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
   Create a Global Objective</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

   <link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
   <script language="javascript">


   /**************************************************************************
   **
   ** Function:  checkData()
   ** Input:   none
   ** Output:  none
   **
   ** Description:  This method checks to be sure data was entered.    
   ***************************************************************************/
   function checkData()
   {
      noErrors = true;
      errorMessage = "";
      if ( createObjective.objectiveID.value == ""  || 
           createObjective.measure.value == "" )
      {
         errorMessage = errorMessage + 
                        "Please enter a value for all fields.";
         noErrors = false;
      }
		else if ( createObjective.measure.value < -1 ||
					 createObjective.measure.value > 1 )
		{
			errorMessage = "Please enter a measure value between -1.0 and 1.0.";
			noErrors = false;
		}
		else if ( isNaN(createObjective.measure.value) &&
					 !(createObjective.measure.value == "unknown") )
		{
			errorMessage = "Please enter a value between -1.0 and 1.0 or unknown.";
			noErrors = false;
		}

      if ( !noErrors )
      {
         alert(errorMessage);
      }
      return noErrors;
   }


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
   <body>

   <p>
   <a href="/adl/runtime/LMSMenu.jsp">Go Back To Main Menu</a>
   </p>

<%
   
   String createObjError = (String)request.getAttribute("objErr");
   String objectiveID = "";
   String ObjUserID = "";
   String ObjSatisfied = "";
   String ObjMeasure = "unknown"; 

   if ( !createObjError.equals("") )
   {
      //ObjectivesData od = (ObjectivesData)request.getAttribute("objData");
      objectiveID = (String)request.getAttribute("objID");
      ObjUserID = (String)request.getAttribute("userID");
      ObjSatisfied = (String)request.getAttribute("satisfied");
      ObjMeasure = (String)request.getAttribute("measure");
   
      if( createObjError.equals( "dupobjid" ) )
      {
%>
   <h2>The following error was caught</h2>
   <p>
   This Objective ID already exists for this user, please choose another 
   Objective ID
   </p>
<%
      }
   }
%>


   <p class="font_header">
   <b>Create a Global Objective</b>
   </p>
   <form method="post" 
   		name="createObjective"
   		action="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.ADD_OBJ %>"
         onsubmit="return checkData()">
      <table width="450" border="0" align="left">
         <tr>
            <td colspan="2">
               <hr>
            </td>
         </tr>
         <tr>
            <td bgcolor="#5E60BD" colspan="2" class="white_text"><b>&nbsp;  
            Please provide the following new objective information:</b></td>
         </tr>
         <tr>
            <td width="20%"><label for="userID">User ID:</label></td>
            <td>
               <select name="userID" id="userID">
<%
               Vector users = (Vector)request.getAttribute("users");
               String userID = "";
               for( int i = 0; i < users.size(); i++)
               {
                  UserProfile userProf = (UserProfile)users.elementAt(i);
                  userID = userProf.mUserID;
                  if( userID.equals(ObjUserID) )
                  {
%>
               <option selected><%=userID%></option>
<%
                  }
                  else
                  {
%>
               <option><%=userID%></option>
<%
                  }
               }
%>
            
               </select>
            </td>
         </tr>
         <tr>
            <td><label for="objectiveID">Objective ID:</label></td>
            <td>
<%
            if( objectiveID != null )
            {
%>
               
               <input type="text" name="objectiveID" id="objectiveID" 
                  value="<%=objectiveID%>">
<%
            }
            else
            {
%>
               
               <input type="text" name="objectiveID" id="objectiveID">
<%
            }
%>
         </td>
         <tr>
            <td><label for="satisfied">Satisfaction<br />
            	 status:</label></td>
            <td>
               <select name="satisfied" id="satisfied">
                  <option selected>unknown</option> <option>satisfied</option>  
                  <option>not satisfied</option>
               </select>
            </td>
         </tr>
         <tr>
            <td><label for="measure">Measure:</label></td>
            <td>
<%
            if( ObjMeasure != null)
            {
%>
               
               <input type="text" name="measure" id="measure" 
                  value="<%=ObjMeasure%>">
<%
            }
            else
            {
%>
               
               <input type="text" name="measure" id="measure" value="unknown">
<%
            }
%>
         </td>
         </tr>
         <tr>
            <td>&nbsp;</td>
            <td>(A decimal value between -1.0 and 1.0, if unspecified or invalid 
            this value will default to "unknown")

            </td>
         </tr>
         <tr>
            <td colspan="2">
               <hr>
            </td>
         </tr>
         <tr>
            <td>&nbsp;</td>
         </tr>
         <tr>
            <td colspan="2" align="center">
               <input type="submit" value="Submit">
            </td>
         </tr>
         <tr>
            <td>
              <a href="javascript:newWindow('/adl/help/globalObjectivesHelp.htm');
                                                                ">Help!</a></td>
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

