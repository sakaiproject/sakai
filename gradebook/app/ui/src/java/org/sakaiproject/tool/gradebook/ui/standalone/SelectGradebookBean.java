/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.ui.standalone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.ui.InitializableBean;

@Slf4j
public class SelectGradebookBean extends InitializableBean {
	private Authn authnService;
	private FrameworkManager frameworkManager;

	/**
	 * Return a list of gradebooks accessible by the currently logged-in user.
	 */
	public List getGradebooks() {
		String userUid = authnService.getUserUid();
		List gradebooks = frameworkManager.getAccessibleGradebooks(userUid);

		// JSF's "f:param" doesn't java.net.URLEncoder.encode the
		// parameter value for us. If it did, we would just return the gradebooks
		// list straight.
		List returnList = new ArrayList(gradebooks.size());
		for (Iterator iter = gradebooks.iterator(); iter.hasNext(); ) {
			Gradebook gradebook = (Gradebook)iter.next();
			returnList.add(new GradebookRow(gradebook));
		}

		return returnList;
	}
	public class GradebookRow {
		private String name;
		private String uid;
		public GradebookRow() {
		}
		public GradebookRow(Gradebook gradebook) {
			this.name = gradebook.getName();
			try {
				this.uid = URLEncoder.encode(gradebook.getUid(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				log.error("Unlikely exception thrown", ex);
			}
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
	}

	public Authn getAuthnService() {
		return authnService;
	}
	public void setAuthnService(Authn authnService) {
		this.authnService = authnService;
	}

	public FrameworkManager getFrameworkManager() {
		return frameworkManager;
	}
	public void setFrameworkManager(FrameworkManager frameworkManager) {
		this.frameworkManager = frameworkManager;
	}
}
