/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.ui.GradebookBean;

/**
 * Validates and standardizes course grades.
 */
public class CourseGradeConverter implements Converter, Serializable {

	public Object getAsObject(FacesContext context, UIComponent component, String value)
		throws ConverterException {
		String standardizedGrade = null;
		if (value != null) {
			// Get the current gradebook.
			GradebookBean gbb = (GradebookBean)FacesUtil.resolveVariable("gradebookBean");

			// Get the current grade mapping.
			GradeMapping mapping = gbb.getGradebookManager().getGradebook(gbb.getGradebookId()).getSelectedGradeMapping();

			// Find the corresponding standardized form, if any.
			standardizedGrade = mapping.standardizeInputGrade(value);
			if (standardizedGrade == null) {
				throw new ConverterException(new FacesMessage(FacesUtil.getLocalizedString(context,
					"org.sakaiproject.gradebook.tool.jsf.CourseGradeConverter.INVALID")));
			}
		}
		return standardizedGrade;
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return null;
		}
		return (String)value;
	}

}


