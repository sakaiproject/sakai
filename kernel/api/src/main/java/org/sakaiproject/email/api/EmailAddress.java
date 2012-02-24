/**********************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.email.api;

import java.util.ArrayList;
import java.util.List;

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
	private final String address;

	/**
	 * Constructor for the minimum values of this class.
	 *
	 * @param address
	 *            Email address of recipient
	 * @throws IllegalArgumentException If address is null or empty.
	 */
	public EmailAddress(String address)
	{
		this.address = address;
	}

	/**
	 * Constructor for all values of this class.
	 *
	 * @param name
	 *            Personal part of an email address.
	 * @param address
	 *            Actual address of email recipient.
	 * @throws IllegalArgumentException If address is null or empty.
	 * @see org.sakaiproject.email.api.EmailAddress
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

	@Override
	/**
	 * Create a string representation of this email address.
	 *
	 * @return A String that uses following format:<br/>
	 *   if personal part of email is available: "First Last <email@example.com>"<br/>
	 *   if only the address part is available: "
	 */
	public String toString()
	{
		String retval = getAddress();
		if (getPersonal() != null && getPersonal().trim().length() > 0)
		{
			retval = "\"" + getPersonal() + "\" <" + getAddress() + ">";
		}
		return retval;
	}

	/**
	 * Build a list of Strings from an email address list.  Equivalent to iterating the list and
	 * calling toString() on each element.
	 *
	 * @param emails
	 * @return
	 */
	public static List<String> toStringList(List<EmailAddress> emails)
	{
		ArrayList<String> output = new ArrayList<String>();
		if (emails != null && !emails.isEmpty())
		{
			for (EmailAddress ea : emails)
			{
				output.add(ea.toString());
			}
		}
		return output;
	}

	/**
	 * Turn a list of email addresses into a comma delimited String.  The output format of each
	 * entry is: "First Last" <email@address.tld>
	 *
	 * @param emails
	 * @return
	 */
	public static String toString(List<EmailAddress> emails)
	{
		StringBuilder output = new StringBuilder();
		if (emails != null && !emails.isEmpty())
		{
			for (EmailAddress ea : emails)
			{
				output.append(ea.toString()).append(", ");
			}
			// remove the extra trailing comma and space
			output.delete(output.length() - 2, output.length());
		}
		return output.toString();
	}
}