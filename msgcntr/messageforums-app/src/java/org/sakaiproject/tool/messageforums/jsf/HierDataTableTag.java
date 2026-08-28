/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HierDataTableTag.java $
 * $Id: HierDataTableTag.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.UIComponentELTag;
import jakarta.servlet.jsp.JspException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
@Slf4j
public class HierDataTableTag extends UIComponentELTag
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
	@Setter
	private java.lang.String noarrows;

	
	//
	// General Methods
	//
	
	public String getRendererType() { return "HierDataTableRender"; }
	public String getComponentType() { return "jakarta.faces.HtmlDataTable"; }
	
	private static boolean isEL(String val) {
		return val.startsWith("#{") && val.endsWith("}");
	}
	
	private void setAttr(UIComponent component, String attr, String val) {
		if (val == null) return;
		if (isEL(val)) {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ValueExpression ve = ctx.getApplication().getExpressionFactory().createValueExpression(ctx.getELContext(), val, Object.class);
			component.setValueExpression(attr, ve);
		} else {
			component.getAttributes().put(attr, val);
		}
	}
	
	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		UIData data = null;
		try {
			data = (UIData)component;
		}
		catch (ClassCastException cce) {
			throw new IllegalStateException("Component " + component.toString() + " not expected type.  Expected: UIData.  Perhaps you're missing a tag?");
		}

		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();

		if (first != null) {
			if (isEL(first)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), first, Object.class);
				data.setValueExpression("first", ve);
			} else {
				int _first = Integer.valueOf(first).intValue();
				data.setFirst(_first);
			}
		}
		if (rows != null) {
			if (isEL(rows)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), rows, Object.class);
				data.setValueExpression("rows", ve);
			} else {
				int _rows = Integer.valueOf(rows).intValue();
				data.setRows(_rows);
			}
		}
		if (value != null) {
			if (isEL(value)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), value, Object.class);
				data.setValueExpression("value", ve);
			} else {
				data.setValue(value);
			}
		}
		data.setVar(var);
		if (bgcolor != null) {
			if (isEL(bgcolor)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), bgcolor, Object.class);
				data.setValueExpression("bgcolor", ve);
			} else {
				data.getAttributes().put("bgcolor", bgcolor);
			}
		}
		if (border != null) {
			if (isEL(border)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), border, Object.class);
				data.setValueExpression("border", ve);
			} else {
				int _border = Integer.valueOf(border).intValue();
				if (_border != Integer.MIN_VALUE) {
					data.getAttributes().put("border", Integer.valueOf(_border));
				}
			}
		}
		if (cellpadding != null) {
			if (isEL(cellpadding)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), cellpadding, Object.class);
				data.setValueExpression("cellpadding", ve);
			} else {
				data.getAttributes().put("cellpadding", cellpadding);
			}
		}
		if (cellspacing != null) {
			if (isEL(cellspacing)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), cellspacing, Object.class);
				data.setValueExpression("cellspacing", ve);
			} else {
				data.getAttributes().put("cellspacing", cellspacing);
			}
		}
		if (columnClasses != null) {
			if (isEL(columnClasses)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), columnClasses, Object.class);
				data.setValueExpression("columnClasses", ve);
			} else {
				data.getAttributes().put("columnClasses", columnClasses);
			}
		}
		if (dir != null) {
			if (isEL(dir)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), dir, Object.class);
				data.setValueExpression("dir", ve);
			} else {
				data.getAttributes().put("dir", dir);
			}
		}
		if (footerClass != null) {
			if (isEL(footerClass)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), footerClass, Object.class);
				data.setValueExpression("footerClass", ve);
			} else {
				data.getAttributes().put("footerClass", footerClass);
			}
		}
		if (frame != null) {
			if (isEL(frame)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), frame, Object.class);
				data.setValueExpression("frame", ve);
			} else {
				data.getAttributes().put("frame", frame);
			}
		}
		if (headerClass != null) {
			if (isEL(headerClass)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), headerClass, Object.class);
				data.setValueExpression("headerClass", ve);
			} else {
				data.getAttributes().put("headerClass", headerClass);
			}
		}
		if (lang != null) {
			if (isEL(lang)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), lang, Object.class);
				data.setValueExpression("lang", ve);
			} else {
				data.getAttributes().put("lang", lang);
			}
		}
		if (onclick != null) {
			if (isEL(onclick)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onclick, Object.class);
				data.setValueExpression("onclick", ve);
			} else {
				data.getAttributes().put("onclick", onclick);
			}
		}
		if (ondblclick != null) {
			if (isEL(ondblclick)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), ondblclick, Object.class);
				data.setValueExpression("ondblclick", ve);
			} else {
				data.getAttributes().put("ondblclick", ondblclick);
			}
		}
		if (onkeydown != null) {
			if (isEL(onkeydown)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onkeydown, Object.class);
				data.setValueExpression("onkeydown", ve);
			} else {
				data.getAttributes().put("onkeydown", onkeydown);
			}
		}
		if (onkeypress != null) {
			if (isEL(onkeypress)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onkeypress, Object.class);
				data.setValueExpression("onkeypress", ve);
			} else {
				data.getAttributes().put("onkeypress", onkeypress);
			}
		}
		if (onkeyup != null) {
			if (isEL(onkeyup)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onkeyup, Object.class);
				data.setValueExpression("onkeyup", ve);
			} else {
				data.getAttributes().put("onkeyup", onkeyup);
			}
		}
		if (onmousedown != null) {
			if (isEL(onmousedown)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onmousedown, Object.class);
				data.setValueExpression("onmousedown", ve);
			} else {
				data.getAttributes().put("onmousedown", onmousedown);
			}
		}
		if (onmousemove != null) {
			if (isEL(onmousemove)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onmousemove, Object.class);
				data.setValueExpression("onmousemove", ve);
			} else {
				data.getAttributes().put("onmousemove", onmousemove);
			}
		}
		if (onmouseout != null) {
			if (isEL(onmouseout)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onmouseout, Object.class);
				data.setValueExpression("onmouseout", ve);
			} else {
				data.getAttributes().put("onmouseout", onmouseout);
			}
		}
		if (onmouseover != null) {
			if (isEL(onmouseover)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), onmouseover, Object.class);
				data.setValueExpression("onmouseover", ve);
			} else {
				data.getAttributes().put("onmouseover", onmouseover);
			}
		}
		if (onmouseup != null) {
			if (isEL(onmouseup)) {
				ValueExpression vb = application.getExpressionFactory().createValueExpression(context.getELContext(), onmouseup, Object.class);
				data.setValueExpression("onmouseup", vb);
			} else {
				data.getAttributes().put("onmouseup", onmouseup);
			}
		}
		if (rowClasses != null) {
			if (isEL(rowClasses)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), rowClasses, Object.class);
				data.setValueExpression("rowClasses", ve);
			} else {
				data.getAttributes().put("rowClasses", rowClasses);
			}
		}
		if (rules != null) {
			if (isEL(rules)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), rules, Object.class);
				data.setValueExpression("rules", ve);
			} else {
				data.getAttributes().put("rules", rules);
			}
		}
		if (style != null) {
			if (isEL(style)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), style, Object.class);
				data.setValueExpression("style", ve);
			} else {
				data.getAttributes().put("style", style);
			}
		}
		if (styleClass != null) {
			if (isEL(styleClass)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), styleClass, Object.class);
				data.setValueExpression("styleClass", ve);
			} else {
				data.getAttributes().put("styleClass", styleClass);
			}
		}
		if (summary != null) {
			if (isEL(summary)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), summary, Object.class);
				data.setValueExpression("summary", ve);
			} else {
				data.getAttributes().put("summary", summary);
			}
		}
		if (title != null) {
			if (isEL(title)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), title, Object.class);
				data.setValueExpression("title", ve);
			} else {
				data.getAttributes().put("title", title);
			}
		}
		if (width != null) {
			if (isEL(width)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), width, Object.class);
				data.setValueExpression("width", ve);
			} else {
				data.getAttributes().put("width", width);
			}
		}
		if (expanded != null) {
			if (isEL(expanded)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), expanded, Object.class);
				data.setValueExpression("expanded", ve);
			} else {
				data.getAttributes().put("expanded", expanded);
			}
		}
		if (noarrows != null){
			if (isEL(noarrows)) {
				ValueExpression ve = application.getExpressionFactory().createValueExpression(context.getELContext(), noarrows, Object.class);
				data.setValueExpression("noarrows", ve);
			} else {
				data.getAttributes().put("noarrows", noarrows);
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
