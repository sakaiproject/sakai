/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class FinResponseValidator implements Validator {

	public FinResponseValidator() {
		// TODO Auto-generated constructor stub
	}

	public void validate(FacesContext context, UIComponent component, Object value)
	throws ValidatorException {

		// The number can be in a decimal format or complex format
		if (!FinQuestionValidator.isRealNumber((String)value) && !FinQuestionValidator.isComplexNumber((String)value)) {
			String error = (String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "fin_invalid_characters_error");
			throw new ValidatorException(new FacesMessage(error));
		}
	}
}
