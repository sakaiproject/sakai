/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006 Sakai Foundation, the MIT Corporation
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
		String[][] users = {{"authid_teacher", "Instructor"}, {"stu_0", "Student"}, {"authid_ta", "TA"}, {"authid_nowhere", "Nobody"}};
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



