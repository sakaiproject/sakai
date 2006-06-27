/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.jsf;

import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


public class VBarChartTag extends UIComponentTag {
	// Declare a bean property for attributes.
	public String	type		= "week";
	public String	lastDateStr	= null;
	public String	column1Str	= null;
	public String	column2Str	= null;
	public String	column3Str	= null;
	public Long		lastDate	= null;
	public List		column1		= null;
	public List		column2		= null;
	public List		column3		= null;

	// Associate the renderer and component type.
	public String getComponentType() {
		return "org.sakaiproject.sitestats.tool.jsf.VBarChart";
	}

	public String getRendererType() {
		return null;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		FacesContext context = FacesContext.getCurrentInstance();

		// type
		if(type != null){
			if(isValueReference(type)){
				Application app = context.getApplication();
				ValueBinding vb = app.createValueBinding(type);
				component.setValueBinding("type", vb);
			}else component.getAttributes().put("type", type);
		}
		
		// column1
		if (isValueReference(column1Str)) {
            ValueBinding vb = context.getApplication().createValueBinding(column1Str);
            if(vb!=null){
                column1 = (List) vb.getValue(context);
                component.setValueBinding("column1", vb);
            }else component.getAttributes().put("column1", column1);
        } else {
            throw new NullPointerException();
        }	

		// column2
		if (isValueReference(column2Str)) {
            ValueBinding vb=context.getApplication().createValueBinding(column2Str);
            if(vb!=null){
            	column2 = (List) vb.getValue(context);
                component.setValueBinding("column2", vb);
            }else component.getAttributes().put("column2", column2);
        } else {
            throw new NullPointerException();
        }	

		// column3
		if (isValueReference(column3Str)) {
            ValueBinding vb=context.getApplication().createValueBinding(column3Str);
            if(vb!=null){
            	column3 = (List) vb.getValue(context);
                component.setValueBinding("column3", vb);
            }else component.getAttributes().put("column3", column3);
        } else {
            throw new NullPointerException();
        }				

		// lastDate
		if (isValueReference(lastDateStr)) {
            ValueBinding vb=context.getApplication().createValueBinding(lastDateStr);
            if(vb!=null){
            	lastDate = (Long) vb.getValue(context);
                component.setValueBinding("lastDate", vb);
            }else component.getAttributes().put("lastDate", lastDate);
        } else {
            throw new NullPointerException();
        }	
	}

	public void release() {
		super.release();
		type = null;
		column1 = null;
		column2 = null;
	}


	public void setType(String type) {
		this.type = type;
	}

	public void setColumn1(String column1) {
		this.column1Str = column1;
	}

	public void setColumn2(String column2) {
		this.column2Str = column2;
	}

	public void setColumn3(String column3) {
		this.column3Str = column3;
	}

	public void setLastDate(String date) {
		this.lastDateStr = date;
	}
}
