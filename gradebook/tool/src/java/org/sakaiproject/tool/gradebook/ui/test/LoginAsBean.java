/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.ui.InitializableBean;

public class LoginAsBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(LoginAsBean.class);

	private List loginChoices;

	public class WhoAndWhat {
		private String userUid;
		private String gradebookUid;
		private String role;
		private String entryPage;	// TODO Replace with entry servlet

		private WhoAndWhat(String userUid, String gradebookUid, String role) {
			this.userUid = userUid;
			this.gradebookUid = gradebookUid;
			this.role = role;
			if (role.equals("Student")) {
				entryPage = "studentView.jsf";
			} else {
				entryPage = "overview.jsf";
			}
		}

		public String getUserUid() {
			return userUid;
		}
		public String getGradebookUid() {
			return gradebookUid;
		}
		public String getRole() {
			return role;
		}
		public String getEntryPage() {
			return entryPage;
		}
	}

	public void init() {
		String[][] users = {{"authid_teacher", "Instructor"}, {"stu_0", "Student"}, {"authid_nowhere", "Nobody"}};
		String gradebookUid = "QA_6";
		try {
			gradebookUid = URLEncoder.encode(gradebookUid, "UTF-8");	// Since f:param won't do it
		} catch (UnsupportedEncodingException ex) {
			logger.error("Unlikely exception thrown", ex);
		}
		loginChoices = new ArrayList(users.length);
		for (int i = 0; i < users.length; i++) {
			String userUid = users[i][0];
			String role = users[i][1];
			loginChoices.add(new WhoAndWhat(userUid, gradebookUid, role));
		}
	}

	public List getLoginChoices() {
		return loginChoices;
	}
}



