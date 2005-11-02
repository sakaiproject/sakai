/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California and The Regents of the University of Michigan
*
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

package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;

import org.sakaiproject.api.section.coursemanagement.User;

public class UserImpl implements User, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String userUid;
	private String displayId;
	private String sortName;
	private String displayName;
	
	public UserImpl(String displayId, String displayName, String sortName, String uid) {
		this.displayId = displayId;
		this.displayName = displayName;
		this.sortName = sortName;
		this.userUid = uid;
	}

	public String getUserUid() {
		return userUid;
	}

	public String getSortName() {
		return sortName;
	}

	public String getDisplayId() {
		return displayId;
	}

	public String getDisplayName() {
		return displayName;
	}
}
