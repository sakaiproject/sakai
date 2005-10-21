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

package org.sakaiproject.tool.gradebook.jsf.iterator;

import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.NamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

/**
 * This is a bit like UIData, except that it doesn't put any constraints on which
 * children it processes. (UIData renderers ignore everything but UIColumn children.)
 * <gb:iterator value="#{assignmentDetailsBean.scoreRows}" var="scoreRow">
 *    ...
 * </gb:iterator>
 */

public class IteratorComponent extends UIComponentBase implements NamingContainer {
	private static final Log log = LogFactory.getLog(IteratorComponent.class);

	public final static String COMPONENT_TYPE = "org.sakaiproject.tool.gradebook.jsf.iterator";
	public static final String COMPONENT_FAMILY = "javax.faces.Data";

	private Object value = null;
	private String var = null;

	public void encodeChildren(FacesContext context) throws IOException {
		Collection dataModel = getDataModel();
		if (log.isDebugEnabled()) log.debug("encodeChildren: dataModel=" + dataModel);
		if (dataModel != null) {
			Map requestMap = context.getExternalContext().getRequestMap();
			ResponseWriter writer = context.getResponseWriter();
			writer.write("<blockquote>");
			for (Iterator iter = dataModel.iterator(); iter.hasNext(); ) {
				Object varObject = iter.next();
				if (var != null) {
					if (varObject != null) {
						requestMap.put(var, varObject);
					} else {
						requestMap.remove(var);
					}
				}
				encodeRecursive(context, getChildren());
			}
			requestMap.remove(var);
			writer.write("</blockquote>");
			writer.flush();
		}
	}

	private void encodeRecursive(FacesContext context, List components) throws IOException {
		for (Iterator iter = components.iterator(); iter.hasNext(); ) {
			UIComponent component = (UIComponent)iter.next();
			if (component.isRendered()) {
				component.encodeBegin(context);
				if (component.getRendersChildren()) {
					component.encodeChildren(context);
				} else {
					encodeRecursive(context, component.getChildren());
				}
				component.encodeEnd(context);
			}
		}
	}

	private Collection getDataModel() {
		Collection dataModel = null;
		Object val = getValue();
		if (val != null) {
			if (val instanceof Collection) {
				dataModel = (Collection)val;
			} else {
				if (log.isDebugEnabled()) log.debug("value is not a Collection: " + val);
			}
		}
		return dataModel;
	}

	public void encodeBegin(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) log.debug("encodeBegin");
	}
	public void encodeEnd(FacesContext context) throws IOException {
		if (log.isDebugEnabled()) log.debug("encodeEnd");
		return;
	}

	public void decode(FacesContext context) {
		return;
	}

	public boolean getRendersChildren() {
		return true;
	}

	public void setValueBinding(String name, ValueBinding binding) {
		if ("var".equals(name)) {
			throw new IllegalArgumentException();
		}
		super.setValueBinding(name, binding);
	}


	public Object getValue() {
		if (log.isDebugEnabled()) log.debug("getValue: value=" + value + ", valueBinding=" + getValueBinding("value"));
		Object retVal = null;
		if (value != null) {
			retVal = value;
		} else {
			ValueBinding binding = getValueBinding("value");
			if (binding != null) {
				retVal = binding.getValue(getFacesContext());
			}
		}
		return retVal;
	}
	public void setValue(Object value) {
		if (log.isDebugEnabled()) log.debug("setValue " + value);
		this.value = value;
	}
	public String getVar() {
		if (log.isDebugEnabled()) log.debug("getVar: var=" + var);
		return var;
	}
	public void setVar(String var) {
		if (log.isDebugEnabled()) log.debug("setVar " + var);
		this.var = var;
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[3];
		values[0] = super.saveState(context);
		values[1] = value;
		values[2] = var;
		return values;
	}
	public void restoreState(FacesContext context, Object state) {
		if (log.isDebugEnabled()) log.debug("restoreState " + state);
		Object values[] = (Object[])state;
		super.restoreState(context, values[0]);
		value = values[1];
		var = (String)values[2];
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

}
