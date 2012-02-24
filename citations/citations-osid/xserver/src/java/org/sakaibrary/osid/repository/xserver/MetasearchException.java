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

package org.sakaibrary.osid.repository.xserver;

public class MetasearchException extends org.osid.repository.RepositoryException {

	private static final long serialVersionUID = 1L;

	public static final String METASEARCH_ERROR = "Metasearch error has occured. Please contact your site's support team.";
	public static final String SESSION_TIMED_OUT = "Metasearch session has " +
			"timed out. Please restart your search session.";
	public static final String ASSET_NOT_FETCHED = "An Asset is available, but" +
			" has not yet been fetched.";
	
	protected MetasearchException( String message ) {
		super( message );
	}
}
