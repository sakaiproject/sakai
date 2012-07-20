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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

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
