/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HierPvtMsgDataTableTag.java $
 * $Id: HierPvtMsgDataTableTag.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.jsf;

import java.io.IOException;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.jsp.JspException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
@Slf4j
public class HierPvtMsgDataTableTag extends UIComponentTag 
{

	//
	// Instance Variables
	//
	
	@Setter
	private java.lang.String first;
	@Setter
	private java.lang.String rows;
	@Setter
	private java.lang.String value;
	@Setter
	private java.lang.String var;
	@Setter
	private java.lang.String bgcolor;
	@Setter
	private java.lang.String border;
	@Setter
	private java.lang.String cellpadding;
	@Setter
	private java.lang.String cellspacing;
	@Setter
	private java.lang.String columnClasses;
	@Setter
	private java.lang.String dir;
	@Setter
	private java.lang.String footerClass;
	@Setter
	private java.lang.String frame;
	@Setter
	private java.lang.String headerClass;
	@Setter
	private java.lang.String lang;
	@Setter
	private java.lang.String onclick;
	@Setter
	private java.lang.String ondblclick;
	@Setter
	private java.lang.String onkeydown;
	@Setter
	private java.lang.String onkeypress;
	@Setter
	private java.lang.String onkeyup;
	@Setter
	private java.lang.String onmousedown;
	@Setter
	private java.lang.String onmousemove;
	@Setter
	private java.lang.String onmouseout;
	@Setter
	private java.lang.String onmouseover;
	@Setter
	private java.lang.String onmouseup;
	@Setter
	private java.lang.String rowClasses;
	@Setter
	private java.lang.String rules;
	@Setter
	private java.lang.String style;
	@Setter
	private java.lang.String styleClass;
	@Setter
	private java.lang.String summary;
	@Setter
	private java.lang.String title;
	@Setter
	private java.lang.String width;
	@Setter
	private java.lang.String expanded;

	
	//
	// General Methods
	//
	
	public String getRendererType() { return "HierPvtMsgDataTableRender"; }
	public String getComponentType() { return "javax.faces.HtmlDataTable"; }
	
	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		UIData data = null;
		try {
			data = (UIData)component;
		}
		catch (ClassCastException cce) {
			throw new IllegalStateException("Component " + component.toString() + " not expected type.  Expected: UIData.  Perhaps you're missing a tag?");
		}

		Application application = FacesContext.getCurrentInstance().getApplication();

