/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.content;

import junit.framework.TestCase;

public class CleanupDeletedContentTest extends TestCase {

	public void testFormatSize() {
		assertEquals("100Gb", CleanupDeletedContent.formatSize(1024L * 1024 * 1024 * 100));
		
		assertEquals("2Gb", CleanupDeletedContent.formatSize(1L+ Integer.MAX_VALUE));
		assertEquals("1Gb", CleanupDeletedContent.formatSize(Integer.MAX_VALUE));
		
		assertEquals("1Mb", CleanupDeletedContent.formatSize(1024 * 1024));
		
		assertEquals("1023b", CleanupDeletedContent.formatSize(1023));
		assertEquals("1kb", CleanupDeletedContent.formatSize(1024));
		assertEquals("1kb", CleanupDeletedContent.formatSize(1025));
	}

}
