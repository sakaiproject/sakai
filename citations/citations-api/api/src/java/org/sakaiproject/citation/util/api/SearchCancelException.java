/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/citations/trunk/citations-api/api/src/java/org/sakaiproject/citation/util/api/SearchCancelException.java $
 * $Id: SearchCancelException.java 22676 2007-03-15 17:34:35Z gbhatnag@umich.edu $
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
package org.sakaiproject.citation.util.api;

/**
 * SearchCancelException encapsulates the exception thrown by the Citations Helper in the event of a
 * user-submitted cancellation of a search.
 * 
 * @author gbhatnag
 *
 */
public class SearchCancelException extends Exception
{

	public SearchCancelException()
	{
		super();
	}
	
	public SearchCancelException( String message )
	{
		super( message );
	}
}