		if (first != null) {
			if (isValueReference(first)) {
				ValueBinding vb = application.createValueBinding(first);
				data.setValueBinding("first", vb);
			} else {
				int _first = Integer.valueOf(first).intValue();
				data.setFirst(_first);
			}
		}
		if (rows != null) {
			if (isValueReference(rows)) {
				ValueBinding vb = application.createValueBinding(rows);
				data.setValueBinding("rows", vb);
			} else {
				int _rows = Integer.valueOf(rows).intValue();
				data.setRows(_rows);
			}
		}
		if (value != null) {
			if (isValueReference(value)) {
				ValueBinding vb = application.createValueBinding(value);
				data.setValueBinding("value", vb);
			} else {
				data.setValue(value);
			}
		}
		data.setVar(var);
		if (bgcolor != null) {
			if (isValueReference(bgcolor)) {
				ValueBinding vb = application.createValueBinding(bgcolor);
				data.setValueBinding("bgcolor", vb);
			} else {
				data.getAttributes().put("bgcolor", bgcolor);
			}
		}
		if (border != null) {
			if (isValueReference(border)) {
				ValueBinding vb = application.createValueBinding(border);
				data.setValueBinding("border", vb);
			} else {
				int _border = Integer.valueOf(border).intValue();
				if (_border != Integer.MIN_VALUE) {
					data.getAttributes().put("border", Integer.valueOf(_border));
				}
			}
		}
		if (cellpadding != null) {
			if (isValueReference(cellpadding)) {
				ValueBinding vb = application.createValueBinding(cellpadding);
				data.setValueBinding("cellpadding", vb);
			} else {
				data.getAttributes().put("cellpadding", cellpadding);
			}
		}
		if (cellspacing != null) {
			if (isValueReference(cellspacing)) {
				ValueBinding vb = application.createValueBinding(cellspacing);
				data.setValueBinding("cellspacing", vb);
			} else {
				data.getAttributes().put("cellspacing", cellspacing);
			}
		}
		if (columnClasses != null) {
			if (isValueReference(columnClasses)) {
				ValueBinding vb = application.createValueBinding(columnClasses);
				data.setValueBinding("columnClasses", vb);
			} else {
				data.getAttributes().put("columnClasses", columnClasses);
			}
		}
		if (dir != null) {
			if (isValueReference(dir)) {
				ValueBinding vb = application.createValueBinding(dir);
				data.setValueBinding("dir", vb);
			} else {
				data.getAttributes().put("dir", dir);
			}
		}
		if (footerClass != null) {
			if (isValueReference(footerClass)) {
				ValueBinding vb = application.createValueBinding(footerClass);
				data.setValueBinding("footerClass", vb);
			} else {
				data.getAttributes().put("footerClass", footerClass);
			}
		}
		if (frame != null) {
			if (isValueReference(frame)) {
				ValueBinding vb = application.createValueBinding(frame);
				data.setValueBinding("frame", vb);
			} else {
				data.getAttributes().put("frame", frame);
			}
		}
		if (headerClass != null) {
			if (isValueReference(headerClass)) {
				ValueBinding vb = application.createValueBinding(headerClass);
				data.setValueBinding("headerClass", vb);
			} else {
				data.getAttributes().put("headerClass", headerClass);
			}
		}
		if (lang != null) {
			if (isValueReference(lang)) {
				ValueBinding vb = application.createValueBinding(lang);
				data.setValueBinding("lang", vb);
			} else {
				data.getAttributes().put("lang", lang);
			}
		}
		if (onclick != null) {
			if (isValueReference(onclick)) {
				ValueBinding vb = application.createValueBinding(onclick);
				data.setValueBinding("onclick", vb);
			} else {
				data.getAttributes().put("onclick", onclick);
			}
		}
		if (ondblclick != null) {
			if (isValueReference(ondblclick)) {
				ValueBinding vb = application.createValueBinding(ondblclick);
				data.setValueBinding("ondblclick", vb);
			} else {
				data.getAttributes().put("ondblclick", ondblclick);
			}
		}
		if (onkeydown != null) {
			if (isValueReference(onkeydown)) {
				ValueBinding vb = application.createValueBinding(onkeydown);
				data.setValueBinding("onkeydown", vb);
			} else {
				data.getAttributes().put("onkeydown", onkeydown);
			}
		}
		if (onkeypress != null) {
			if (isValueReference(onkeypress)) {
				ValueBinding vb = application.createValueBinding(onkeypress);
				data.setValueBinding("onkeypress", vb);
			} else {
				data.getAttributes().put("onkeypress", onkeypress);
			}
		}
		if (onkeyup != null) {
			if (isValueReference(onkeyup)) {
				ValueBinding vb = application.createValueBinding(onkeyup);
				data.setValueBinding("onkeyup", vb);
			} else {
				data.getAttributes().put("onkeyup", onkeyup);
			}
		}
		if (onmousedown != null) {
			if (isValueReference(onmousedown)) {
				ValueBinding vb = application.createValueBinding(onmousedown);
				data.setValueBinding("onmousedown", vb);
			} else {
				data.getAttributes().put("onmousedown", onmousedown);
			}
		}
		if (onmousemove != null) {
			if (isValueReference(onmousemove)) {
				ValueBinding vb = application.createValueBinding(onmousemove);
				data.setValueBinding("onmousemove", vb);
			} else {
				data.getAttributes().put("onmousemove", onmousemove);
			}
		}
		if (onmouseout != null) {
			if (isValueReference(onmouseout)) {
				ValueBinding vb = application.createValueBinding(onmouseout);
				data.setValueBinding("onmouseout", vb);
			} else {
				data.getAttributes().put("onmouseout", onmouseout);
			}
		}
		if (onmouseover != null) {
			if (isValueReference(onmouseover)) {
				ValueBinding vb = application.createValueBinding(onmouseover);
				data.setValueBinding("onmouseover", vb);
			} else {
				data.getAttributes().put("onmouseover", onmouseover);
			}
		}
		if (onmouseup != null) {
			if (isValueReference(onmouseup)) {
				ValueBinding vb = application.createValueBinding(onmouseup);
				data.setValueBinding("onmouseup", vb);
			} else {
				data.getAttributes().put("onmouseup", onmouseup);
			}
		}
		if (rowClasses != null) {
			if (isValueReference(rowClasses)) {
				ValueBinding vb = application.createValueBinding(rowClasses);
				data.setValueBinding("rowClasses", vb);
			} else {
				data.getAttributes().put("rowClasses", rowClasses);
			}
		}
		if (rules != null) {
			if (isValueReference(rules)) {
				ValueBinding vb = application.createValueBinding(rules);
				data.setValueBinding("rules", vb);
			} else {
				data.getAttributes().put("rules", rules);
			}
		}
		if (style != null) {
			if (isValueReference(style)) {
				ValueBinding vb = application.createValueBinding(style);
				data.setValueBinding("style", vb);
			} else {
				data.getAttributes().put("style", style);
			}
		}
		if (styleClass != null) {
			if (isValueReference(styleClass)) {
				ValueBinding vb = application.createValueBinding(styleClass);
				data.setValueBinding("styleClass", vb);
			} else {
				data.getAttributes().put("styleClass", styleClass);
			}
		}
		if (summary != null) {
			if (isValueReference(summary)) {
				ValueBinding vb = application.createValueBinding(summary);
				data.setValueBinding("summary", vb);
			} else {
				data.getAttributes().put("summary", summary);
			}
		}
		if (title != null) {
			if (isValueReference(title)) {
				ValueBinding vb = application.createValueBinding(title);
				data.setValueBinding("title", vb);
			} else {
				data.getAttributes().put("title", title);
			}
		}
		if (width != null) {
			if (isValueReference(width)) {
				ValueBinding vb = application.createValueBinding(width);
				data.setValueBinding("width", vb);
			} else {
				data.getAttributes().put("width", width);
			}
		}
		if (expanded != null) {
			if (isValueReference(expanded)) {
				ValueBinding vb = application.createValueBinding(expanded);
				data.setValueBinding("expanded", vb);
			} else {
				data.getAttributes().put("expanded", expanded);
			}
		}
	}
	
	//
	// Methods From TagSupport
	//
	
	public int doStartTag() throws JspException {
		int rc = 0;
		try {
			rc = super.doStartTag();
		} catch (JspException e) {
			if (log.isDebugEnabled()) {
				log.debug(getDebugString(), e);
			}
			throw e;
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug(getDebugString(), t);
			}
			throw new JspException(t);
		}
		return rc;
	}
	
	public int doEndTag() throws JspException {
		int rc = 0;
		try {
			rc = super.doEndTag();
		} catch (JspException e) {
			if (log.isDebugEnabled()) {
				log.debug(getDebugString(), e);
			}
			throw e;
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug(getDebugString(), t);
			}
			throw new JspException(t);
		}
		return rc;
	}
	
	public String getDebugString() {
		String result = "id: "+this.getId()+" class: "+this.getClass().getName();
		return result;
	}
}
