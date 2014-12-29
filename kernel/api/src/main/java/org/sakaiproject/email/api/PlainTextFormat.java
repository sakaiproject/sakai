package org.sakaiproject.email.api;

/**
 * Format types of text when using a content-type of text.  Taken from RFC 2646.<br>
 * http://www.ietf.org/rfc/rfc2646.txt
 */
public interface PlainTextFormat
{
	String FIXED = "fixed";
	String FLOWED = "flowed";
}
