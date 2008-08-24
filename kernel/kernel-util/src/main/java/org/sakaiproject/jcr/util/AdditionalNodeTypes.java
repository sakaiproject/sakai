/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jcr.api.JCRRegistrationService;

/**
 * This can be declared in a Sakai Service Component registration to declare
 * nodetypes necessary for it's data. To use this component in a Spring config
 * file use
 * 
 * <pre>
 *  &lt;bean class=&quot;org.sakaiproject.jcr.util.AdditionalNodeTypes&quot; init-method=&quot;init&quot;&gt;
 *   &lt;property name=&quot;jcrRegistrationService&quot; ref=&quot;org.sakaiproject.jcr.api.JCRRegistrationService&quot; /&gt;
 *   &lt;property name=&quot;nodetypeResources&quot;&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;org/sakaiproject/mailarchive/api/EmailNodeTypes.xml&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Steve Githens
 */
public class AdditionalNodeTypes
{
	private static final Log log = LogFactory.getLog(AdditionalNodeTypes.class);

	private JCRRegistrationService jcrRegService;

	public void setJcrRegistrationService(JCRRegistrationService service)
	{
		jcrRegService = service;
	}

	private String[] nodetypeResources = new String[] {};

	public void setNodetypeResources(String[] res)
	{
		nodetypeResources = res;
	}

	private Map<String, String> namespaces = new HashMap<String, String>();

	public void setNamespaces(Map<String, String> additionalNamespaces)
	{
		namespaces = additionalNamespaces;
	}

	public void init()
	{
		log.info("init()");
		try
		{
			for (Iterator<String> i = namespaces.keySet().iterator(); i.hasNext();)
			{
				String prefix = i.next();
				String uri = namespaces.get(prefix);
				log.info("Registering Namespace: " + prefix + ", " + uri);
				jcrRegService.registerNamespace(prefix, uri);
			}

			for (int i = 0; i < nodetypeResources.length; i++)
			{
				log.info("Registering NodeTypes from file: " + nodetypeResources[i]);

				InputStream is = this.getClass().getClassLoader().getResourceAsStream(nodetypeResources[i]);
                jcrRegService.registerNodetypes(is);
			}

		}
		catch (Exception e)
		{
			log.warn("Error Registering Additional JCR NameSpaces/Nodetypes", e);
		}
	}
}
