/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * OneWayHash converts a plain text string into an encoded string.
 * </p>
 */
public class OneWayHash
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(OneWayHash.class);

	/**
	 * Encode the clear text into an encoded form.
	 * 
	 * @param clear
	 *        The text to encode.
	 * @param truncated
	 *        return a value truncated as we used to be before fixing SAK-5922 (works only with MD5 algorithm base64'ed)
	 * @return The encoded and base64'ed text.
	 */
	public static String encode(String clear, boolean truncated)
	{
		try
		{
			// compute the digest using the MD5 algorithm
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(clear.getBytes("UTF-8"));

			// encode as base64
			ByteArrayOutputStream bas = new ByteArrayOutputStream(digest.length + digest.length / 3 + 1);
			OutputStream encodedStream = MimeUtility.encode(bas, "base64");
			encodedStream.write(digest);

			String rv;
			if (truncated)
			{
				// we used to pick up the encoding before it was complete, leaving off the last 4 characters of the encoded value
				rv = bas.toString();
				encodedStream.close();
			}
			else
			{
				// close the stream to complete the encoding
				encodedStream.close();
				rv = bas.toString();
			}
			
			// Mimic the logic of DbUserService commit() and trim any whitespace
			// (notably the CR/LF that the JavaMail method may have appended).
			rv = StringUtils.trimToEmpty(rv);

			return rv;
		}
		catch (Exception e)
		{
			M_log.warn("OneWayHash.encode: exception: " + e);
			return null;
		}
	}
}
