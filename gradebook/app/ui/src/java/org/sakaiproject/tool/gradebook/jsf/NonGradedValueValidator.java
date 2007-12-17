/**********************************************************************************
*
* $Id: NonGradedValueValidator.java 9271 2007-12-17 09:52:49 $
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
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class NonGradedValueValidator implements Validator, Serializable 
{
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException 
	{
		if (value != null) 
		{
			String grade = (String) value;
			if(grade.length() > 8)
			{
				String temp = FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.NonGradedValueValidator.MAXLENGTH");
				throw new ValidatorException(new FacesMessage(
						FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.NonGradedValueValidator.MAXLENGTH")));
      }
		}
	}
}


