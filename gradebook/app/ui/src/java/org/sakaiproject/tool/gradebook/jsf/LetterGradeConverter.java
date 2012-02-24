/**********************************************************************************
*
* $Id: etterGradeConverter.java 22547 2007-03-13 16:56:50Z ray@media.berkeley.edu $
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.ui.GradebookBean;

/**
 * Validates and standardizes letter grades.
 */
public class LetterGradeConverter implements Converter, Serializable {

	public Object getAsObject(FacesContext context, UIComponent component, String value)
		throws ConverterException {
		value = StringUtils.trimToNull(value);
		if (value != null) {
			// Get the current gradebook.
			GradebookBean gbb = (GradebookBean)FacesUtil.resolveVariable("gradebookBean");

			// Get the current grade mapping.
			Gradebook gradebook = gbb.getGradebookManager().getGradebook(gbb.getGradebookId());
			LetterGradePercentMapping mapping = gbb.getGradebookManager().getLetterGradePercentMapping(gradebook);

			// Find the corresponding standardized form, if any.
			value = mapping.standardizeInputGrade(value);
			if (value == null) {
				throw new ConverterException(new FacesMessage(FacesUtil.getLocalizedString(context,
					"org.sakaiproject.gradebook.tool.jsf.LetterGradeConverter.INVALID")));
			}
		}
		return value;
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}
		return (String)value;
	}

}


