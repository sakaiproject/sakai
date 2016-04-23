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

package org.sakaiproject.james;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

/**
 * <p>
 * JamesServlet starts James.
 * </p>
 */
public class JamesServlet extends HttpServlet
{
	/** Our log (commons). */
	private static Logger M_log = LoggerFactory.getLogger(JamesServlet.class);

	/** config variable and system property for the james / phoenix home. */
	private final static String PHOENIX_HOME = "phoenix.home";

	/** The james / phoenix home value. */
	protected String m_phoenixHome = null;

	/** The JamesRunner (Thread). */
	protected JamesRunner m_runner = null;

	/** James thread */
	public class JamesRunner extends Thread
	{
		/**
		 * construct and start the init activity
		 */
		public JamesRunner()
		{
			start();
		}

		/**
		 * Run the James thread.
		 */
		public void run()
		{
			System.setProperty(PHOENIX_HOME, m_phoenixHome);

			M_log.info("run: starting James service");

			// start James / Avalon running in this VM.
			try
			{
				// Set the log directory for the phoenix log
				String[] args = new String[2];
				args[0] = "-l";
				args[1] = JamesServlet.getLogDirectory() + "phoenix.log";

				int exitCode = PhoenixLauncherMain.startup(args, new HashMap(), true);
				M_log.info("run: James service stopped: " + exitCode);
			}
			catch (Throwable e)
			{
				M_log.warn("run: exception:", e);
			}
		}
	}

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai James Servlet";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		startJames(config);
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		if (m_runner == null) return;

		PhoenixLauncherMain.shutdown();

		m_runner = null;

