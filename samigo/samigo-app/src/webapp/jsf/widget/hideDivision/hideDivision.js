/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// does "hide" (hideUnhideAllDivs()) need to be run?
var runHide=true;
// should function be disabled?
var wysiwygShowHideDiv=false;

// show/hide hideDivision tag with JSF id of "hideDivisionNo" with "context" path
function showHideDiv(hideDivisionNo, context)
{
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
 */
function FCKeditor_OnComplete( editorInstance )
{
   if (navigator.product == "Gecko")
   { 
      hideDivs();
   } 
}


var exceptionId = "";
function setExceptionId(thisExceptionIdValue)
{
  exceptionId = thisExceptionIdValue;
}
function hideUnhideAllDivsExceptOne(action)
{
  wysiwygShowHideDiv = true;

  var exceptionFullId = "__hide_division_" + exceptionId;

  if(runHide==true)
  {
    runHide=false;
    myDocumentElements=document.getElementsByTagName("div");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
      if((divisionNo != null) && (divisionNo== exceptionFullId))
      {
        continue;
      }
      if (divisionNo.indexOf("__hide_division_")==0)
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
{  var divisionNo=""; 
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
{  var divisionNo=""; 
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
{  var divisionNo=""; 
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
{ myDocumentElements=document.getElementsByTagName("span");
    for (i=0;i<myDocumentElements.length;i++)
    {
      divisionNo = "" + myDocumentElements[i].id;
     if(divisionNo.indexOf("q")>=0)
      getTheElement(divisionNo).style.display="none";
}
}
