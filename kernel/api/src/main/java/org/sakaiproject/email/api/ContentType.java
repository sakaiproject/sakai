package org.sakaiproject.email.api;

/**
 * Common content types (primary/subtype) sent with message for use by client handling. Used when
 * sending email messages. The most commonly used are:
 * <p>
 * TEXT_PLAIN - for plain, unformated text only. Also consider {@link PlainTextFormat} when using
 * this.<br>
 * TEXT_HTML - for html formatted text
 * </p>
 */
public interface ContentType
{
	/**
	 * Plain message with no formatting
	 */
	String TEXT_PLAIN = "text/plain";

	/**
	 * Html formatted message
	 */
	String TEXT_HTML = "text/html";

	/**
	 * Richtext formatted message
	 */
	String TEXT_RICH = "text/richtext";
}