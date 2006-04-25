/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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


