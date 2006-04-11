/**********************************************************************************
* $URL$
* $Id$
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

// show/hide hideDivision tag with JSF id of "hideDivisionNo" with "context" path
function showHideDiv(hideDivisionNo, context)
{
  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);
  if(divisionNo)
  {
  if(divisionNo.style.display =="block")
  {
    divisionNo.style.display="none";
    if (imgNo)
    {
      imgNo.src = context + "/hideDivision/images/right_arrow.gif";
     }
  }
  else
    {
     divisionNo.style.display="block";
     if(imgNo)
     {
     imgNo.src = context + "/hideDivision/images/down_arrow.gif";
     }
    }
  }

  if (navigator.product == "Gecko")
  {
    for (i=0; i<2; i++) resetWysiwygMode();
  }
}

// if there are _any_ wysiwyg editors present toggle into wysiwyg mode
// for all wysiwygs; needed for Gecko based browsers
// future enhancement, toggle only those where the id (document.htmlareas[i][0])
// is in the division and reset focus to the appropriate(??) object
function resetWysiwygMode()
{

  if (document.htmlareas == undefined)
   {
//   document.write("htmlareas is undefined");
   return;
   }

  var counter = document.htmlareas.length;

  for (i=0; i<counter; i++)
  {
  editor = document.htmlareas[i][1];
  editor.setMode("textmode");
  editor.setMode("wysiwyg");
  }
}

// getElementById with special handling of old browsers
function getTheElement(thisid)
{

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
