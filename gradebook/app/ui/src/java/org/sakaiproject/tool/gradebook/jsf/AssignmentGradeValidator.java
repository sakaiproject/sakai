/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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
import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validates assignment grades entered into the gradebook.  Since we display a
 * maximum of two decimal places in the UI, we use this validator to ensure that
 * the maximum precision entered into the gradebook is also two decimal places.
 * This should reduce rounding errors between actual scores and what is displayed
 * in the UI.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class AssignmentGradeValidator implements Validator, Serializable {
	/**
	 * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		if (value != null) {
			if (!(value instanceof Number)) {
				throw new IllegalArgumentException("The assignment grade must be a number");
			}
			double grade = ((Number)value).doubleValue();
			BigDecimal bd = new BigDecimal(grade);
			bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
			double roundedVal = bd.doubleValue();
			double diff = grade - roundedVal;
			if(diff != 0) {
				throw new ValidatorException(new FacesMessage(
		                	FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.PRECISION")));
			}
		}
	}
}
