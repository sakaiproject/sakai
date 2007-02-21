/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.summarycalendar.jsf.InitializableBean;
import org.sakaiproject.util.ResourceLoader;

public class PrefsBean extends InitializableBean implements Serializable {
	private static final long	serialVersionUID	= -6671159843904531584L;

	/** Our log (commons). */
	private static Log			LOG					= LogFactory.getLog(PrefsBean.class);

	/** Resource bundle */
	private transient ResourceLoader				msgs				= new ResourceLoader("org.sakaiproject.tool.summarycalendar.bundle.Messages");

	/** Bean members */

	/** Private members */
	private String				message				= null;

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("PrefsBean.init()");
		if(message != null){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
			message = null;
		}
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public String update() {
		message = msgs.getString("prefs_updated");
		return "prefs";
	}

	public String cancel() {
		message = null;
		return "calendar";
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
}
