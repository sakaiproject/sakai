/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/***
 This class parses the user agent string and sets javasciptOK and
 * cssOK following the rules described below.  If you want to check
 * for specific browsers/versions then use this class to parse the
 * user agent string and use the accessor methods in this class.
 *
 * JavaScriptOK means that the browser understands JavaScript on the
 * same level the Navigator 3 does.  Specifically, it can use named
 * images.  This allows easier rollovers.  If a browser doesn't do
 * this (Nav 2 or MSIE 3), then we just assume it can't do any
 * JavaScript.  Referencing images by load order is too hard to
 * maintain.
 *
 * CSSOK is kind of sketchy in that Nav 4 and MSIE work differently,
 * but they do seem to have most of the functionality.  MSIE 4 for the
 * Mac has buggy CSS support, so we let it do JavaScript, but no CSS.
 *
 * Ported from Leon's PHP code at
 * http://www.working-dogs.com/freetrade by Frank.
 *
 * @author <a href="mailto:frank.kim@clearink.com">Frank Y. Kim</a>
 * @author <a href="mailto:leon@clearink.com">Leon Atkisnon</a>
 * @author <a href="mailto:mospaw@polk-county.com">Chris Mospaw</a>
 * @author <a href="mailto:bgriffin@cddb.com">Benjamin Elijah Griffin</a>
 * @version $Id$
 */

package org.sakaiproject.portal.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class BrowserDetector
{
	public static final String MSIE = "MSIE";

	public static final String OPERA = "Opera";

	public static final String MOZILLA = "Mozilla";

	public static final String WINDOWS = "Windows";

	public static final String UNIX = "Unix";

	public static final String MACINTOSH = "Macintosh";

	private static final Log log = LogFactory.getLog(BrowserDetector.class);

	private String userAgentString = "";

	private String browserName = "";

	private float browserVersion = (float) 1.0;

	private String browserPlatform = "unknown";

	private boolean javascriptOK = false;

	private boolean cssOK = false;

	private boolean fileUploadOK = false;

	public BrowserDetector(HttpServletRequest request)
	{
		super();
		if (request != null)
		{
			this.userAgentString = request.getHeader("user-agent");
		}
		try {
			parse();
		} catch ( Exception ex ) {
			log.debug("Unable to detect browser");
		}
	}

	/**
	 * @param string
	 */
	public BrowserDetector(String userAgentString)
	{
		this.userAgentString = userAgentString;
	}

	public boolean isCssOK()
	{
		return cssOK;
	}

	public boolean isFileUploadOK()
	{
		return fileUploadOK;
	}

	public boolean isJavascriptOK()
	{
		return javascriptOK;
	}

	public String getBrowserName()
	{
		return browserName;
	}

	public String getBrowserPlatform()
	{
		return browserPlatform;
	}

	public float getBrowserVersion()
	{
		return browserVersion;
	}

	public String getUserAgentString()
	{
		return userAgentString;
	}

	private void parse()
	{
		if (userAgentString != null && userAgentString.length() > 0)
		{
			int versionStartIndex = userAgentString.indexOf("/");
			int versionEndIndex = userAgentString.indexOf(" ");
			browserName = userAgentString.substring(0, versionStartIndex);
			try
			{
				String agentSubstring = null;
				if (versionEndIndex < 0)
				{
					agentSubstring = userAgentString.substring(versionStartIndex + 1);
				}
				else
				{
					agentSubstring = userAgentString.substring(versionStartIndex + 1,
							versionEndIndex);
				}
				browserVersion = toFloat(agentSubstring);
			}
			catch (NumberFormatException e)
			{
			}
			if (userAgentString.indexOf(MSIE) != -1)
			{
				versionStartIndex = (userAgentString.indexOf(MSIE) + MSIE.length() + 1);
				versionEndIndex = userAgentString.indexOf(";", versionStartIndex);
				browserName = MSIE;
				try
				{
					browserVersion = toFloat(userAgentString.substring(versionStartIndex,
							versionEndIndex));
				}
				catch (NumberFormatException e)
				{
				}
			}
			if (userAgentString.indexOf(OPERA) != -1)
			{
				versionStartIndex = (userAgentString.indexOf(OPERA) + OPERA.length() + 1);
				versionEndIndex = userAgentString.indexOf(" ", versionStartIndex);
				browserName = OPERA;
				try
				{
					browserVersion = toFloat(userAgentString.substring(versionStartIndex,
							versionEndIndex));
				}
				catch (NumberFormatException e)
				{
				}
			}
			if ((userAgentString.indexOf("Windows") != -1)
					|| (userAgentString.indexOf("WinNT") != -1)
					|| (userAgentString.indexOf("Win98") != -1)
					|| (userAgentString.indexOf("Win95") != -1))
			{
				browserPlatform = WINDOWS;
			}
			if (userAgentString.indexOf("Mac") != -1)
			{
				browserPlatform = MACINTOSH;
			}
			if (userAgentString.indexOf("X11") != -1)
			{
				browserPlatform = UNIX;
			}
			if (browserPlatform == WINDOWS)
			{
				if (browserName.equals(MOZILLA))
				{
					if (browserVersion >= 3.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
					}
					if (browserVersion >= 4.0)
					{
						cssOK = true;
					}
				}
				else if (browserName == MSIE)
				{
					if (browserVersion >= 4.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
						cssOK = true;
					}
				}
				else if (browserName == OPERA)
				{
					if (browserVersion >= 3.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
						cssOK = true;
					}
				}
			}
			else if (browserPlatform == MACINTOSH)
			{
				if (browserName.equals(MOZILLA))
				{
					if (browserVersion >= 3.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
					}
					if (browserVersion >= 4.0)
					{
						cssOK = true;
					}
				}
				else if (browserName == MSIE)
				{
					if (browserVersion >= 4.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
					}
					if (browserVersion > 4.0)
					{
						cssOK = true;
					}
				}
			}
			else if (browserPlatform == UNIX)
			{
				if (browserName.equals(MOZILLA))
				{
					if (browserVersion >= 3.0)
					{
						javascriptOK = true;
						fileUploadOK = true;
					}
					if (browserVersion >= 4.0)
					{
						cssOK = true;
					}
				}
			}
		}
	}

	private static final float toFloat(String s)
	{
		return Float.valueOf(s).floatValue();
	}
}
