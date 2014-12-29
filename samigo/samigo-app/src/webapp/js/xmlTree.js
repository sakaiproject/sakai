/**********************************************************************************
* $HeadURL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/jsf/component/RichTextEditArea.java $
* $Id: RichTextEditArea.java 226 2005-06-23 23:46:26Z esmiley@stanford.edu $
***********************************************************************************
*
 * Copyright (c) 2005 The Sakai Foundation
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

var openImg = new Image();
   openImg.src = "../../images/tree/open.gif";
   var closedImg = new Image();
   closedImg.src = "../../images/tree/closed.gif";

   function showBranch(branch){
      var objBranch =
         document.getElementById(branch).style;
      if(objBranch.display=="none")
         objBranch.display="block";
      else
         objBranch.display="none";
      swapFolder('I' + branch);
   }

   function swapFolder(img){
      objImg = document.getElementById(img);
      if(objImg.src.indexOf('open.gif')>-1)
         objImg.src = closedImg.src;
      else
         objImg.src = openImg.src;
   }
