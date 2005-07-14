/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.gradebooktest;

import java.io.*;
import java.util.*;

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
		uidFound = getGradebookService().gradebookExists(uid);
		log.info("search uid=" + uid + ", uidFound=" + uidFound);
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


