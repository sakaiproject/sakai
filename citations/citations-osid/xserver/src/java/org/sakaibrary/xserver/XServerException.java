/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaibrary.xserver;

public class XServerException extends Exception {

	private static final long serialVersionUID = 1L;

	// errorCode holds error_code from X-server
	private String errorCode;

	/**
	 * Constructs a new MetaLibException with given errorCode and errorText
	 *
	 * @param errorCode String representing error_code sent from X-server
	 * @param errorText String representing error_text sent from X-server
	 */
	public XServerException( String errorCode, String errorText ) {
		super( errorText );

		this.errorCode = errorCode;
	}

	/**
	 * Gets error_text sent from X-server
	 *
	 * @return String representing error_text
	 */
	public String getErrorText() {
		return getMessage();
	}

	/**
	 * Gets error_code sent from X-server
	 *
	 * @return String representing error_code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Get the int value of the error code (-1 if none)
	 */
	public int getErrorCodeIntValue()
	{
	  try
	  {
	    return Integer.parseInt(errorCode);
	  }
	  catch (NumberFormatException ignore) { }

	  return -1;
	}
}
