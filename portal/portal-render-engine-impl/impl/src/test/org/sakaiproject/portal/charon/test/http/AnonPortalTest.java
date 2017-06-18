/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.test.http;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */

@Slf4j
public class AnonPortalTest extends TestCase
{
	private static final String TEST_URL = "http://localhost:8080/library/js/headscripts.js";

	private static final String BASE_URL = "http://localhost:8080/";

	private static final String[] seedURLs = { "/portal", "/portal/worksite", "/portal/gallery" };

	private static final String ADMIN_USER = "admin";

	private static final String ADMIN_PASSWORD = "admin";

	private static final ActionHandler loginHandler = new LoginActionHandler(ADMIN_USER,ADMIN_PASSWORD);

	private static final ActionHandler[] actionHandlers = {
		loginHandler
	};

	ServletUnitClient client = null;

	private WebConversation wc;

	private boolean enabled = true;

	private byte[] buffer;

	private Map<String, String> visited;



	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		try
		{
			wc = new WebConversation();
			WebRequest req = new GetMethodWebRequest(TEST_URL);
			WebResponse resp = wc.getResponse(req);
			DataInputStream inputStream = new DataInputStream(resp.getInputStream());
			buffer = new byte[resp.getContentLength()];
			inputStream.readFully(buffer);
			visited = new HashMap<String, String>();
		}
		catch (Exception notfound)
		{
			enabled = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testSinglePage()
	{
		if (enabled)
		{
			log.info("Single Page Test Enabled");
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testFrontPage() throws Exception
	{
		if (enabled)
		{
			WebResponse resp = null;
			try
			{
				for (String seed : seedURLs)
				{
					log.info("Testing Seed URL "+seed);
					WebRequest req = new GetMethodWebRequest(BASE_URL + seed);
					resp = wc.getResponse(req);
					clickAll(resp, 0, 10);
				}

			}
			catch (HttpException hex)
			{
				log.error("Failed with ", hex);
				fail(hex.getMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @param resp
	 * @param i
	 * @throws SAXException
	 * @throws IOException
	 */
	private void clickAll(WebResponse resp, int i, int maxDepth) throws SAXException,
			IOException
	{

		if (i > maxDepth) return;
		processLinks(resp,i+1,maxDepth);
		
		WebForm[] formst = resp.getForms();
		log.info("Found "+formst.length+" forms");
		for ( WebForm f : formst ) {
			log.info("Processing "+f.getMethod()+" "+f.getAction());
			if ( "post".equals(f.getMethod()) ) {
				WebResponse formResp = performAction(f);
				if ( formResp != null ) {
					processLinks(formResp,i+1,maxDepth);
				}
			}
		}
	}
		/**
	 * @param resp
	 * @param i
	 * @param maxDepth
		 * @throws SAXException 
		 * @throws IOException 
	 */
	private void processLinks(WebResponse resp, int i, int maxDepth) throws SAXException, IOException
	{
		char[] p = new char[i];
		for (int j = 0; j < i; j++)
		{
			p[j] = ' ';
		}
		String pad = new String(p);
		WebLink[] links = resp.getLinks();
		for (WebLink link : links)
		{
			String url = link.getURLString();
			if (visited.get(url) == null)
			{
				visited.put(url, url);
				if (isLocal(url))
				{
					log.info("Getting " + pad + link.getURLString());
					WebResponse response = null;
					try
					{

						response = link.click();
					}
					catch (HttpException ex)
					{
						log.warn("Failed to get Link " + url + " code:"
								+ ex.getResponseCode() + " cause:"
								+ ex.getResponseMessage());
					}
					catch (ConnectException cex)
					{
						log.error("Failed to get Connection to "+url);
					}
					if (response != null)
					{
						clickAll(response, i, maxDepth);
					}

				}
				else
				{
					log.info("Ignoring External URL " + url);
				}
			}
		}
	}

	/**
	 * @param url
	 * @return
	 */
	private boolean isLocal(String url)
	{
		return url.startsWith("/")
		|| (url.indexOf("//") > 0 && url.startsWith(BASE_URL));
	}

	/**
	 * @param f
	 * @throws SAXException 
	 * @throws IOException 
	 */
	private WebResponse performAction(WebForm f) throws IOException, SAXException
	{
		String action = f.getAction();
		
		if ( isLocal(action) ) {
			action = getLocalUrl(action);
			for( ActionHandler ah : actionHandlers ) {
				WebResponse resp = ah.post(wc,f,action);
				if ( resp !=  null ) {
					return resp;
				}
			}
		}
		return null;
		
	}

	/**
	 * @param action
	 * @return
	 */
	private String getLocalUrl(String action)
	{
		if ( action.startsWith(BASE_URL) ) {
			action = action.substring(BASE_URL.length());
			if ( action.charAt(0) != '/' ) {
				return "/"+action;
			}
		}
		return action;
	}

}
