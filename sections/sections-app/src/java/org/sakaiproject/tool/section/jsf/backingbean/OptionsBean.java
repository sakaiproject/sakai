/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.tool.section.jsf.JsfUtil;

public class OptionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(OptionsBean.class);
	
	private boolean selfRegister;
	private boolean selfSwitch;
	
	public void init() {
		Course course = getCourse();
		selfRegister = course.isSelfRegistrationAllowed();
		selfSwitch = course.isSelfSwitchingAllowed();
	}

	public String update() {
		Course course = getCourse();
		getSectionManager().setSelfRegistrationAllowed(course.getUuid(), selfRegister);
		getSectionManager().setSelfSwitchingAllowed(course.getUuid(), selfSwitch);
		
		JsfUtil.addRedirectSafeMessage(JsfUtil.getLocalizedMessage("options_update_successful"));
		return "overview";
	}
	
	public boolean isSelfRegister() {
		return selfRegister;
	}

	public void setSelfRegister(boolean selfRegister) {
		this.selfRegister = selfRegister;
	}

	public boolean isSelfSwitch() {
		return selfSwitch;
	}

	public void setSelfSwitch(boolean selfSwitch) {
		this.selfSwitch = selfSwitch;
	}
	
	

}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