		super.destroy();
	}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // do not return anything - SAK-23222
    }
    
	protected static String getLogDirectory()
	{
		String logDir = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.logdir"));

		if(logDir == null) {
			M_log.info("init(): smtp.logdir not set, defaulting to {sakai.home}/logs/");
			logDir = System.getProperty("sakai.home");
			if(!logDir.endsWith("/")) {
				logDir += "/";
			}
			logDir += "logs/";
		} else {
			if(!logDir.endsWith("/")) {
				logDir += "/";
			}

			if(!logDir.startsWith("/")) {
				// if the path is relative work from catalina.base/catalina.home
				String catalinaDir = System.getProperty("catalina.base");
				if(catalinaDir == null) {
					catalinaDir = System.getProperty("catalina.home");
				}

				if(!catalinaDir.endsWith("/")) {
					catalinaDir += "/";
				}

				logDir = catalinaDir + logDir;
			}
		}

		M_log.debug("init(): using " + logDir + " as James log directory");
		return logDir;
	}


	public class JamesConfigurationException extends Exception {}


	protected void startJames(ServletConfig config)
	{
		// get config info
		String logDir = JamesServlet.getLogDirectory();
		String host = ServerConfigurationService.getServerName();
		String dns1 = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.dns.1"));
		String dns2 = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.dns.2"));
		String smtpPort = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.port"));
		boolean enabled = ServerConfigurationService.getBoolean("smtp.enabled", false);
		
		String postmasterAddress = null;
		String postmasterLocalPart = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.postmaster.address.local-part"));
		String postmasterDomain = StringUtils.trimToNull(ServerConfigurationService.getString("smtp.postmaster.address.domain"));
		if (postmasterDomain != null) {
			if (postmasterLocalPart == null) {
				postmasterLocalPart = "postmaster";
			}
			postmasterAddress = postmasterLocalPart + "@" + postmasterDomain;
			try {
				InternetAddress email = new InternetAddress(postmasterAddress);
				email.validate();
			} catch(Exception ex) {
				M_log.warn("init(): '" + postmasterAddress + "' is not valid");
				postmasterAddress = null;
			}
		}

		// check for missing values
		if(host == null) host = "127.0.0.1";
		if(smtpPort == null) smtpPort = "25";
    
		M_log.debug("init(): host: " + host + " enabled: " + enabled + " dns1: " + dns1 + " dns2: "
			    + dns2 + " smtp.port: " + smtpPort + " logdir: " + logDir);

		// if not enabled, don't start james
		if (!enabled)
		{
			M_log.debug("init(): James not enabled, aborting");
			return;
		}

		// set the home for james / phoenix, as configured
		String homeRelative = config.getInitParameter(PHOENIX_HOME);
		if (homeRelative == null)
		{
			// or pointing to the webapps root if not configured
			homeRelative = "";
		}

		// expand to real path
		m_phoenixHome = getServletContext().getRealPath(homeRelative);

		try {
			customizeConfig(host, dns1, dns2, smtpPort, logDir, postmasterAddress);
		} catch(JamesConfigurationException e) {
			M_log.error("init(): James could not be configured, aborting");
			return;
		}

		// start the James thread
		m_runner = new JamesRunner();
	}

	protected void customizeConfig(String host, String dns1, String dns2, String smtpPort, String logDir, String postmasterAddress)
	    throws JamesConfigurationException
	{
		String configPath = m_phoenixHome + "/apps/james/SAR-INF/config.xml";
		String environmentPath = m_phoenixHome + "/apps/james/SAR-INF/environment.xml";

		XPath xpath = XPathFactory.newInstance().newXPath();
		Document doc;

		try {
			// process config.xml first
			doc = Xml.readDocument(configPath);
			if(doc == null) {
				M_log.error("init(): James config file " + configPath + "could not be found.");
				throw new JamesConfigurationException();
			}
			
			if (postmasterAddress == null) {
				postmasterAddress = "postmaster@" + host;
			}

			// build a hashmap of node paths and values to set
			HashMap<String, String> nodeValues = new HashMap<String, String>();

			// WARNING!! in XPath, node-set indexes begin with 1, and NOT 0
			nodeValues.put("/config/James/servernames/servername[1]", host);
			nodeValues.put("/config/dnsserver/servers/server[2]", dns1);
			nodeValues.put("/config/dnsserver/servers/server[3]", dns2);
			nodeValues.put("/config/James/postmaster", postmasterAddress);
			nodeValues.put("/config/smtpserver/port", smtpPort);

			// loop through the hashmap, setting each value, or failing if one can't be found
			for(String nodePath : nodeValues.keySet()) {
				if(!(Boolean)xpath.evaluate(nodePath, doc, XPathConstants.BOOLEAN)) {
					if(nodePath.startsWith("/config/dnsserver/servers/server")) {
						// add node (only if we're dealing with DNS server entries)
						Element element = doc.createElement("server");
						element.appendChild(doc.createTextNode( nodeValues.get(nodePath) ));
						Node parentNode = (Node) xpath.evaluate("/config/dnsserver/servers", doc, XPathConstants.NODE);
						parentNode.appendChild(element);
						
					}else{
						// else, throw an exception
						throw new JamesConfigurationException();
					}
					
				}else{
					// change existing node (if value != null else remove it)
					Node node = (Node) xpath.evaluate(nodePath, doc, XPathConstants.NODE);
					if(nodeValues.get(nodePath) != null){
						node.setTextContent(nodeValues.get(nodePath));
					}else{
						node.getParentNode().removeChild(node);
					}
				}
			}

			M_log.debug("init(): writing James configuration to " + configPath);
			Xml.writeDocument(doc, configPath);


			// now handle environment.xml
			doc = Xml.readDocument(environmentPath);
			if(doc == null) {
				M_log.error("init(): James config file " + environmentPath + "could not be found.");
				throw new JamesConfigurationException();
			}

			String nodePath = "/server/logs/targets/file/filename";
			String nodeValue = logDir + "james";

			if(!(Boolean)xpath.evaluate(nodePath, doc, XPathConstants.BOOLEAN)) {
				M_log.error("init(): Could not find XPath '" + nodePath + "' in " + environmentPath + ".");
				throw new JamesConfigurationException();
			}
			((Node)xpath.evaluate(nodePath, doc, XPathConstants.NODE)).setTextContent(nodeValue);

			M_log.debug("init(): writing James configuration to " + environmentPath);
			Xml.writeDocument(doc, environmentPath);
		} catch(JamesConfigurationException e) {
			throw e;
		} catch(Exception e) {
			M_log.warn("init(): An unhandled exception was encountered while configuring James: " + e.getMessage());
		}
	}

}
