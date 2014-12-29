/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/MessageTitleValidator.java $
 * $Id: MessageForumsNavigationHandler.java 9227 2007-04-30 15:02:42Z rjlowe@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.sun.faces.util.MessageFactory;

public class MessageTitleValidator implements Validator
{
	public static final String MESSAGE_KEY = "cdfm_invalidMessageTitleString";
	
	public MessageTitleValidator() {}
	
	public void validate(FacesContext context, UIComponent component, Object value)
		throws ValidatorException{
		
		if((context == null) || (component == null)) {
			throw new IllegalArgumentException (
					context == null ? "Context" : "Component" + " cannot be null");
		}
		
		String val = (String) value;
		
		//null imput is not considered valid
		if (val == null || val.trim().length() == 0)
		{
			throw new ValidatorException (MessageFactory.getMessage(context, MESSAGE_KEY, new Object[] {val}));
		}
	}
	
}
