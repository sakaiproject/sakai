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
 * OverQuotaException is thrown whenever an attempt is made to change or add a Resource which fails because a quota would be violated by this attempt.
 * </p>
 * <p>
 * The id of the Resource is available as part of the exception.
 * </p>
 */
public class OverQuotaException extends SakaiException
{
	public OverQuotaException(String ref)
	{
		super(ref);
	}

	public String toString()
	{
		return super.toString() + " ref: " + m_id;
	}
}
