/**
 * Copyright (c) 2003-2013 The Apereo Foundation
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
package org.sakaiproject.exception;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PermissionExceptionTest {

	/**
	 * Check that we get a sensible message from a permission exception.
	 */
	@Test
	public void checkExceptionMessage() {
		PermissionException pe = new PermissionException("userId", "a.lock.that.failed", "/some/sakai/reference");
		assertNotNull(pe.getMessage());
		assertNotSame("", pe.getMessage());
		
		assertNotNull(pe.toString());
		assertNotSame("", pe.toString());
	}

}
