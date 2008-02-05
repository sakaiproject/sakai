package org.sakaiproject.util;

import junit.framework.TestCase;

public class WebTest extends TestCase {
	
	public void testBasicUrlMatch() {
		assertEquals("I like <a href=\"http://www.apple.com\">http://www.apple.com</a> and stuff", Web.encodeUrlsAsHtml(Web.escapeHtml("I like http://www.apple.com and stuff")));
	}
	
	public void testCanDoSsl() {
		assertEquals("<a href=\"https://sakaiproject.org\">https://sakaiproject.org</a>", Web.encodeUrlsAsHtml("https://sakaiproject.org"));
	}
	
	public void testCanIgnoreTrailingExclamation() {
		assertEquals("Hey, it's <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>!", Web.encodeUrlsAsHtml("Hey, it's http://sakaiproject.org!"));
	}
	
	public void testCanIgnoreTrailingQuestion() {
		assertEquals("Have you ever seen <a href=\"http://sakaiproject.org\">http://sakaiproject.org</a>? Just wondering.", Web.encodeUrlsAsHtml("Have you ever seen http://sakaiproject.org? Just wondering."));
	}
	
	public void testCanEncodeQueryString() {
		assertEquals("See <a href=\"http://sakaiproject.org/index.php?task=blogcategory&id=181\">http://sakaiproject.org/index.php?task=blogcategory&amp;id=181</a> for more info.", Web.encodeUrlsAsHtml(Web.escapeHtml("See http://sakaiproject.org/index.php?task=blogcategory&id=181 for more info.")));
	}
	
	public void testCanTakePortNumber() {
		assertEquals("<a href=\"http://localhost:8080/portal\">http://localhost:8080/portal</a>", Web.encodeUrlsAsHtml("http://localhost:8080/portal"));
	}
	
	public void testCanTakePortNumberAndQueryString() {
		assertEquals("<a href=\"http://www.loco.com:3000/portal?person=224\">http://www.loco.com:3000/portal?person=224</a>", Web.encodeUrlsAsHtml("http://www.loco.com:3000/portal?person=224"));
	}
	
	public void testCanIgnoreExistingHref() {
		assertEquals("<a href=\"http://sakaiproject.org\">Sakai Project</a>", Web.encodeUrlsAsHtml("<a href=\"http://sakaiproject.org\">Sakai Project</a>"));
	}
	
	public void testALongUrlFromNyTimes() {
		assertEquals("<a href=\"http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW\">http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&amp;symb=LLNW</a>",
				Web.encodeUrlsAsHtml(Web.escapeHtml("http://www.nytimes.com/mem/MWredirect.html?MW=http://custom.marketwatch.com/custom/nyt-com/html-companyprofile.asp&symb=LLNW")));
	}

}
