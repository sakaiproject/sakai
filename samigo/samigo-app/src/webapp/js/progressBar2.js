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

var ID;
clearInterval(ID);

var totalseconds = (end - begin) / 1000;
var minutes = totalseconds / 60;
var hours = minutes / 60;
var days = hours / 24;
var month = end.getMonth() + 1;
var year = end.getYear();
var day = end.getDay();

if (totalseconds > 0)
{
  start_countdown();
  progressBarInit(totalseconds);
  ID=window.setTimeout("update();", 1000);
}