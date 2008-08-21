package org.sakaiproject.email.api;

/**
 * Value object for email address properties.  Mimics javax.mail.internet.InternetAddress without
 * requiring a dependency on javax.mail by client code.
 */
public interface EmailAddress
{
	/**
	 * Get the name associated to this email addressee.
	 * 
	 * @return The personal part of this email address.
	 */
	public String getPersonal();

	/**
	 * Get the recipient's email address.
	 * 
	 * @return The email address of the recipient.
	 */
	public String getAddress();
}