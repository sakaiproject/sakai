/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

// does "hide" (hideUnhideAllDivs()) need to be run?
var runHide=true;
// should function be disabled?
var wysiwygShowHideDiv=false;

// show/hide hideDivision tag with JSF id of "hideDivisionNo" with "context" path
function showHideDiv(hideDivisionNo, context)
{
  //alert("showHideDiv()");
  var tmpdiv = "__hide_division_" + hideDivisionNo;
  var tmpimg = "__img_hide_division_" + hideDivisionNo;
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);
  if(divisionNo)
   {
    if(divisionNo.style.display =="block")
    {
      divisionNo.style.display="none";
      if (imgNo)
      {
        imgNo.src = context + "/images/right_arrow.gif";
      }
    }
    else
    {
      divisionNo.style.display="block";

      if(imgNo)
      {
       imgNo.src = context + "/images/down_arrow.gif";
      }

      // Gecko fix, remove wysiwygs, recreate
      if (wysiwygShowHideDiv && navigator.product == "Gecko")
      {
        removeWysiwygi(divisionNo);
        resetWysiwygi(divisionNo);
      }
    }
   }
}

// if a DIV id has our special flag, toggle its visibility
function hideUnhideAllDivs(action)
{
  //alert("hideUnhideAllDivs()");
  if(runHide==true)
  {
    runHide=false;
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
        divisionNo = "" + myDocumentElements[i].id;
        if (divisionNo.indexOf("__hide_division_")==0)
        {
            elem = document.getElementById(divisionNo);
            if (elem){
               //Don't hide if FF and FCK.. let FCK hide all later
               if (document.wysiwyg == "FCKeditor" && navigator.product == "Gecko")
               {
               }
               else
               {
                   elem.style.display =action;
               }
            }
        }
    }
  }
}

function hideUnhideAllDivsExceptFirst(action)
{
  //alert("hideUnhideAllDivsExceptFirst()");
  if(runHide==true)
  {
    runHide=false;
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
      
        if (i==0) continue;
          divisionNo = "" + myDocumentElements[i].id;
          if (divisionNo.indexOf("__hide_division_")==0)
          {
            elem = document.getElementById(divisionNo);
            if (elem){
            elem.style.display =action;
            }
          }
    }
  }
}

//special handling if page has WYSIWYGs
function hideUnhideAllDivsWithWysiwyg(action)
{
  wysiwygShowHideDiv = true;
  hideUnhideAllDivs(action)
}


// if there are _any_ wysiwyg editors present toggle into wysiwyg mode
// for all wysiwygs in division; needed for Gecko based browsers
function resetWysiwygi(hDiv)
{
 //alert("resetWysiwygi()");
 if (document.htmlareas != undefined)
 {
   var counter = document.htmlareas.length;
   // alert(counter);

   for (i=0; i<counter; i++)
   {
    // alert("i="+i);
    var editor = document.htmlareas[i][1];
    if (editor == undefined)
    {
      // in a bad state, so we recreate wysiwyg
      // first look up the ith wysiwyg id
      var ta = document.htmlareas[i][0];
      // alert("ta = " + ta);

      // check if the textarea is in the division
      var tas = hDiv.getElementsByTagName("textarea");
      for (j=0; j<tas.length; j++)
      {
       var childId = tas[j].id;
       // alert("childId=" + childId);

       if (childId==ta)
       {
        // we make sure that the textarea is not set to "none"
        tas[j].style.display = "block";

        // finally, create a configuration and assign an HTMLArea to this textarea
        var config=new HTMLArea.Config();
        config.toolbar = [['fontname', 'space','fontsize', 'space','formatblock',
          'space','bold', 'italic', 'underline'],    ['separator','strikethrough',
          'subscript', 'superscript', 'separator', 'space', 'undo', 'redo',
          'separator', 'justifyleft', 'justifycenter', 'justifyright',
          'justifyfull', 'separator','outdent', 'indent'],        ['separator',
          'forecolor', 'hilitecolor', 'textindicator', 'separator',
          'inserthorizontalrule', 'createlink', 'insertimage', 'separator',
          'showhelp', 'about' ],];
        config.width='450px';
        config.height='140px';
        editor = HTMLArea.replace(ta,config);
        document.htmlareas[i][1] = editor;
        break;
       }
     }
    }
   }
 }
 else 
 {
    var tas = hDiv.getElementsByTagName("textarea");
    for (j=0; j<tas.length; j++)
    {
       var childId = tas[j].id;
       if (document.wysiwyg == "FCKeditor")
       {
           editor = FCKeditorAPI.GetInstance(childId);
           if (editor && editor.EditorDocument && editor.EditMode == FCK_EDITMODE_WYSIWYG) {
              editor.SwitchEditMode()
              editor.SwitchEditMode()
           }
       }  

    }
    return;
 }


}

