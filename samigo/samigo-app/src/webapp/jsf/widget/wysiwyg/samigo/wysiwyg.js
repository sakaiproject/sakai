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
