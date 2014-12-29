package org.sakaiproject.email.api;

/**
 * Type safe constant for recipient types
 */
public enum RecipientType
{
	// recipients to be marked in the "to" header
	TO,
	// recipients to be marked in the "cc" header
	CC,
	// recipients to be marked in the "bcc" header
	BCC,
	// actual recipients of message. if specified, other recipients are marked in the headers
	// but not used in the SMTP transport.
	ACTUAL
}