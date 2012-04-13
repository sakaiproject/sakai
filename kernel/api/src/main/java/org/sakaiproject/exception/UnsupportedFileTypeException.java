/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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
 * This is thrown when a file is needed but the selected file doesn't have the correct type
 */
public class UnsupportedFileTypeException extends SakaiException
{
	/**
	 *
	 */
	public UnsupportedFileTypeException()
	{
		super();
	}

	/**
	 * @param cause
	 */
	public UnsupportedFileTypeException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 */
	public UnsupportedFileTypeException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedFileTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
