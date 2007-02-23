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
