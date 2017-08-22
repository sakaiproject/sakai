/**
 * Copyright (c) 2003-2007 The Apereo Foundation
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
package org.sakaiproject.portal.render.api;

import java.io.IOException;

/**
 * Exception thrown when an error occurs while preprocessing or rendering a
 * portlet.
 * 
 * @since Sakai 2.2.3
 * @version $Rev$
 */
public class ToolRenderException extends IOException
{

	/**
	 * Root cause;
	 */
	private Throwable throwable;

	/**
	 * Default constructor
	 * 
	 * @param message
	 *        the exception message
	 * @param throwable
	 *        the root cause.
	 */
	public ToolRenderException(String message, Throwable throwable)
	{
		super(message);
		this.throwable = throwable;
	}

	/**
	 * Alternate constructor indicating that this exception is the root cause.
	 * 
	 * @param message
	 *        the exception message
	 */
	public ToolRenderException(String message)
	{
		super(message);
	}

	/**
	 * Retrieve the exception which caused this exception to be rethrown.
	 * 
	 * @return the root cause
	 */
	public Throwable getThrowable()
	{
		return throwable;
	}

}
