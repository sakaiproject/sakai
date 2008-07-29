package org.sakaiproject.email.api;

/**
 * Common email header constants. These are defined in an interface as Strings because using an enum
 * would have lost the use of dashes and created the need for more verbose enum code resulting in
 * the same effect as this interface.
 * 
 * @author Carl Hall (carl.hall@et.gatech.edu)
 */
public interface EmailHeaders
{
	String FROM = "From";
	String TO = "To";
	String CC = "Cc";
	String SUBJECT = "Subject";
	String DATE = "Date";
	String CONTENT_TYPE = "Content-Type";
	String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
	String REPLY_TO = "Reply-To";
	String REFERENCES = "References";
	String IN_REPLY_TO = "In-Reply-To";
	String LIST_ID = "List-Id";
	String MESSAGE_ID = "Message-Id";
}