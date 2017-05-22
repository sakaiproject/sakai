package org.sakaiproject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebTest {

	private static Logger log = LoggerFactory.getLogger(WebTest.class);
	
	@BeforeClass
	public static void beforeClass() {
		try {
            log.debug("starting oneTimeSetup");
			//oneTimeSetup();
            log.debug("finished oneTimeSetup");
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Test
	public void testContentDispositionInline() {
		Assert.assertEquals("inline; filename=\"file.txt\"; filename*=UTF-8''file.txt",
				Web.buildContentDisposition("file.txt", false));
	}

	@Test
	public void testContentDispositionAttachment() {
		Assert.assertEquals("attachment; filename=\"file.txt\"; filename*=UTF-8''file.txt",
				Web.buildContentDisposition("file.txt", true));
	}

	@Test
	public void testContentDispositionSemiColon() {
		Assert.assertEquals("inline; filename=\"start;stop.txt\"; filename*=UTF-8''start%3Bstop.txt",
				Web.buildContentDisposition("start;stop.txt", false));
	}

	@Test
	public void testContentDispositionQuotes() {
		Assert.assertEquals("inline; filename=\"start\\\"stop.txt\"; filename*=UTF-8''start%22stop.txt",
				Web.buildContentDisposition("start\"stop.txt", false));
	}

	@Test
	public void testContentDispositionUTF8() {
		// encoding hello world in greek.
		Assert.assertEquals("inline; filename=\"???? ??? ?????.txt\"; " +
				"filename*=UTF-8''%CE%93%CE%B5%CE%B9%CE%B1%20%CF%83%CE%B1%CF%82%20%CE%BA%CF%8C%CF%83%CE%BC%CE%BF.txt",
				Web.buildContentDisposition("\u0393\u03B5\u03B9\u03B1 \u03C3\u03B1\u03C2 \u03BA\u03CC\u03C3\u03BC\u03BF.txt", false));
	}

	@Test
	public void testContentDispositionISO8859() {
		Assert.assertEquals("inline; filename=\"exposé.txt\"; filename*=UTF-8''expos%C3%A9.txt",
				Web.buildContentDisposition("exposé.txt", false));
	}
}
