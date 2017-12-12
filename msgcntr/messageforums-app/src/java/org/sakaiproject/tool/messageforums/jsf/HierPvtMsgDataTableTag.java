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

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;
import javax.faces.el.MethodBinding;
import javax.faces.webapp.UIComponentTag;
import javax.faces.webapp.UIComponentBodyTag;
import javax.servlet.jsp.JspException;

import com.sun.faces.util.Util;
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
	
	private java.lang.String first;
	private java.lang.String rows;
	private java.lang.String value;
	private java.lang.String _var;
	
	private java.lang.String bgcolor;
	private java.lang.String border;
	private java.lang.String cellpadding;
	private java.lang.String cellspacing;
	private java.lang.String columnClasses;
	private java.lang.String dir;
	private java.lang.String footerClass;
	private java.lang.String frame;
	private java.lang.String headerClass;
	private java.lang.String lang;
	private java.lang.String onclick;
	private java.lang.String ondblclick;
	private java.lang.String onkeydown;
	private java.lang.String onkeypress;
	private java.lang.String onkeyup;
	private java.lang.String onmousedown;
	private java.lang.String onmousemove;
	private java.lang.String onmouseout;
	private java.lang.String onmouseover;
	private java.lang.String onmouseup;
	private java.lang.String rowClasses;
	private java.lang.String rules;
	private java.lang.String style;
	private java.lang.String styleClass;
	private java.lang.String summary;
	private java.lang.String title;
	private java.lang.String width;
	private java.lang.String expanded;
	
	//
	// Setter Methods
	//
	
	public void setFirst(java.lang.String first) {
		this.first = first;
	}
	
	public void setRows(java.lang.String rows) {
		this.rows = rows;
	}
	
	public void setValue(java.lang.String value) {
		this.value = value;
	}
	
	public void setVar(java.lang.String _var) {
		this._var = _var;
	}
	
	public void setBgcolor(java.lang.String bgcolor) {
		this.bgcolor = bgcolor;
	}
	
	public void setBorder(java.lang.String border) {
		this.border = border;
	}
	
	public void setCellpadding(java.lang.String cellpadding) {
		this.cellpadding = cellpadding;
	}
	
	public void setCellspacing(java.lang.String cellspacing) {
		this.cellspacing = cellspacing;
	}
	
	public void setColumnClasses(java.lang.String columnClasses) {
		this.columnClasses = columnClasses;
	}
	
	public void setDir(java.lang.String dir) {
		this.dir = dir;
	}
	
	public void setFooterClass(java.lang.String footerClass) {
		this.footerClass = footerClass;
	}
	
	public void setFrame(java.lang.String frame) {
		this.frame = frame;
	}
	
	public void setHeaderClass(java.lang.String headerClass) {
		this.headerClass = headerClass;
	}
	
	public void setLang(java.lang.String lang) {
		this.lang = lang;
	}
	
	public void setOnclick(java.lang.String onclick) {
		this.onclick = onclick;
	}
	
	public void setOndblclick(java.lang.String ondblclick) {
		this.ondblclick = ondblclick;
	}
	
	public void setOnkeydown(java.lang.String onkeydown) {
		this.onkeydown = onkeydown;
	}
	
	public void setOnkeypress(java.lang.String onkeypress) {
		this.onkeypress = onkeypress;
	}
	
	public void setOnkeyup(java.lang.String onkeyup) {
		this.onkeyup = onkeyup;
	}
	
	public void setOnmousedown(java.lang.String onmousedown) {
		this.onmousedown = onmousedown;
	}
	
	public void setOnmousemove(java.lang.String onmousemove) {
		this.onmousemove = onmousemove;
	}
	
	public void setOnmouseout(java.lang.String onmouseout) {
		this.onmouseout = onmouseout;
	}
	
	public void setOnmouseover(java.lang.String onmouseover) {
		this.onmouseover = onmouseover;
	}
	
	public void setOnmouseup(java.lang.String onmouseup) {
		this.onmouseup = onmouseup;
	}
	
	public void setRowClasses(java.lang.String rowClasses) {
		this.rowClasses = rowClasses;
	}
	
	public void setRules(java.lang.String rules) {
		this.rules = rules;
	}
	
	public void setStyle(java.lang.String style) {
		this.style = style;
	}
	
	public void setStyleClass(java.lang.String styleClass) {
		this.styleClass = styleClass;
	}
	
	public void setSummary(java.lang.String summary) {
		this.summary = summary;
	}
	
	public void setTitle(java.lang.String title) {
		this.title = title;
	}
	
	public void setWidth(java.lang.String width) {
		this.width = width;
	}
	
	public void setExpanded(java.lang.String expanded) {
		this.expanded = expanded;
	}
	
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
		
		if (first != null) {
			if (isValueReference(first)) {
				ValueBinding vb = Util.getValueBinding(first);
				data.setValueBinding("first", vb);
			} else {
				int _first = Integer.valueOf(first).intValue();
				data.setFirst(_first);
			}
		}
		if (rows != null) {
			if (isValueReference(rows)) {
				ValueBinding vb = Util.getValueBinding(rows);
				data.setValueBinding("rows", vb);
			} else {
				int _rows = Integer.valueOf(rows).intValue();
				data.setRows(_rows);
			}
		}
		if (value != null) {
			if (isValueReference(value)) {
				ValueBinding vb = Util.getValueBinding(value);
				data.setValueBinding("value", vb);
			} else {
				data.setValue(value);
			}
		}
		data.setVar(_var);
		if (bgcolor != null) {
			if (isValueReference(bgcolor)) {
				ValueBinding vb = Util.getValueBinding(bgcolor);
				data.setValueBinding("bgcolor", vb);
			} else {
				data.getAttributes().put("bgcolor", bgcolor);
			}
		}
		if (border != null) {
			if (isValueReference(border)) {
				ValueBinding vb = Util.getValueBinding(border);
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
				ValueBinding vb = Util.getValueBinding(cellpadding);
				data.setValueBinding("cellpadding", vb);
			} else {
				data.getAttributes().put("cellpadding", cellpadding);
			}
		}
		if (cellspacing != null) {
			if (isValueReference(cellspacing)) {
				ValueBinding vb = Util.getValueBinding(cellspacing);
				data.setValueBinding("cellspacing", vb);
			} else {
				data.getAttributes().put("cellspacing", cellspacing);
			}
		}
		if (columnClasses != null) {
			if (isValueReference(columnClasses)) {
				ValueBinding vb = Util.getValueBinding(columnClasses);
				data.setValueBinding("columnClasses", vb);
			} else {
				data.getAttributes().put("columnClasses", columnClasses);
			}
		}
		if (dir != null) {
			if (isValueReference(dir)) {
				ValueBinding vb = Util.getValueBinding(dir);
				data.setValueBinding("dir", vb);
			} else {
				data.getAttributes().put("dir", dir);
			}
		}
		if (footerClass != null) {
			if (isValueReference(footerClass)) {
				ValueBinding vb = Util.getValueBinding(footerClass);
				data.setValueBinding("footerClass", vb);
			} else {
				data.getAttributes().put("footerClass", footerClass);
			}
		}
		if (frame != null) {
			if (isValueReference(frame)) {
				ValueBinding vb = Util.getValueBinding(frame);
				data.setValueBinding("frame", vb);
			} else {
				data.getAttributes().put("frame", frame);
			}
		}
		if (headerClass != null) {
			if (isValueReference(headerClass)) {
				ValueBinding vb = Util.getValueBinding(headerClass);
				data.setValueBinding("headerClass", vb);
			} else {
				data.getAttributes().put("headerClass", headerClass);
			}
		}
		if (lang != null) {
			if (isValueReference(lang)) {
				ValueBinding vb = Util.getValueBinding(lang);
				data.setValueBinding("lang", vb);
			} else {
				data.getAttributes().put("lang", lang);
			}
		}
		if (onclick != null) {
			if (isValueReference(onclick)) {
				ValueBinding vb = Util.getValueBinding(onclick);
				data.setValueBinding("onclick", vb);
			} else {
				data.getAttributes().put("onclick", onclick);
			}
		}
		if (ondblclick != null) {
			if (isValueReference(ondblclick)) {
				ValueBinding vb = Util.getValueBinding(ondblclick);
				data.setValueBinding("ondblclick", vb);
			} else {
				data.getAttributes().put("ondblclick", ondblclick);
			}
		}
		if (onkeydown != null) {
			if (isValueReference(onkeydown)) {
				ValueBinding vb = Util.getValueBinding(onkeydown);
				data.setValueBinding("onkeydown", vb);
			} else {
				data.getAttributes().put("onkeydown", onkeydown);
			}
		}
		if (onkeypress != null) {
			if (isValueReference(onkeypress)) {
				ValueBinding vb = Util.getValueBinding(onkeypress);
				data.setValueBinding("onkeypress", vb);
			} else {
				data.getAttributes().put("onkeypress", onkeypress);
			}
		}
		if (onkeyup != null) {
			if (isValueReference(onkeyup)) {
				ValueBinding vb = Util.getValueBinding(onkeyup);
				data.setValueBinding("onkeyup", vb);
			} else {
				data.getAttributes().put("onkeyup", onkeyup);
			}
		}
		if (onmousedown != null) {
			if (isValueReference(onmousedown)) {
				ValueBinding vb = Util.getValueBinding(onmousedown);
				data.setValueBinding("onmousedown", vb);
			} else {
				data.getAttributes().put("onmousedown", onmousedown);
			}
		}
		if (onmousemove != null) {
			if (isValueReference(onmousemove)) {
				ValueBinding vb = Util.getValueBinding(onmousemove);
				data.setValueBinding("onmousemove", vb);
			} else {
				data.getAttributes().put("onmousemove", onmousemove);
			}
		}
		if (onmouseout != null) {
			if (isValueReference(onmouseout)) {
				ValueBinding vb = Util.getValueBinding(onmouseout);
				data.setValueBinding("onmouseout", vb);
			} else {
				data.getAttributes().put("onmouseout", onmouseout);
			}
		}
		if (onmouseover != null) {
			if (isValueReference(onmouseover)) {
				ValueBinding vb = Util.getValueBinding(onmouseover);
				data.setValueBinding("onmouseover", vb);
			} else {
				data.getAttributes().put("onmouseover", onmouseover);
			}
		}
		if (onmouseup != null) {
			if (isValueReference(onmouseup)) {
				ValueBinding vb = Util.getValueBinding(onmouseup);
				data.setValueBinding("onmouseup", vb);
			} else {
				data.getAttributes().put("onmouseup", onmouseup);
			}
		}
		if (rowClasses != null) {
			if (isValueReference(rowClasses)) {
				ValueBinding vb = Util.getValueBinding(rowClasses);
				data.setValueBinding("rowClasses", vb);
			} else {
				data.getAttributes().put("rowClasses", rowClasses);
			}
		}
		if (rules != null) {
			if (isValueReference(rules)) {
				ValueBinding vb = Util.getValueBinding(rules);
				data.setValueBinding("rules", vb);
			} else {
				data.getAttributes().put("rules", rules);
			}
		}
		if (style != null) {
			if (isValueReference(style)) {
				ValueBinding vb = Util.getValueBinding(style);
				data.setValueBinding("style", vb);
			} else {
				data.getAttributes().put("style", style);
			}
		}
		if (styleClass != null) {
			if (isValueReference(styleClass)) {
				ValueBinding vb = Util.getValueBinding(styleClass);
				data.setValueBinding("styleClass", vb);
			} else {
				data.getAttributes().put("styleClass", styleClass);
			}
		}
		if (summary != null) {
			if (isValueReference(summary)) {
				ValueBinding vb = Util.getValueBinding(summary);
				data.setValueBinding("summary", vb);
			} else {
				data.getAttributes().put("summary", summary);
			}
		}
		if (title != null) {
			if (isValueReference(title)) {
				ValueBinding vb = Util.getValueBinding(title);
				data.setValueBinding("title", vb);
			} else {
				data.getAttributes().put("title", title);
			}
		}
		if (width != null) {
			if (isValueReference(width)) {
				ValueBinding vb = Util.getValueBinding(width);
				data.setValueBinding("width", vb);
			} else {
				data.getAttributes().put("width", width);
			}
		}
		if (expanded != null) {
			if (isValueReference(expanded)) {
				ValueBinding vb = Util.getValueBinding(expanded);
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
