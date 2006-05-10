/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebooktest;

import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradebookService;

public class GradebookBean {
	private static final Log log = LogFactory.getLog(GradebookBean.class);

	private String uid;
	private boolean uidFound;
	private GradebookService gradebookService;

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public boolean isUidFound() {
		return uidFound;
	}

	public void search(ActionEvent event) {
		uidFound = getGradebookService().isGradebookDefined(uid);
		log.info("search uid=" + uid + ", uidFound=" + uidFound);
	}

	public void create(ActionEvent event) {
		getGradebookService().addGradebook(uid, "Gradebook " + uid);
		log.info("created Gradebook with uid=" + uid);
	}

	public GradebookService getGradebookService() {
		log.info("getGradebookService " + gradebookService);
		return gradebookService;
	}
	public void setGradebookService(GradebookService gradebookService) {
		log.info("setGradebookService " + gradebookService);
		this.gradebookService = gradebookService;
	}

}


