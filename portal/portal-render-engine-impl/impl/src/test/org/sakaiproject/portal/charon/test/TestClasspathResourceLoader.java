/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.test;

import java.io.InputStream;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * @author ieb
 *
 */
public class TestClasspathResourceLoader extends ClasspathResourceLoader
{

	/**
	 * 
	 */
	public TestClasspathResourceLoader()
	{
		// TODO Auto-generated constructor stub
	}
	/* (non-Javadoc)
	 * @see org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader#getResourceStream(java.lang.String)
	 */
	@Override
	public synchronized InputStream getResourceStream(String arg0) throws ResourceNotFoundException
	{
		InputStream in =  super.getResourceStream(arg0);
		if ( in == null ) {
			in =  this.getClass().getResourceAsStream(arg0);
		}
		if ( in == null ) {
			System.err.println("Failed to load "+arg0);
			
		}
		return in;
	}

}
