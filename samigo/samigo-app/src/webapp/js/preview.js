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
//Returns String format of ISO date
function parseISODate(parameterDate)
	{	var duration="";
		pattern =  /^P((\d*)Y)?((\d*)M)?((\d*)W)?((\d*)D)?T?((\d*)H)?((\d*)M)?((\d*)S)?/;
		//	var m = pattern.exec("P2Y3M4W5DT1H4M10S");
		var m = pattern.exec(parameterDate);
		if (m!=null)
		{
		if (m[2] != ""){duration = m[2]+ " Year(s) ";	}
		if (m[4] != ""){duration = duration + m[4]+ " Month(s) ";}
		if (m[6] != ""){duration = duration + m[6]+ " Week(s) ";}
		if (m[8] != ""){duration = duration + m[8]+ " Day(s) ";	}
		if (m[10] != ""){duration = duration + m[10]+ " Hour(s) ";}
		if (m[12] != ""){duration = duration + m[12]+ " Minute(s) ";}
		if (m[14] != ""){duration = duration + m[14]+ " Second(s) ";}
		}

		return duration;
    }