/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.model.api;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMErrorCodes;

@Slf4j
public class ScoBeanImpl implements ScoBean
{
	private static final long serialVersionUID = 1L;

	// String value of FALSE for JavaScript returns.
	protected static final String STRING_FALSE = "false";

	// String value of TRUE for JavaScript returns.
	protected static final String STRING_TRUE = "true";

	// Indicates if the SCO is in a 'terminated' state.
	protected boolean mTerminateCalled = false;

	@Setter private int version;
	@Setter private boolean isInitialized = false;
	@Getter @Setter private boolean isSuspended = false;
	@Getter @Setter private boolean isTerminated = false;
	@Getter @Setter private Long dataManagerId;
	@Getter private String scoId;
	private SessionBean sessionBean;

	public ScoBeanImpl(String scoId, SessionBean sessionBean)
	{
		this.scoId = scoId;
		this.sessionBean = sessionBean;
	}

	/**
	 * Clears error codes and sets mInitialedState and mTerminated State to
	 * default values.
	 */
	@Override
	public void clearState()
	{
		setInitialized(false);
		setTerminated(false);
		mTerminateCalled = false;
		IErrorManager errorManager = sessionBean.getErrorManager();
		if (errorManager != null)
		{
			errorManager.clearCurrentErrorCode();
		}
	}

	/**
	 * Insert a backward slash (\) before each double quote (") or backslash (\)
	 * to allow the character to be displayed in the data model log. Receives
	 * the value and returns the newly formatted value
	 */
	public String formatValue(String baseString)
	{
		int indexQuote = baseString.indexOf('"');
		int indexSlash = baseString.indexOf('\\');

		if (indexQuote >= 0 || indexSlash >= 0)
		{
			int index = 0;
			String strFirst = "";
			String strLast = "";
			char insertValue = '\\';

			while (index < baseString.length())
			{
				if ((baseString.charAt(index) == '\"') || (baseString.charAt(index) == '\\'))
				{
					strFirst = baseString.substring(0, index);
					strLast = baseString.substring(index, baseString.length());
					baseString = strFirst.concat(Character.toString(insertValue)).concat(strLast);
					index += 2;
				}
				else
				{
					index++;
				}
			}
		}

		return baseString;
	}

	@Override
	public boolean isInitialized()
	{
		IErrorManager errorManager = sessionBean.getErrorManager();
		if ((!isTerminated) && (version == ScoBean.SCO_VERSION_2))
		{
			errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
		}

		return isInitialized;
	}

	/**
	 * This method implements the interface with the JavaScript running on the
	 * client side of the Sample RTE.
	 * 
	 * @param message
	 *            The String that is evaluated by the Java Script eval
	 *            command--usually it is a Java Script function name.
	 */
	public void jsCall(String message)
	{
		// TODO: Find a way to communicate back to the browser here  
		//JSObject.getWindow(this).eval(message);
		log.warn("Called jsCall with message: {}", message);
	}
}
