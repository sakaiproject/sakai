/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.section.jsf;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.ext.HtmlTableRenderer;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;

public class SectionTableRenderer extends HtmlTableRenderer{
	private static final Log log = LogFactory.getLog(SectionTableRenderer.class);
	
	public SectionTableRenderer() {
		super();
		log.info("Constructing " + this);
	}
	protected void renderRowStart(FacesContext facesContext,
			ResponseWriter writer, UIData uiData, Iterator rowStyleClassIterator)
			throws IOException {
				super.renderRowStart(facesContext, writer, uiData, rowStyleClassIterator);
				log.info("**********************************");
				log.info("Working with a component of type " + uiData.getClass());
				log.info("rendering a table row with value " + uiData.getValue());
//				CourseSectionDecorator section = (CourseSectionDecorator)uiData.getValue();
	}
}
