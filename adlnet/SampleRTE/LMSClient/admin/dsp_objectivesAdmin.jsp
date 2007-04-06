<%
   String is_admin = (String)session.getAttribute( "AdminCheck" );
   
   // Condition statement to determine if the Session Variable has been
   // properly set.
   if ( (! (is_admin == null)) && ( is_admin.equals("true")) )
   {
%>
<%@page import = "java.sql.*, java.util.*, 
                  java.io.*, org.adl.samplerte.server.*" %>
<%
   /***************************************************************************
   **
   ** Filename:  objectivesAdmin.jsp 
   **
   ** File Description: This file allows an administrator to update or delete
   **                   Global Objective values.
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
      Vector courseObjs = (Vector)request.getAttribute("objs");

      String objID = null;
      String learnerID = null;
      String satisfied = null;
      String measure = null;
	  String bodyString = "<tr>";
      ObjectivesData od = null;
      ObjectivesData odG = null;
           
      // Loops through all of the global objectives and outputs them in the
      // table with a radio button for selection of delete or reset.
      for ( int i = 0; i < courseObjs.size(); i++ )
      {  
         od = (ObjectivesData)courseObjs.elementAt(i);
            
         objID = od.mObjectiveID;
         learnerID = od.mUserID;
         satisfied = od.mSatisfied;
         measure = od.mMeasure;
         bodyString += "<td scope='row'><input type='radio' name='" + objID + 
                       "~" + learnerID + "' value='reset'/></td>";
         bodyString += "<td><input type='radio' name='" + objID + "~" +
					   learnerID + "' value='delete'/></td>";
         bodyString += "<td><input type='radio' name='" + objID + "~" +
					   learnerID + "'/></td>";
         bodyString += "<td>" + objID + "</td>";
         bodyString += "<td>" + learnerID + "</td>";
         bodyString += "<td>" + satisfied + "</td>";
         bodyString += "<td>" + measure + "</td>";
			bodyString += "</tr>";
      }
%>
<html>
<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0 - 
    Global Objectives Admin
   </title> 
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<script language=javascript>

  /**********************************************************************
   **  Function: resetAll()
   **  Description: This function selects all the reset buttons.
   **    
   **********************************************************************/
   function resetAll( )
   {
       var numRadioButtons = 3;
       var form = window.document.forms[0];
       for (var i=0; i<window.document.compAdmin.elements.length; i++)  
       {  
          if ( form.elements[i].type == "radio") 
         {    if ( ( i % numRadioButtons ) == 0)
              form.elements[i].checked = true;
          }   
       }
       document.forms[0].selectAll[0].checked = false;
    }

   /**********************************************************************
   **  Function: deleteAll()
   **  Description: This function selects all the delete buttons.
   **    
   **********************************************************************/
   function deleteAll( )
   {
       var numRadioButtons = 3;
       var form = window.document.forms[0];
       for (var i=0; i<window.document.compAdmin.elements.length; i++)  
       { 
          if ( form.elements[i].type == "radio") 
          {   
              if ( ( i % numRadioButtons ) == 1)
              form.elements[i].checked = true;
          }   
       }
       document.forms[0].selectAll[1].checked = false;

    }
    
   /**********************************************************************
   **  Function: clearAll()
   **  Description: This function selects all the delete buttons.
   **    
   **********************************************************************/
   function clearAll( )
   {
       var form = window.document.forms[0];
       for (var i=0; i<window.document.compAdmin.elements.length; i++)  
       { 
          if ( form.elements[i].type == "radio") 
          {   
              
              form.elements[i].checked = false;
          }   
       }

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

   <link href="/adl/includes/sampleRTE_style.css" rel="stylesheet" type="text/css">
   
</head>
   
<body bgcolor="#FFFFFF">
  
   <p><a href="/adl/runtime/LMSMenu.jsp">Go Back To Main Menu</a></p>
   <form method="post" 
         action="/adl/LMSCourseAdmin?type=<%= ServletRequestTypes.EDIT_OBJ %>" 
         name="compAdmin">
      <table width="450" border="0">
         <tr>
            <td>
               <hr>
            </td>
         </tr>
         <tr>
            <td bgcolor="#5E60BD" colspan="2" class="white_text">
			   <b>
                  &nbsp;Please select the global objectives you would like to 
				                                           delete or reset
               </b>
            </td>
         </tr>
      </table>
      <table width="450" border="1" >
         <tr bgcolor="gray"  class="white_text">
           <th scope="col">
             <b> Reset </b>
           </th>
           <th scope="col">
             <b> Delete </b>                 
           </th>
           <th scope="col">
             <b> Clear Selection </b>                 
           </th>
           <th scope="col">
             <b> ObjID </b>
           </th>
           <th scope="col">
             <b> Learner </b>
           </th>
           <th scope="col">
             <b> Satisfaction <br />
                 Status </b>
           </th>
           <th scope="col">
             <b> Measure </b>            
           </th>
         </tr>

		<%= bodyString %>

         <tr>
           <td scope="row">
              <input type='radio' name='selectAll' value='reset' 
                                                   onClick="resetAll()" />
           </td>
           <td>
              <input type='radio' name='selectAll' value='delete' 
                                                   onClick="deleteAll()" />
           </td>
           <td>
              <input type='radio' name='selectAll' value='clear' 
                                                   onClick="clearAll()" />  
           </td> 
           <td>
              Select All
           </td>
           <td>&nbsp;</td>
           <td>&nbsp;</td>
           <td>&nbsp;</td>
         </tr>
      </table>
      <table width="450">
         <tr>
           <td></td>
         </tr>
         <tr>
            <td>
               <hr>
            </td>
         </tr>
         <tr >
           <td align="center">
              <input type="submit" name="submit" value="Submit" />
           </td>
         </tr>
         <tr>
            <td>
               <A href="javascript:newWindow('/adl/help/globalObjectivesHelp.htm');">
               Help!</A>
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
      response.sendRedirect( "/adl/runtime/LMSMain.htm" ); 
   }
%>

