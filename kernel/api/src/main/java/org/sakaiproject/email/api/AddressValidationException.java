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

public class AddressValidationException extends Exception
{
	private final List<EmailAddress> invalidAddresses;

	/**
	 * Default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	public AddressValidationException(String msg, EmailAddress invalidAddress)
	{
		super(msg);
		invalidAddresses = new ArrayList<EmailAddress>();
		invalidAddresses.add(invalidAddress);
	}

	public AddressValidationException(String msg, Throwable throwable, EmailAddress invalidAddress)
	{
		super(msg, throwable);
		invalidAddresses = new ArrayList<EmailAddress>();
		invalidAddresses.add(invalidAddress);
	}

	public AddressValidationException(String msg, List<EmailAddress> invalidAddresses)
	{
		super(msg);
		this.invalidAddresses = invalidAddresses;
	}

	public AddressValidationException(Throwable throwable, List<EmailAddress> invalidAddresses)
	{
		super(throwable);
		this.invalidAddresses = invalidAddresses;
	}

	public AddressValidationException(String msg, Throwable throwable,
			List<EmailAddress> invalidAddresses)
	{
		super(msg, throwable);
		this.invalidAddresses = invalidAddresses;
	}

	public List<EmailAddress> getInvalidEmailAddresses()
	{
		return invalidAddresses;
	}
}
