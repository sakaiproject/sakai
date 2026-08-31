/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.jsf2.util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;
import lombok.Getter;
import lombok.Setter;

/**
 * This source file collects the dependencies of the Sakai tag library
 * on the JSF implementation into one place.
 * This is where JSF tags, renderers, and components that extend the
 * Sun JSF implementation (or the MyFaces implementation) live.
 * To switch between Sun RI vs. MyFaces, just comment/uncomment
 * the appropriate block of inner classes and recompile.
 */
public class JSFDepends
{
	  public static class ButtonRenderer extends com.sun.faces.renderkit.html_basic.ButtonRenderer {}
	  public static class CommandLinkRenderer extends com.sun.faces.renderkit.html_basic.CommandLinkRenderer {}

	  @Getter
	  @Setter
	  public static class PanelGridTag extends UIComponentELTag
	  {
		  private String styleClass;
		  private String style;
		  private String columns;
		  private String columnClasses;
		  private String rowClasses;
		  private String border;
		  private String cellpadding;
		  private String cellspacing;
		  private String width;
		  private String title;
		  private String id;
		  private String binding;
		  private String rendered;

		  @Override
		  public String getComponentType()
		  {
			  return "javax.faces.PanelGrid";
		  }

		  @Override
		  public String getRendererType()
		  {
			  return "javax.faces.Grid";
		  }

		  @Override
		  protected void setProperties(UIComponent component)
		  {
			  super.setProperties(component);

			  if (styleClass != null) {
				  component.getAttributes().put("styleClass", styleClass);
			  }
			  if (style != null) {
				  component.getAttributes().put("style", style);
			  }
			  if (columns != null) {
				  component.getAttributes().put("columns", columns);
			  }
			  if (columnClasses != null) {
				  component.getAttributes().put("columnClasses", columnClasses);
			  }
			  if (rowClasses != null) {
				  component.getAttributes().put("rowClasses", rowClasses);
			  }
			  if (border != null) {
				  component.getAttributes().put("border", border);
			  }
			  if (cellpadding != null) {
				  component.getAttributes().put("cellpadding", cellpadding);
			  }
			  if (cellspacing != null) {
				  component.getAttributes().put("cellspacing", cellspacing);
			  }
			  if (width != null) {
				  component.getAttributes().put("width", width);
			  }
			  if (title != null) {
				  component.getAttributes().put("title", title);
			  }
			  if (id != null) {
				  component.setId(id);
			  }
			  if (binding != null) {
				  component.setValueExpression("binding", getFacesContext().getApplication().getExpressionFactory()
						  .createValueExpression(getFacesContext().getELContext(), binding, UIComponent.class));
			  }
			  if (rendered != null) {
				  component.setRendered(Boolean.valueOf(rendered));
			  }
		  }
	  }
}
