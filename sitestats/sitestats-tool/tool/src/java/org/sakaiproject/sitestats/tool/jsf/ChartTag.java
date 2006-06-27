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

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


public class ChartTag extends UIComponentTag {
	// Parameters def. (required)
	public final static String	P_RENDERED			= "rendered";
	public final static String	P_TYPE				= "type";
	public final static String	P_DATASET			= "dataset";
	public final static String	P_HEIGHT			= "height";
	public final static String	P_WIDTH				= "width";
	// Parameters def. (optional)
	public final static String	P_TITLE				= "title";
	public final static String	P_LEGEND			= "legend";
	public final static String	P_XLABEL			= "xlabel";
	public final static String	P_YLABEL			= "ylabel";
	public final static String	P_BACKGROUND		= "background";
	public final static String	P_DRAWTRANSPARENT	= "drawTransparent";
	public final static String	P_DRAWBORDER		= "drawBorder";
	public final static String	P_DRAWOUTLINE		= "drawOutline";

	// Declare a bean property for attributes.
	public String				rendered			= null;
	public String				type				= null;
	public String				dataset				= null;
	public String				height				= null;
	public String				width				= null;
	public String				title				= null;
	public String				legend				= null;
	public String				xlabel				= null;
	public String				ylabel				= null;
	public String				background			= null;
	public String				drawtransparent		= null;
	public String				drawborder			= null;
	public String				drawoutline			= null;

	// Associate the renderer and component type.
	public String getComponentType() {
		return ChartComponent.COMPONENT_TYPE;
	}

	public String getRendererType() {
		return null;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		
		if(rendered != null){
			if(isValueReference(rendered)){				
				ValueBinding vb = app.createValueBinding(rendered);
				component.setValueBinding(P_RENDERED, vb);
			}else component.getAttributes().put(P_RENDERED, rendered);
		}
		
		if(type != null){
			if(isValueReference(type)){				
				ValueBinding vb = app.createValueBinding(type);
				component.setValueBinding(P_TYPE, vb);
			}else component.getAttributes().put(P_TYPE, type);
		}
		
		if(dataset != null){
			if(isValueReference(dataset)){				
				ValueBinding vb = app.createValueBinding(dataset);
				component.setValueBinding(P_DATASET, vb);
			}else component.getAttributes().put(P_DATASET, dataset);
		}
		
		if(height != null){
			if(isValueReference(height)){				
				ValueBinding vb = app.createValueBinding(height);
				component.setValueBinding(P_HEIGHT, vb);
			}else component.getAttributes().put(P_HEIGHT, height);
		}
		
		if(width != null){
			if(isValueReference(width)){				
				ValueBinding vb = app.createValueBinding(width);
				component.setValueBinding(P_WIDTH, vb);
			}else component.getAttributes().put(P_WIDTH, width);
		}
		
		if(title != null){
			if(isValueReference(title)){				
				ValueBinding vb = app.createValueBinding(title);
				component.setValueBinding(P_TITLE, vb);
			}else component.getAttributes().put(P_TITLE, title);
		}
		
		if(legend != null){
			if(isValueReference(legend)){				
				ValueBinding vb = app.createValueBinding(legend);
				component.setValueBinding(P_LEGEND, vb);
			}else component.getAttributes().put(P_LEGEND, legend);
		}
		
		if(xlabel != null){
			if(isValueReference(xlabel)){				
				ValueBinding vb = app.createValueBinding(xlabel);
				component.setValueBinding(P_XLABEL, vb);
			}else component.getAttributes().put(P_XLABEL, xlabel);
		}
		
		if(ylabel != null){
			if(isValueReference(ylabel)){				
				ValueBinding vb = app.createValueBinding(ylabel);
				component.setValueBinding(P_YLABEL, vb);
			}else component.getAttributes().put(P_YLABEL, ylabel);
		}
		
		if(background != null){
			if(isValueReference(background)){				
				ValueBinding vb = app.createValueBinding(background);
				component.setValueBinding(P_BACKGROUND, vb);
			}else component.getAttributes().put(P_BACKGROUND, background);
		}
		
		if(drawtransparent != null){
			if(isValueReference(drawtransparent)){				
				ValueBinding vb = app.createValueBinding(drawtransparent);
				component.setValueBinding(P_DRAWTRANSPARENT, vb);
			}else component.getAttributes().put(P_DRAWTRANSPARENT, drawtransparent);
		}
		
		if(drawborder != null){
			if(isValueReference(drawborder)){				
				ValueBinding vb = app.createValueBinding(drawborder);
				component.setValueBinding(P_DRAWBORDER, vb);
			}else component.getAttributes().put(P_DRAWBORDER, drawborder);
		}
		
		if(drawoutline != null){
			if(isValueReference(drawoutline)){				
				ValueBinding vb = app.createValueBinding(drawoutline);
				component.setValueBinding(P_DRAWOUTLINE, vb);
			}else component.getAttributes().put(P_DRAWOUTLINE, drawoutline);
		}
	}

	public void release() {
		super.release();
		rendered = null;
		type = null;
		dataset = null;
		height = null;
		width = null;
		title = null;
		legend = null;
		xlabel = null;
		ylabel = null;
		background = null;
		drawtransparent = null;
		drawborder = null;
		drawoutline = null;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getDrawborder() {
		return drawborder;
	}

	public void setDrawborder(String drawborder) {
		this.drawborder = drawborder;
	}

	public String getDrawoutline() {
		return drawoutline;
	}

	public void setDrawoutline(String drawoutline) {
		this.drawoutline = drawoutline;
	}

	public String getDrawtransparent() {
		return drawtransparent;
	}

	public void setDrawtransparent(String drawtransparent) {
		this.drawtransparent = drawtransparent;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public String getRendered() {
		return rendered;
	}

	public void setRendered(String rendered) {
		this.rendered = rendered;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getXlabel() {
		return xlabel;
	}

	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;
	}

	public String getYlabel() {
		return ylabel;
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;
	}

}
