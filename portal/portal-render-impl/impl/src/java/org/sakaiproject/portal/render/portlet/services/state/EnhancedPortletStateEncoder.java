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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced version of the PortletStateEncoder. This implementation translates
 * the PortletState into a properties file format for serialization. This
 * results in a url parameters which is significatly shorter than ther simple
 * version. This may be desirable in a cases where the url query string is
 * allready significant and may surpass the limit of 2083 characters imposed by
 * some browsers.
 * 
 * @version $Id: EnhancedPortletStateEncoder.java 21641 2007-02-15 23:38:43Z
 *          csev@umich.edu $
 * @since 2.2.3
 */
@Slf4j
public class EnhancedPortletStateEncoder implements PortletStateEncoder
{
	//
	// Implementation Note:
	// - Map Keys below are qualified in order to prevent
	// conflicts with portlet parameters
	// - Keys have been abbreviated to shorten the query
	// string parameter resulting
	//

	private static final String ID_PARAM = "o.s.p.r.s.ID";

	private static final String ACTION_PARAM = "o.s.p.r.s.ACTION";

	private static final String SECURE_PARAM = "o.s.p.r.s.SECURE";

	private static final String PORTLET_MODE_PARAM = "o.s.p.r.s.PORTLET_MODE";

	private static final String WINDOW_STATE_PARAM = "o.s.p.r.s.WINDOW_STATE";

	private WebRecoder urlSafeEncoder = new Base64Recoder();

	public WebRecoder getUrlSafeEncoder()
	{
		return urlSafeEncoder;
	}

	public void setUrlSafeEncoder(WebRecoder urlSafeEncoder)
	{
		this.urlSafeEncoder = urlSafeEncoder;
	}

	public String encode(PortletState portletState)
	{
		log.debug("Encoding PortletState [action={}]", portletState.isAction());

		Properties p = new Properties();
		p.setProperty(ID_PARAM, portletState.getId());
		p.setProperty(ACTION_PARAM, String.valueOf(portletState.isAction()));
		p.setProperty(SECURE_PARAM, String.valueOf(portletState.isSecure()));

		p.setProperty(WINDOW_STATE_PARAM, portletState.getWindowState().toString());
		p.setProperty(PORTLET_MODE_PARAM, portletState.getPortletMode().toString());

		Map parms = portletState.getParameters();
		Iterator it = parms.entrySet().iterator();

		String normalParms = "";
		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			Object o = entry.getValue();
			if (o instanceof String)
			{
				p.setProperty("" + entry.getKey(), o.toString());
				normalParms = normalParms + "&" + entry.getKey() + "=" + o.toString();
			}
			else
			{
				String[] vals = (String[]) o;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < vals.length; i++)
				{
					if (vals[i] == null)
					{
						continue;
					}

					if (i > 0)
					{
						sb.append(",");
					}
					sb.append(vals[i]);
				}
				p.setProperty("" + entry.getKey(), sb.toString());
				normalParms = normalParms + "&" + entry.getKey() + "=" + sb.toString();
			}
		}

		log.debug("Encoded PortletState to properties for Tool '{}'.", portletState.getId());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			p.store(out, "Sakai Portlet State");
		}
		catch (IOException e)
		{
			throw new IllegalStateException("This should never occor");
		}

		return urlSafeEncoder.encode(out.toByteArray()); 
		//IEB Removed this, why are we encoding normal params raw when they are just a copy of the encoded ones + normalParms;
	}

	public PortletState decode(String uri)
	{
		byte[] bits = urlSafeEncoder.decode(uri);
		ByteArrayInputStream in = new ByteArrayInputStream(bits);

		Properties p = new Properties();
		try
		{
			p.load(in);
		}
		catch (IOException e)
		{
			throw new IllegalStateException("This should never occor");
		}

		String id = p.getProperty(ID_PARAM);
		PortletState state = new PortletState(id);
		state.setAction(Boolean.valueOf(p.getProperty(ACTION_PARAM)).booleanValue());
		state.setSecure(Boolean.valueOf(p.getProperty(SECURE_PARAM)).booleanValue());
		state.setWindowState(new WindowState(p.getProperty(WINDOW_STATE_PARAM)));
		state.setPortletMode(new PortletMode(p.getProperty(PORTLET_MODE_PARAM)));

		p.remove(ID_PARAM);
		p.remove(ACTION_PARAM);
		p.remove(SECURE_PARAM);
		p.remove(WINDOW_STATE_PARAM);
		p.remove(PORTLET_MODE_PARAM);

		Map map = new HashMap();
		Iterator i = p.entrySet().iterator();
		while (i.hasNext())
		{
			Map.Entry parm = (Map.Entry) i.next();
			String key = parm.getKey().toString();
			String val = parm.getValue().toString();

			StringTokenizer st = new StringTokenizer(val, ",");
			String[] parms = new String[st.countTokens()];
			int j = 0;
			while (st.hasMoreTokens())
			{
				parms[j++] = st.nextToken();
			}
			map.put(key, parms);
		}

		state.setParameters(map);

		log.debug("Decoded PortletState for Tool '{}'", state.getId());

		return state;
	}

}
