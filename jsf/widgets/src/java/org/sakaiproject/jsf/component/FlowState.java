/**********************************************************************************
*
* $Id$
*
***********************************************************************************
 * Copyright (c) 2006 Sakai Foundation
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
**********************************************************************************/

package org.sakaiproject.jsf.component;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.model.PhaseAware;

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
 * &lt;sakaix:flowState bean="#{phaseAwareBean}" /&gt
 *
 * should be placed in the JSP file before any other bean references are made.
 *
 * <p>
 * The bean must implement the PhaseAware interface and be serializable.
 * Any non-transient fields in the bean will be saved and restored from this component.
 */
public class FlowState extends UIComponentBase {
	private static final Log logger = LogFactory.getLog(FlowState.class);

	public static final String COMPONENT_TYPE = "org.sakaiproject.jsf.FlowState";

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


