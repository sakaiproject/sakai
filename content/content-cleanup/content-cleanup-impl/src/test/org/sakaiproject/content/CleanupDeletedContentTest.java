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
