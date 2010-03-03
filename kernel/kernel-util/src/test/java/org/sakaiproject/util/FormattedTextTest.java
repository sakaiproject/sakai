package org.sakaiproject.util;

import junit.framework.TestCase;

public class FormattedTextTest extends TestCase {

	public void testProcessAnchor() {
		// Check we add the target attribute
		assertEquals("<a  href=\"http://sakaiproject.org/\" target=\"_blank\">", FormattedText.processAnchor("<a href=\"http://sakaiproject.org/\">"));
	}
	
	public void testProcessAnchorRelative() {
		// Check we add the target attribute
		assertEquals("<a  href=\"other.html\" target=\"_blank\">", FormattedText.processAnchor("<a href=\"other.html\">"));
	}
	
	public void testProcessAnchorMailto() {
		assertEquals("<a  href=\"mailto:someone@example.com\" target=\"_blank\">", FormattedText.processAnchor("<a href=\"mailto:someone@example.com\">"));
	}
	
	public void testProcessAnchorName() {
		assertEquals("<a  href=\"#anchor\" target=\"_blank\">", FormattedText.processAnchor("<a href=\"#anchor\">"));
	}
}
