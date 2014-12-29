/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.exception;

/**
 * <p>
 * ImportException is thrown whenever there is in error while importing schedule items.
 * </p>
 */
public class ImportException extends SakaiException
{
	/**
	 * Default constructor
	 */
	public ImportException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ImportException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ImportException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ImportException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public String getMessage() {
		return getId();
	}

}
