/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/exception/SakaiException.java $
 * $Id: OverQuotaException.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2012 Sakai Foundation
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
 * SakaiException is a baseclass for Sakai Exceptions * </p>
 * <p>
 * The id of the Resource is available as part of the exception.
 * </p>
 * @since 1.4.0
 */
public class SakaiException extends Exception
{
	protected String m_id = null;

	public SakaiException () {}
	public SakaiException(String id)
	{
		m_id = id;
	}

	/**
	 * @param cause
	 */
	public SakaiException(Throwable cause)
	{
			super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SakaiException(String message, Throwable cause)
	{
			super(message, cause);
	}


	public String getId() 
	{
			return m_id;
	}

	public String getReference()
	{
		return m_id;
	}

	@Override
	public String toString()
	{
		return super.toString() + " id: " + m_id;
	}

}
