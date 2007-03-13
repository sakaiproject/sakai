/*
 * Created on 13 Mar 2007
 */
package org.sakaiproject.portal.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtils
{

	public static String addParameter(String URL, String name, String value)
	{
		int qpos = URL.indexOf('?');
		int hpos = URL.indexOf('#');
		char sep = qpos == -1 ? '?' : '&';
		String seg = sep + encodeUrl(name) + '=' + encodeUrl(value);
		return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg
				+ URL.substring(hpos);
	}

	/**
	 * The same behaviour as Web.escapeUrl, only without the "funky encoding" of
	 * the characters ? and ; (uses JDK URLEncoder directly).
	 * 
	 * @param toencode
	 *        The string to encode.
	 * @return <code>toencode</code> fully escaped using URL rules.
	 */
	public static String encodeUrl(String url)
	{
		try
		{
			return URLEncoder.encode(url, "UTF-8");
		}
		catch (UnsupportedEncodingException uee)
		{
			throw new IllegalArgumentException(uee);
		}
	}

}
