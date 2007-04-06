<%@ page contentType="text/html;charset=utf-8" %>

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
   ** Filename:  dsp_courses.jsp
   **
   ** File Description: This file displays a list of courses.
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
<%         
   String setProcess = (String)request.getAttribute("setProcess");
   int valueInt = 0;
   String formBody = "";
   String submitValue = "";
   String submissionType = "";
   String coursesListMsg = "";

   Vector coursesList = new Vector();
   coursesList = (Vector)request.getAttribute("courses");


   if (  coursesList.size() == 0 ) 
   {
        formBody = "<p class='font_header'><b>" +
            "No courses have been imported into the Sample RTE." +
            "</b></p>";
   }
   else
   {
  
   if (coursesList != null)
   {   coursesListMsg = "Courses List";
       formBody = "<table width='458'><tr><td colspan='2'><hr></td>" + 
         "</tr><tr><td bgcolor='#5E60BD' colspan='2' class='white_text'><b>" +
          "Please select courses: </b></td></tr></table>" + 
           "<table width='458'><tr bgcolor='white'><td align='left'" +
           " colspan='3'></td></tr>";

	  if ( setProcess.equals("manage") )
      {
         submissionType = "manage";
         valueInt = ServletRequestTypes.GET_SCOS;
         formBody += "<tr><td colspan='1'><label for='courseID'>Courses:" +
                    "</label><td colspan='3' align='left'>" + 
                    "<select name='courseID' id='courseID'>";
         for ( int i=0; i< coursesList.size(); i++)
         {
         	CourseData courseItem = (CourseData)coursesList.elementAt(i);
    			formBody += "<option value='" + courseItem.mCourseID + "'>" +
                        courseItem.mCourseTitle + " - " + 
								courseItem.mImportDateTime + "</option>";
         }
         
         formBody += "</select></td></tr>";
         submitValue = "";
        
	    } 
	    else 
	    {
           submissionType = "delete";
	       String color = "#FFFFFF";
	       valueInt = ServletRequestTypes.DELETE_COURSE;
	       for ( int i=0; i< coursesList.size(); i++)
	       {
	          CourseData courseItem = (CourseData)coursesList.elementAt(i);
	
	          formBody = formBody + "<tr bgcolor='" + color + 
                  "'><td width='10%'>" + 
                  "<input type='checkbox' name='chkCourse' id='" + 
                  courseItem.mCourseID + "' value='" + courseItem.mCourseID + 
                  "'/></td><td><label for='" + courseItem.mCourseID + "'><p>" +
                  courseItem.mCourseTitle + "<br>Imported on: " + 
	              courseItem.mImportDateTime + "</p></label></td></tr>";
	          
	          if(color.equals("#FFFFFF"))
	          {
	             color = "#CDCDCD";
	          }
	          else
	          {
	             color = "#FFFFFF";
	          }
	       }                                        
           submitValue="return checkValues()";                         
       }
        formBody += "<tr><td colspan='3'><input type='hidden' name='type'" +  
            "value=" + valueInt + " /><hr></td></tr><tr>" + 
            "<td colspan='3' align='center'>" +
             "<input type='submit' name='submit' value='Submit' /></td>" +
             "</tr></table>";
    }
   
   }
%>


<SCRIPT language="javascript">
var submitType = "";
/****************************************************************************
** isArray(obj)
** Returns true if the object is an array, else false
***************************************************************************/
function isArray(obj){return(typeof(obj.length)=="undefined")?false:true;}


/****************************************************************************
** getInputValue(input_object[,delimiter])
**   Get the value of any form input field
**   Multiple-select fields are returned as comma-separated values, or
**   delmited by the optional second argument
**  (Doesn't support input types: button,file,reset,submit)
***************************************************************************/
function getInputValue(obj,delimiter) {
    
	var use_default=(arguments.length>2)?arguments[2]:false;
	if (isArray(obj) && (typeof(obj.type)=="undefined")) {
		var values=new Array();
        ;
		for(var i=0;i<obj.length;i++){
			var v=getSingleInputValue(obj[i],use_default,delimiter);
			if(v!=null){values[values.length]=v;}
			}
		return commifyArray(values,delimiter);
		}
	return getSingleInputValue(obj,use_default,delimiter);
	}

/****************************************************************************
** getSingleInputValue(input_object,use_default,delimiter)
**   Utility function used by others
***************************************************************************/
function getSingleInputValue(obj,use_default,delimiter) {
	switch(obj.type){
		case 'radio': case 'checkbox': return(((use_default)?obj.defaultChecked:obj.checked)?obj.value:null);
		case 'text': case 'hidden': case 'textarea': return(use_default)?obj.defaultValue:obj.value;
		case 'password': return((use_default)?null:obj.value);
		case 'select-one':
			if (obj.options==null) { return null; }
			if(use_default){
				var o=obj.options;
				for(var i=0;i<o.length;i++){if(o[i].defaultSelected){return o[i].value;}}
				return o[0].value;
				}
			if (obj.selectedIndex<0){return null;}
			return(obj.options.length>0)?obj.options[obj.selectedIndex].value:null;
		case 'select-multiple': 
			if (obj.options==null) { return null; }
			var values=new Array();
			for(var i=0;i<obj.options.length;i++) {
				if((use_default&&obj.options[i].defaultSelected)||(!use_default&&obj.options[i].selected)) {
					values[values.length]=obj.options[i].value;
					}
				}
			return (values.length==0)?null:commifyArray(values,delimiter);
		}
	alert("FATAL ERROR: Field type "+obj.type+" is not supported for this function");
	return null;
}

/****************************************************************************
** commifyArray(array[,delimiter])
**  Take an array of values and turn it into a comma-separated string
**  Pass an optional second argument to specify a delimiter other than
**   comma.
***************************************************************************/
function commifyArray(obj,delimiter){
	if (typeof(delimiter)=="undefined" || delimiter==null) {
		delimiter = ",";
		}
	var s="";
	if(obj==null||obj.length<=0){return s;}
	for(var i=0;i<obj.length;i++){
		s=s+((s=="")?"":delimiter)+obj[i].toString();
		}
	return s;
}

/****************************************************************************
** Function:  checkValues()
** Input:   none
** Output:  boolean
**
** Description:  This function ensures that a course is selected
**               before submitting.
**
***************************************************************************/
function checkValues()
{              
    submitType = "<%=submissionType%>";
    if ( submitType == "delete" ) 
    {
        var inputValue = "";
        inputValue = getInputValue(scoAdmin.chkCourse);
        
        if (inputValue=="") 
        {
           alert('Please Select a Course.');
           return false;
        }
    }
return true;
}
</SCRIPT>

<head>
   <title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0
   - Courses</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

   <link href="includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

</head>

   <body bgcolor="#FFFFFF">

   <p>
   <a href="javascript:history.go(-1)">Back To Main Menu</a>
   </p>

   <p class="font_header">
   <b>
      <%= coursesListMsg %>
   </b>
   </p>
     <form method='post' action='/adl/LMSCourseAdmin'name='scoAdmin'
         onSubmit="return checkValues()"  >
         
       	<%= formBody %> 
         
   </form>
   </body>
</html>

<%
   }
   else
   {
      //Redirect the user to the LMSMenu page.
      response.sendRedirect( "runtime/LMSMain.htm" );
   }
%>
