package org.sakaiproject.util;

import junit.framework.TestCase;

public class WebTest extends TestCase {

	public void testContentDispositionInline() {
		assertEquals("inline; filename=\"file.txt\"; filename*=UTF-8''file.txt",
				Web.buildContentDisposition("file.txt", false));
	}

	public void testContentDispositionAttachment() {
		assertEquals("attachment; filename=\"file.txt\"; filename*=UTF-8''file.txt",
				Web.buildContentDisposition("file.txt", true));
	}

	public void testContentDispositionSemiColon() {
		assertEquals("inline; filename=\"start;stop.txt\"; filename*=UTF-8''start%3Bstop.txt",
				Web.buildContentDisposition("start;stop.txt", false));
	}

	public void testContentDispositionQuotes() {
		assertEquals("inline; filename=\"start\\\"stop.txt\"; filename*=UTF-8''start%22stop.txt",
				Web.buildContentDisposition("start\"stop.txt", false));
	}

	public void testContentDispositionUTF8() {
		// encoding hello world in greek.
		assertEquals("inline; filename=\"???? ??? ?????.txt\"; " +
				"filename*=UTF-8''%CE%93%CE%B5%CE%B9%CE%B1%20%CF%83%CE%B1%CF%82%20%CE%BA%CF%8C%CF%83%CE%BC%CE%BF.txt",
				Web.buildContentDisposition("\u0393\u03B5\u03B9\u03B1 \u03C3\u03B1\u03C2 \u03BA\u03CC\u03C3\u03BC\u03BF.txt", false));
	}

	public void testContentDispositionISO8859() {
		assertEquals("inline; filename=\"exposé.txt\"; filename*=UTF-8''expos%C3%A9.txt",
				Web.buildContentDisposition("exposé.txt", false));
	}
}
