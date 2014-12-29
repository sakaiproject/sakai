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

package org.sakaiproject.signup.tool.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.sakaiproject.signup.tool.util.Utilities;

/**
 * <P>
 * This class is a validator to make sure that user has to input something else
 * other than space(s) character only.
 * </P>
 */
public class EmptyStringValidator implements Validator {

	/**
	 * Throw exception if there is only space(s) character as input.
	 */
	public void validate(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
		String str = (String) value;
		if (str.trim().length() < 1) {
			((UIInput) toValidate).setValid(false);

			FacesMessage message = new FacesMessage();
			message.setDetail(Utilities.rb.getString("signup.validator.stringWithSpaceOnly"));
			message.setSummary(Utilities.rb.getString("signup.validator.stringWithSpaceOnly"));
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

}
