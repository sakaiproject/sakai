package org.sakaiproject.email.api;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

/**
 * Value object for email address properties.  Mimics javax.mail.internet.InternetAddress requiring
 * a dependency on javax.mail by client code.
 */
public class EmailAddress
{
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

	// holds the personal part of the email address aka "name"
	private String personal;

	// an address to be used as an email message recipient
	private String address;

	/**
	 * Constructor for the minimum values of this class.
	 * 
	 * @param address
	 *            Email address of recipient
	 */
	public EmailAddress(String address) throws IllegalArgumentException
	{
		if (address == null || address.trim().length() == 0)
			throw new IllegalArgumentException("Email cannot be empty.");

		this.address = address;
	}

	/**
	 * Constructor for all values of this class.
	 * 
	 * @param name
	 *            Personal part of an email address.
	 * @param address
	 *            Actual address of email recipient.
	 */
	public EmailAddress(String address, String name)
	{
		this(address);
		this.personal = name;
	}

	/**
	 * Get the name associated to this email addressee.
	 * 
	 * @return The personal part of this email address.
	 */
	public String getPersonal()
	{
		return personal;
	}

	/**
	 * Get the recipient's email address.
	 * 
	 * @return The email address of the recipient.
	 */
	public String getAddress()
	{
		return address;
	}

	/**
	 * Convenience method to bulk convert from {@link javax.mail.internet.InternetAddress} to
	 * {@link EmailAddress}
	 * 
	 * @param iaddrs
	 * @return
	 */
	public static List<EmailAddress> toEmailAddress(InternetAddress[] iaddrs)
	{
		ArrayList<EmailAddress> eaddrs = null;
		if (iaddrs != null)
		{
			eaddrs = new ArrayList<EmailAddress>(iaddrs.length);
			for (InternetAddress iaddr : iaddrs)
			{
				eaddrs.add(toEmailAddress(iaddr));
			}
		}
		return eaddrs;
	}

	/**
	 * Convenience method to convert from {@link javax.mail.internet.InternetAddress} to
	 * {@link EmailAddress}
	 * 
	 * @param iaddrs
	 * @return
	 */
	public static EmailAddress toEmailAddress(InternetAddress iaddr)
	{
		EmailAddress eaddr = null;
		if (iaddr != null)
		{
			eaddr = new EmailAddress(iaddr.getAddress(), iaddr.getPersonal());
		}
		return eaddr;
	}
}