// remove the secret wysiwyg divs that substituted for the textareas
function removeWysiwygi(hDiv)
{
	//alert("removeWysiwygi for " + hDiv.id);
    var subDivs=hDiv.getElementsByTagName("div");
    for (i=0;i<subDivs.length;i++)
    {
      var child = subDivs[i];
      var childClass = "" + child.className;
      if (childClass.indexOf("htmlarea")==0)
      {
        child.parentNode.removeChild(child);
      }
    }
}

// getElementById with special handling of old browsers
function getTheElement(thisid){

  var thiselm = null;

  if (document.getElementById)
  {
    // browser implements part of W3C DOM HTML ( Gecko, Internet Explorer 5+, Opera 5+
    thiselm = document.getElementById(thisid);
  }
  else if (document.all){
    // Internet Explorer 4 or Opera with IE user agent
    thiselm = document.all[thisid];
  }
  else if (document.layers){
    // Navigator 4
    thiselm = document.layers[thisid];
  }

  if(thiselm)	{

    if(thiselm == null)
    {
      return;
    }
    else
    {
      return thiselm;
    }
  }
}

/* 
 * added by Joshua Ryan Joshua.ryan@asu.edu  7/11/06
 * The FCK editor will call this function when it's done loading/rendering
 * It's a known bug that FCK won't work on FF in a div with display=none,
 * so we delay turning dispaly=none to hide menu items until the editor is
 * done rendering on FF.
 *
 * TODO: When we upgrade to FCK 2.3, switch this to look for FF version as 
 * this bug is fixed in FCK 2.3 for FF > 1.5
 *
 * Fix for SAK-6937 - don't do anything here. Make a call to retainHideUnhideStatus().
 */
function FCKeditor_OnComplete( editorInstance )
{
   //alert("FCKeditor_OnComplete()");
   /*
   if (navigator.product == "Gecko")
   { 
      hideDivs();
   }
   */
}

