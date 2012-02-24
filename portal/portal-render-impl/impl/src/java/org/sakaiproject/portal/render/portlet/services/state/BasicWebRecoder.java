/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.render.portlet.services.state;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * WebRecoder which uses basic url encoding (escaped) values to perform the
 * encoding. Encoded values will not be obfuscated, but will be safe.
 * 
 * @since Sakai 2.2.4
 * @version $Rev$
 */
public class BasicWebRecoder implements WebRecoder
{

	/* Encoding */
	private static final String UTF8 = "UTF-8";

	public String encode(byte[] bits)
	{
		try
		{
			return URLEncoder.encode(new String(bits), UTF8);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(
					"UFT-8 is not supported? Should never happen.");
		}
	}

	public byte[] decode(String string)
	{
		try
		{
			return URLDecoder.decode(string, UTF8).getBytes();
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(
					"UFT-8 is not supported? Should never happen.");
		}
	}
}
