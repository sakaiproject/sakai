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