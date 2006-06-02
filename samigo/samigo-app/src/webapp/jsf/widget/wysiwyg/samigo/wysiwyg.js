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

var item =0;
var ta_editor =  [];
var hidden = [];
var textAreaNames = [];
var runFocus=true;


function initEditors(contextPath)
{
//alert("disabled");
return; // debug

//    alert("begin.");
    myDocumentElements=document.getElementsByTagName("textarea");
    for (i=0;i<myDocumentElements.length;i++)
    {
      name = myDocumentElements[i].name;
//			alert(name);
      if (name.indexOf("nowysiwyg") != -1)
      {
        continue; // any name attribute containing 'nowysiwyg' will be bypassed
      }
      textAreaNames[i] = name;
      editor =
        initEditorById(name, contextPath + "/htmlarea/", "three", true);
      ta_editor[i] = editor;
//      alert(name)
      wait(100);
      hideUnhide(name);
      wait(100);
      hideUnhide(name);
//      alert(editor._textArea.value);

//      alert("added " + name);
    }

    for (i=textAreaNames.length; i>0; name=textAreaNames[--i])
    {
      wait(200);
      setFocus(name);
      wait(100);
      break;
    }

}

function wait(delay)
{
     string="noop("+delay+");";
     setTimeout(string,delay);
}

function noop(delay)
{
//     alert("Ok "+delay/1000+" seconds have elapsed");
}

function setFocus(ta)
{
   var element = document.getElementById(ta);
   element.focus();
   return false;
}
