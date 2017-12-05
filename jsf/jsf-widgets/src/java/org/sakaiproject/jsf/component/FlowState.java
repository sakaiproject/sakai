/**********************************************************************************
*
* $Id$
*
***********************************************************************************
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class FlowState extends UIComponentBase {
	public static final String COMPONENT_TYPE = "org.sakaiproject.jsf.FlowState";

	public FlowState() {
	}

	public PhaseAware getBean() {
		PhaseAware bean = null;
		ValueBinding vb = getValueBinding("bean");
		if (vb != null) {
			bean = (PhaseAware) vb.getValue(getFacesContext());
		}
		if (log.isDebugEnabled()) log.debug("getBean " + bean);
		return bean;
	}

	public Object saveState(FacesContext context) {
		PhaseAware bean = getBean();
		if (log.isDebugEnabled()) log.debug("saveState " + bean);
		Object values[] = new Object[2];
		values[0] = super.saveState(context);
		values[1] = bean;
		return ((Object)values);
	}

	public void restoreState(FacesContext context, Object state) {
		if (log.isDebugEnabled()) log.debug("restoreState " + state);
		Object values[] = (Object[])state;
		super.restoreState(context, values[0]);
		PhaseAware bean = (PhaseAware)values[1];
		ValueBinding vb = getValueBinding("bean");
		if (vb != null) {
			vb.setValue(context, bean);
		}
	}

	public void processRestoreState(FacesContext context, Object state) {
		if (log.isDebugEnabled()) log.debug("processRestoreState " + getBean());
		super.processRestoreState(context, state);
	}

	public void processDecodes(FacesContext context) {
		if (log.isDebugEnabled()) log.debug("processDecodes " + getBean());
		super.processDecodes(context);
	}

	public void processValidators(FacesContext context) {
		PhaseAware bean = getBean();
		if (log.isDebugEnabled()) log.debug("processValidators " + bean);
		super.processValidators(context);
		bean.endProcessValidators();
	}

	public void processUpdates(FacesContext context) {
		PhaseAware bean = getBean();
		if (log.isDebugEnabled()) log.debug("processUpdates " + bean);
		super.processUpdates(context);
		bean.endProcessUpdates();
	}

	public void encodeBegin(FacesContext context) throws IOException {
		PhaseAware bean = getBean();
		if (log.isDebugEnabled()) log.debug("encodeBegin " + bean);
		bean.startRenderResponse();
		super.encodeBegin(context);
	}

	public String getFamily() {
		if (log.isDebugEnabled()) log.debug("getFamily " + getBean());
		return "javax.faces.Data";
	}

}


