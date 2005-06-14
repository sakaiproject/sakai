/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/jsf/flowstate/FlowState.java,v 1.3 2005/05/26 18:04:56 josh.media.berkeley.edu Exp $
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

package org.sakaiproject.tool.gradebook.jsf.flowstate;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This JSF UI component lets backing beans track the request life cycle and
 * save their state within the component tree itself. (These two aspects could
 * be separated with a "saveState" Boolean attribute if we ever need to let
 * a session-scoped bean track the request life cycle.)
 *
 * Like the MyFaces x:saveState tag, this passes the bean's state from request
 * to request without use of session scope. That in turn enables request-thread
 * functionality such as "what if?" scenarios and multiple active
 * application views from a single session.
 *
 * <p>
 * Usage:
 *
 * &lt;gbx:flowState bean="#{phaseAwareBean}" /&gt
 *
 * should be placed in the JSP file before any other bean references are made.
 *
 * <p>
 * The bean must implement the PhaseAware interface and be serializable.
 * Any non-transient fields in the bean will be saved and restored from this component.
 */
public class FlowState extends UIComponentBase {
	private static final Log logger = LogFactory.getLog(FlowState.class);

	public static final String COMPONENT_TYPE = "org.sakaiproject.tool.gradebook.FlowState";

	private PhaseAware _bean;

	public FlowState() {
	}

	public void setBean(PhaseAware bean) {
		if (logger.isDebugEnabled()) logger.debug("setBean " + bean);
		_bean = bean;
	}
	public PhaseAware getBean() {
		if (logger.isDebugEnabled()) logger.debug("getBean " + _bean);
		if (_bean != null) return _bean;

		PhaseAware returnObject = null;
		ValueBinding vb = getValueBinding("bean");
		if (vb != null) {
			returnObject = (PhaseAware)vb.getValue(getFacesContext());
		}
		if (logger.isDebugEnabled()) logger.debug("  returning " + returnObject);
		return returnObject;
	}

	public Object saveState(FacesContext context) {
		if (logger.isDebugEnabled()) logger.debug("saveState " + _bean);
		Object values[] = new Object[2];
		values[0] = super.saveState(context);
		values[1] = getBean();
		return ((Object)values);
	}

	public void restoreState(FacesContext context, Object state) {
		if (logger.isDebugEnabled()) logger.debug("restoreState " + state);
		Object values[] = (Object[])state;
		super.restoreState(context, values[0]);
		_bean = (PhaseAware)values[1];
		ValueBinding vb = getValueBinding("bean");
		if (vb != null) {
			vb.setValue(context, _bean);
		}
	}

	public void processRestoreState(FacesContext context, Object state) {
		if (logger.isDebugEnabled()) logger.debug("processRestoreState " + _bean);
		super.processRestoreState(context, state);
	}

	public void processDecodes(FacesContext context) {
		if (logger.isDebugEnabled()) logger.debug("processDecodes " + _bean);
		super.processDecodes(context);
	}

	public void processValidators(FacesContext context) {
		if (logger.isDebugEnabled()) logger.debug("processValidators " + _bean);
		super.processValidators(context);
		if (_bean != null) {
			_bean.endProcessValidators();
		}
	}

	public void processUpdates(FacesContext context) {
		if (logger.isDebugEnabled()) logger.debug("processUpdates " + _bean);
		super.processUpdates(context);
		if (_bean != null) {
			_bean.endProcessUpdates();
		}
	}

	public void encodeBegin(FacesContext context) throws IOException {
		PhaseAware bean = getBean();
		if (logger.isDebugEnabled()) logger.debug("  getBean=" + bean);
		if (bean != null) {
			bean.startRenderResponse();
		}
		super.encodeBegin(context);
	}

	public String getFamily() {
		if (logger.isDebugEnabled()) logger.debug("getFamily " + _bean);
		return "javax.faces.Data";
	}

}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/java/org/sakaiproject/tool/gradebook/jsf/flowstate/FlowState.java,v 1.3 2005/05/26 18:04:56 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
