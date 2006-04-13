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

package org.sakaiproject.tool.gradebook.ui.standalone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.standalone.FrameworkManager;
import org.sakaiproject.tool.gradebook.ui.InitializableBean;

public class SelectGradebookBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(SelectGradebookBean.class);
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
				logger.error("Unlikely exception thrown", ex);
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
