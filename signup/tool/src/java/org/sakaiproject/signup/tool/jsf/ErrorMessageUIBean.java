/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class will provide a placeholder for keeping error messages.
 * </P>
 */
public class ErrorMessageUIBean {

	private boolean error = false;

	private List<String> errorMessages = new ArrayList<String>();

	/**
	 * Constructor
	 * 
	 */
	public ErrorMessageUIBean() {
	}

	/**
	 * This is a setter.
	 * 
	 * @param errorMsg
	 *            a string error message value.
	 */
	public void setErrorMessages(String errorMsg) {
		if (!this.errorMessages.contains(errorMsg))
			this.errorMessages.add(errorMsg);
		setError(true);
	}

	/**
	 * This is a setter.
	 * 
	 * @param errorMsgs
	 *            a list of string error message objects.
	 */
	public void setErrorMessages(List<String> errorMsgs) {
		this.errorMessages = errorMsgs;
		setError(true);
	}

	/**
	 * This is a getter method for UI.
	 * 
	 * @return a string value.
	 */
	public String getErrorMessage() {
		StringBuffer msg = new StringBuffer();
		int i = 0;
		for (Iterator iter = errorMessages.iterator(); iter.hasNext(); i++) {
			if (i != 0) {
				// msg.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;and
				// &nbsp;");
				msg.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			msg.append((String) iter.next());
		}

		reset();
		return msg.toString();
	}

	/**
	 * This is a getter method.
	 * 
	 * @return a list of String object.
	 */
	public List getErrorMessages() {
		List errorMsgs = this.errorMessages;
		reset();
		return errorMsgs;
	}

	/**
	 * this is for UI purpose.
	 * 
	 * @return true if there is a error message.
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * This is a setter.
	 * 
	 * @param error
	 *            a boolean value.
	 */
	public void setError(boolean error) {
		this.error = error;
	}

	private void reset() {
		errorMessages.clear();
		setError(false);
	}
}
