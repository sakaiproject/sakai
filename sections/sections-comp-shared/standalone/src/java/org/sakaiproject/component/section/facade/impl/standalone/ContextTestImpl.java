/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.component.section.facade.impl.standalone;

import org.sakaiproject.api.section.facade.manager.Context;

/**
 * Testing implementation of Context, which always returns TEST_CONTEXT_ID.
 * 
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class ContextTestImpl implements Context {
	private static final String TEST_CONTEXT_ID = "test context";
	private static final String TEST_CONTEXT_TITLE = "The Test Context Title";
	public String getContext(Object request) {
        return TEST_CONTEXT_ID;
	}
	
	public String getContextTitle(Object request) {
		return TEST_CONTEXT_TITLE;
	}
	
}
