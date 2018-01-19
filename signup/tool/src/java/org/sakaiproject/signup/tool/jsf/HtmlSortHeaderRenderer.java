/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf;

import java.io.IOException;
import java.text.MessageFormat;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlLinkRendererBase;

import org.sakaiproject.util.ResourceLoader;

/**
 * Based on org.apache.myfaces.custom.sortheader.HtmlSortHeaderRenderer.
 * Modified to better distinguish the current sort column and to use an image file
 * to indicate direction.
 */
@Slf4j
public class HtmlSortHeaderRenderer extends HtmlLinkRendererBase {
	public final static String CURRENT_SORT_STYLE = "currentSort";
	public final static String NOT_CURRENT_SORT_STYLE = "notCurrentSort";

	private ResourceLoader rb = new ResourceLoader("messages");

	public void encodeBegin(FacesContext facesContext, UIComponent component)
			throws IOException {
		// If this is a currently sorted sort header, always give it the "currentSort" CSS style class
        if (component instanceof HtmlCommandSortHeader) {
            HtmlCommandSortHeader sortHeader = (HtmlCommandSortHeader)component;
            String styleClass = StringUtils.trimToNull(getStyleClass(facesContext, component));
            String newStyleClass;
            String unStyleClass;
            if (sortHeader.findParentDataTable().getSortColumn().equals(sortHeader.getColumnName())) {
            	newStyleClass = CURRENT_SORT_STYLE;
            	unStyleClass = NOT_CURRENT_SORT_STYLE;
            } else {
            	newStyleClass = NOT_CURRENT_SORT_STYLE;
            	unStyleClass = CURRENT_SORT_STYLE;
            }
            if (StringUtils.indexOf(styleClass, newStyleClass) == -1) {
            	if (StringUtils.indexOf(styleClass, unStyleClass) != -1) {
            		styleClass = StringUtils.replace(styleClass, unStyleClass, newStyleClass);
            	} else if (styleClass != null) {
            		styleClass = (new StringBuilder(styleClass)).append(' ').append(newStyleClass).toString();
            	} else {
            		styleClass = newStyleClass;
            	}
            	sortHeader.setStyleClass(styleClass);
            }
        }
 		super.encodeBegin(facesContext, component); //check for NP
	}

	public void encodeEnd(FacesContext facesContext, UIComponent component)
			throws IOException {
		if (log.isDebugEnabled()) log.debug("encodeEnd rendering " + component);
		if (!UserRoleUtils.isEnabledOnUserRole(component)) {
            super.encodeEnd(facesContext, component);
        } else {
			HtmlCommandSortHeader sortHeader = (HtmlCommandSortHeader) component;
			HtmlDataTable dataTable = sortHeader.findParentDataTable();

			if (sortHeader.isArrow() && sortHeader.getColumnName().equals(dataTable.getSortColumn())) {
				ResponseWriter writer = facesContext.getResponseWriter();

                writer.write(HTML.NBSP_ENTITY);

                HtmlGraphicImage image = new HtmlGraphicImage();
                if (dataTable.isSortAscending()) {
                    image.setValue("/library/image/sakai/sortascending.gif");
                    image.setAlt(MessageFormat.format(rb.getString("sort_ascending"), sortHeader.getColumnName().toLowerCase()));
				} else {
                    image.setValue("/library/image/sakai/sortdescending.gif");
                    image.setAlt(MessageFormat.format(rb.getString("sort_descending"), sortHeader.getColumnName().toLowerCase()));
				}

                writer.startElement(HTML.IMG_ELEM, image);
                writer.writeURIAttribute("src", image.getValue(), null);
                writer.writeAttribute("alt",image.getAlt().toString(), null);
                writer.endElement(HTML.IMG_ELEM);
			}
            super.encodeEnd(facesContext, component);
		}
	}
}