var exceptionIds = "";
function retainHideUnhideStatus(action)
{
  wysiwygShowHideDiv = true;
  //alert("retainHideUnhideStatus()");
  exceptionIds = document.forms[0].elements['assessmentSettingsAction:blockDivs'].value
  var exceptionIdArray = new Array();
  if (exceptionIds != "") {
	  var splitDivs = exceptionIds.split(";");
      //alert("splitDivs length=" + splitDivs.length);
	  for(i = 0; i < splitDivs.length; i++){
		var exceptionFullId = "__hide_division_assessmentSettingsAction:" + splitDivs[i];
	    //alert("exceptionFullId=" + exceptionFullId);
		exceptionIdArray.push(exceptionFullId);
	  }
  }
  
  //alert("length=" + exceptionIdArray.length);

  if(runHide==true)
  {
    runHide=false;
    myDocumentElements=document.getElementsByTagName("div");

    for (i=0;i<myDocumentElements.length;i++)
    {
      var unhide = "false";
      divisionNo = "" + myDocumentElements[i].id;
      if(divisionNo != null && exceptionIdArray.length != 0)
      {
		 //alert("divisionNo=" + divisionNo);
         for(j = 0; j < exceptionIdArray.length; j++){
			 if (exceptionIdArray[j] == divisionNo) {
				 //alert("exceptionIdArray[j]=" + exceptionIdArray[j]);
				 //alert("unhide!");
				 unhide = "true";
				 myDocumentElements[i].style.display = "block";
  			     var exceptionImgId = "__img" + exceptionIdArray[j].substring(1);
				 var imgNo = getTheElement(exceptionImgId);
				 if(imgNo) {
					 imgNo.src = "/samigo/images/down_arrow.gif";
				 }
				 break;
			 }
		 }
      }

      if (unhide == "false" && divisionNo.indexOf("__hide_division_")==0)
      {
        elem = document.getElementById(divisionNo);
        if (elem)
        {
          //Don't hide if FF and FCK.. let FCK hide all later
          if (document.wysiwyg == "FCKeditor" && navigator.product == "Gecko")
          {
          }
          else
          {
			//alert("reset?!");
            elem.style.display =action;
          }
        }
      }
    }
  }
  exceptionId = "";
}

//Huong's adding for use of authoring and template setting page
function showDivs()
{   
    //alert("showDivs()");
	var divisionNo=""; 
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
      if(divisionNo.indexOf("__hide_division_")>=0)
        { 
             showDiv(divisionNo.substring(16),'/samigo');
           
        }
    }  
 }
function hideDivs()
{  
	//alert("hideDivs()");
	var divisionNo=""; 
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
      if(divisionNo.indexOf("__hide_division_")>=0)
        { 
              hideDiv(divisionNo.substring(16),'/samigo');  
          
        }
    }  
 }


function showHideDivs(showOrHide)
{  
    //alert("showHideDivs()");
	var divisionNo=""; 
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
      if(divisionNo.indexOf("__hide_division_")>=0)
        {  if(showOrHide=='show')
	    {
             showDiv(divisionNo.substring(16),'/samigo');
            }
            else
	    {
              hideDiv(divisionNo.substring(16),'/samigo');  
            }
        
        }
    }  
 }



function hideDiv(hideDivisionNo, context)
{
  //alert("hideDiv()");
  var tmpdiv = "__hide_division_" + hideDivisionNo;
  var tmpimg = "__img_hide_division_" + hideDivisionNo;
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);
  if(divisionNo)
  {
      divisionNo.style.display="none";
      if(imgNo)
      {
        imgNo.src = context + "/images/right_arrow.gif";
      }
   }
}

function showDiv(hideDivisionNo, context)
{
  var tmpdiv = "__hide_division_" + hideDivisionNo;
  var tmpimg = "__img_hide_division_" + hideDivisionNo;
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);
  if(divisionNo)
  { divisionNo.style.display="block";
    if(imgNo)
      {
       imgNo.src = context + "/images/down_arrow.gif";
      }
  }
}


//Huong's adding for use of gradeStudent Results
function toggleDiv(idNo)
{ 
  myDocumentElements=document.getElementsByTagName("span");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
       if (divisionNo!=idNo && divisionNo.indexOf(idNo.substring(0,idNo.length-1))>=0)
        { var eleDiv=getTheElement(divisionNo);
         var imgId=idNo+"Im";
          var imgEle=getTheElement(imgId);
            
           if(eleDiv.style.display=='none')
            { 
            eleDiv.style.display='block';
            if (imgEle)
               imgEle.src = "/samigo/images/down_arrow.gif";
            }
           else
            { 
             eleDiv.style.display='none';
             if (imgEle)
               imgEle.src = "/samigo/images/right_arrow.gif";
            }
        }
    }
     
}

function hideAll()
{ 
	//alert("hideAll()");
	myDocumentElements=document.getElementsByTagName("span");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
     if(divisionNo.indexOf("q")>=0)
      getTheElement(divisionNo).style.display="none";
}
}
