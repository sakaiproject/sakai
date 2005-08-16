/**********************************************************************************
*
* $Id: InitializableBean.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
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

package org.sakaiproject.tool.section.jsf.backingbean;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.model.PhaseAware;

public abstract class InitializableBean implements PhaseAware {
	private static final Log logger = LogFactory.getLog(InitializableBean.class);

	protected ResourceBundle messageBundle;
	
	private transient boolean notValidated;

	/**
	 * JSF doesn't provide a way to configure an initialization method which will
	 * be called after the contructor and all framework setters. By convention,
	 * our backing beans use this method. It's triggered either by a Faces configuration
	 * file setting "configured" to true, or by a JSF component directly calling "startRenderResponse".
	 *
	 * For greater subclassing flexibility, the init method is not declared to be
	 * abstract.
	 */
	protected void init() {
        String bundleName = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
        messageBundle =ResourceBundle.getBundle(bundleName);
	}

	/**
	 * Remember if JSF entered the Validations phase. If so, and if we never
	 * reach the Update Model Values phase, then validation failed. That may
	 * be of interest to the backing bean. For example, the backing bean
	 * may choose not to requery and reload data on a validation error.
	 */
	public void endProcessValidators() {
		setNotValidated(true);
		if (logger.isDebugEnabled()) logger.debug("endProcessValidators");
	}

	public void endProcessUpdates() {
		setNotValidated(false);
		if (logger.isDebugEnabled()) logger.debug("endProcessUpdates");
	}

	/**
	 * Call init() at the beginning of every request rendering.
	 * (This should also work to refresh session-scoped beans, but it's
	 * only been tested with request scope.)
	 */
	public void startRenderResponse() {
		if (logger.isDebugEnabled()) logger.debug("startRenderResponse notValidated=" + isNotValidated());
		init();
	}

	public boolean isNotValidated() {
		return notValidated;
	}
	public void setNotValidated(boolean notValidated) {
		this.notValidated = notValidated;
	}

	/**
	 * Signals that configuration is finished.
	 */
	public void setConfigured(boolean isConfigured) {
		if (logger.isDebugEnabled()) logger.debug("setConfigured " + isConfigured);
		if (isConfigured) {
			init();
		}
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id: InitializableBean.java 634 2005-07-14 23:54:16Z jholtzman@berkeley.edu $
 *************************************************************************************************************************************************************************************************************************************************************/
