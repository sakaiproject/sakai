/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.adl.api.ecmascript;

import java.io.Serializable;

public interface IErrorManager extends Serializable {

	/**
	 * The SCORM Version 1.2 API constant.  System constant <br>
	 * used to indicate APIs supported by this error manager.
	 */
	public static final int SCORM_1_2_API = 1;

	/**
	 * The SCORM 2004 API constant.  System constant <br>
	 * used to indicate APIs supported by this error manager.
	 */
	public static final int SCORM_2004_API = 2;

	/**
	 * Returns The current avaliable error code.
	 *
	 * @return The value of the current error code that was set by the most
	 *         recent API call.
	 */
	public String getCurrentErrorCode();

	/**
	 * Sets the error code (from the predefined list of codes).
	 *
	 * @param iCode  The error code being set.
	 */
	public void setCurrentErrorCode(int iCode);

	/**
	 * Sets the current error code to No Error 
	 * (<code>APIErrorCodes.NO_ERROR</code>)
	 */
	public void clearCurrentErrorCode();

	/**
	 * Returns the text associated with a given error code.
	 *
	 * @param iCode  The specified error code for which an error description
	 *               is being requested.
	 *
	 * @return The text associated with the specfied error code.
	 */
	public String getErrorDescription(String iCode);

	/**
	 * Returns the text associated with the current error code.
	 *
	 * @return The text associated with the specfied error code.
	 */
	public String getErrorDescription();

	/**
	 * Returns the diagnostic text associated with an error code.
	 *
	 * @param iCode  The specified error code for which error diagnostic
	 *               information is being requested.
	 *
	 * @return The diagnostic text associated with the specificed error code.
	 */
	public String getErrorDiagnostic(String iCode);

	/**
	 * Returns the diagnostic text associated with the current error code.
	 *
	 * @return The diagnostic text associated with the specificed error code.
	 */
	public String getErrorDiagnostic();

	/**
	 * Determines whether or not the Error Code passed in 
	 * (<code>iErrorCode</code>) is a valid and recognizable SCORM error code.
	 *
	 * @param iErrorCode The error code.
	 * @return Indicates whether or not the error code is valid.
	 */
	public boolean isValidErrorCode(String iErrorCode);

}