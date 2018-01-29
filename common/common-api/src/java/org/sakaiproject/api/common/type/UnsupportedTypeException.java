/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.api.common.type;

/**
 * If a Manager does not support a given Type, this would be an appropriate exception to throw.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @see {@link java.lang.Error}
 */
public class UnsupportedTypeException extends Exception
{
	private static final long serialVersionUID = 3258132466203242544L;

	/**
	 * 
	 */
	public UnsupportedTypeException()
	{
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UnsupportedTypeException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnsupportedTypeException(Throwable cause)
	{
		super(cause);
	}
}
