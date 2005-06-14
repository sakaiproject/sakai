// $Id: hideDivision.js,v 1.19 2005/06/09 23:59:26 esmiley.stanford.edu Exp $

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
 if (document.htmlareas == undefined)
 {
  // alert("htmlareas is undefined");
  return;
 }

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
