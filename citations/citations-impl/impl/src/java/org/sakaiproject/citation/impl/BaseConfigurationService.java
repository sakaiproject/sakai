/**********************************************************************************
 * $URL:  $
 * $Id:   $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.citation.impl;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.citation.api.ConfigurationService;
import org.sakaiproject.citation.api.SiteOsidConfiguration;
import org.sakaiproject.citation.util.api.OsidConfigurationException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.xstream.XStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Accessors(prefix = "m_" )
public class BaseConfigurationService implements ConfigurationService, Observer
{
  /**
   * Locale that will be used if user's locale is not available. 
   */
  private static final String SERVER_DEFAULT_LOCALE = Locale.ENGLISH.getLanguage();
  
  public static final String DEFAULT_SECONDS_BETWEEN_SAVECITE_REFRESHES = "5";
  public static final int MAXIMUM_SECONDS_BETWEEN_SAVECITE_REFRESHES = 30;
  public static final String PARAM_SECONDS_BETWEEN_SAVECITE_REFRESHES = "secondsBetweenSaveciteRefreshes";

  /*
   * All the following properties will be set by Spring using components.xml
   */
  // enable/disable entire helper
    protected boolean m_citationsEnabledByDefault = false;
    protected boolean m_allowSiteBySiteOverride = false;

  // enable/disable helper features -->
    protected String m_googleSearchEnabled = "false";
    protected String m_librarySearchEnabled = "false";
    protected boolean m_externalSearchEnabled = false;

    protected String m_adminSiteName = "citationsAdmin";
    protected String m_configFolder = "config";
    protected String m_configXml = "sakai/citationsConfig.xml";
    protected String m_categoriesXml = "sakai/databaseHierarchy.xml";

    protected SortedSet<String> m_updatableResources = Collections.synchronizedSortedSet(new TreeSet<String>());

  // configuration XML file location
    protected String m_databaseXml;
    protected String m_siteConfigXml;

    // metasearch engine parameters
    protected String m_metasearchUsername;
    protected String m_metasearchPassword;
    protected String m_metasearchBaseUrl;

  // extended repository id (optional, leave as null if not used)
    protected String m_extendedRepositoryId;

  // openURL parameters -->
    protected String m_openUrlLabel;
    protected String m_openUrlResolverAddress;

  // google scholar parameters -->
    protected String m_googleBaseUrl;
    protected String m_sakaiServerKey;
    protected String m_externalSearchUrl;

  // site-specific config/authentication/authorization implementation -->
    protected String m_osidConfig;
  /*
   * End of components.xml properties
   */

  // other config services -->
  @Setter @Getter protected SessionManager m_sessionManager;
  @Setter @Getter protected ServerConfigurationService m_serverConfigurationService;
  @Setter private SecurityService m_securityService;
  @Setter private EntityManager m_entityManager;
  @Setter private EventTrackingService m_eventTrackingService;
  
  private TreeSet<String> m_categories;
  private TreeSet<String> m_configs;

  /*
   * Site specific OSID configuration instance
   */
  private static SiteOsidConfiguration m_siteConfigInstance = null;

  /*
   * Dynamic configuration parameters
   */
  protected static Map<String, Map<String,String>> m_configMaps = new HashMap<String, Map<String,String>>();
  protected static String m_configListRef = null;
  
  /**
   * 
   */
  protected Map<String, List<Map<String, String>>> saveciteClients = new HashMap<String, List<Map<String,String>>>();

  /*
   * Interface methods
   */

  /**
   * Fetch the appropriate XML configuration document for this user
   * @return Configuration XML resource name
   */
  public String getConfigurationXml() throws OsidConfigurationException
  {
    SiteOsidConfiguration siteConfig  = getSiteOsidConfiguration();
    String                configXml   = null;

    if (siteConfig != null)
    {
      configXml = siteConfig.getConfigurationXml();
    }

    if (isNull(configXml))
    {
      configXml = m_siteConfigXml;
    }
    return configXml;
  }

  /**
   * Is the configuration XML file provided and readable
   * @return true If the XML file is provided and readable, false otherwise
   */
  public boolean isConfigurationXmlAvailable()
  {
    try
    {
      String configXml = getConfigurationXml();

      if (configXml == null)
      {
        return false;
      }
      return exists(configXml);
    }
    catch (OsidConfigurationException exception)
    {
      log.warn("Unexpected exception: " + exception);
    }
    return false;
  }

  public String getConfigFolderReference()
    {
      String configFolderRef = null;
    if(! isNull(this.m_adminSiteName) && ! isNull(this.m_configFolder))
    {
      configFolderRef = "/content/group/" + this.m_adminSiteName + "/" + this.m_configFolder + "/";
    }
    return configFolderRef;

    }

  public String getConfigFolderId()
  {
    String configFolderId = null;
    if(! isNull(this.m_adminSiteName) && ! isNull(this.m_configFolder))
    {
      configFolderId = "/group/" + this.m_adminSiteName + "/" + this.m_configFolder + "/";
    }
    return configFolderId;
  }

  /**
   * Fetch the appropriate XML database hierarchy document for this user
   * @return Hierarchy XML resource name
   */
  public String getDatabaseHierarchyXml() throws OsidConfigurationException
  {
    SiteOsidConfiguration   siteConfig  = getSiteOsidConfiguration();
    String                  databaseXml = null;

    if (siteConfig != null)
    {
      databaseXml = siteConfig.getDatabaseHierarchyXml();
    }

    if (isNull(databaseXml))
    {
      databaseXml = m_databaseXml;
    }
    return databaseXml;
  }

  /**
   * Is the database hierarchy XML file provided and readable
   * @return true If the XML file is provided and readable, false otherwise
   */
  public boolean isDatabaseHierarchyXmlAvailable()
  {
    try
    {
      String dbXml = getDatabaseHierarchyXml();

      if( dbXml == null )
      {
        return false;
      }
      return exists(dbXml);
    }
    catch (OsidConfigurationException exception)
    {
      log.warn("Unexpected exception: " + exception);
    }
    return false;
  }

  /**
   * Fetch this user's group affiliations
   * @return A list of group IDs (empty if no IDs exist)
   */
  public List<String> getGroupIds() throws OsidConfigurationException
  {
    SiteOsidConfiguration   siteConfig  = getSiteOsidConfiguration();

    if (siteConfig == null)
    {
      ArrayList<String> emptyList = new ArrayList();

      return emptyList;
    }
    return siteConfig.getGroupIds();
  }

  /**
   * Fetch the site specific extended Repository ID
   * @return The Repository ID
   */
  public synchronized String getSiteConfigExtendedRepositoryId()
  {
    String value = getConfigurationParameter("extended-repository-id");

    return (value != null) ? value : getExtendedRepositoryId();
  }

  /**
   * Fetch the meta-search username
   * @return the username
   */
  public synchronized String getSiteConfigMetasearchUsername()
  {
    String value = getConfigurationParameter("metasearch-username");

    return (value != null) ? value : getMetasearchUsername();
  }

  /**
   * Fetch the meta-search password
   * @return the password
   */
  public synchronized String getSiteConfigMetasearchPassword()
  {
    String value = getConfigurationParameter("metasearch-password");

    return (value != null) ? value : getMetasearchPassword();
  }

  /**
   * Fetch the meta-search base-URL
   * @return the username
   */
  public synchronized String getSiteConfigMetasearchBaseUrl()
  {
    String value = getConfigurationParameter("metasearch-baseurl");

    return (value != null) ? value : getMetasearchBaseUrl();
  }

  /**
   * Fetch the OpenURL label
   * @return the label text
   */
  public synchronized String getSiteConfigOpenUrlLabel()
  {
    String value = getConfigurationParameter("openurl-label");

    return (value != null) ? value : getOpenUrlLabel();
  }

  /**
   * Fetch the OpenURL resolver address
   * @return the resolver address (domain name or IP)
   */
  public synchronized String getSiteConfigOpenUrlResolverAddress()
  {
    String value = getConfigurationParameter("openurl-resolveraddress");

    return (value != null) ? value : getOpenUrlResolverAddress();
  }

  /**
   * Fetch the Google base-URL
   * @return the URL
   */
  public synchronized String getSiteConfigGoogleBaseUrl()
  {
    String value = getConfigurationParameter("google-baseurl");

    return (value != null) ? value : getGoogleBaseUrl();
  }

  /**
   * Fetch the Sakai server key
   * @return the key text
   */
  public synchronized String getSiteConfigSakaiServerKey()
  {
    String value = getConfigurationParameter("sakai-serverkey");

    return (value != null) ? value : getSakaiServerKey();
  }

  /**
   * Get the maximum number of databases we can search at one time
   * @return The maximum value (defaults to <code>SEARCHABLE_DATABASES</code>
   *                            if no other value is specified)
   */
  public synchronized int getSiteConfigMaximumSearchableDBs()
  {
    String  configValue   = getConfigurationParameter("searchable-databases");
    int     searchableDbs = SEARCHABLE_DATABASES;
    /*
     * Supply the default if no value was configured
     */
    if (configValue == null)
    {
      return searchableDbs;
    }
    /*
     * Make sure we have a good value
     */
    try
    {
      searchableDbs = Integer.parseInt(configValue);
      if (searchableDbs <= 0)
      {
        throw new NumberFormatException(configValue);
      }
    }
    catch (NumberFormatException exception)
    {
      if(log.isDebugEnabled()) {
        log.debug("Maximum searchable database exception: "
              +   exception.toString());
      }
      searchableDbs = SEARCHABLE_DATABASES;
    }
    finally
    {
      return searchableDbs;
    }
  }

  /**
   * How should we use URLs marked as "preferred" by the OSID implementation?
   * The choices are:
   *<ul>
   * <li> "false" (do not use the preferred URL at all)
   * <li> "title-link" (provide the preferred URL as the title link)
   * <li> "related-link" (provide as a related link, not as the title link)
   *</ul>
   * Note: "false" is the default value if nothing is specified in the
   *       configuration file
   *
   * @return "related-link", "title-link", or "false"
   */
  public synchronized String getSiteConfigUsePreferredUrls()
  {
    String value = getConfigurationParameter("provide-direct-url");

    if (value == null)
    {
      return "false";
    }

    value = value.trim().toLowerCase();

    if (!(value.equals("false") ||
          value.equals("related-link") ||
          value.equals("title-link")))
    {
      log.debug("Invalid value for <provide-direct-url>: \""
                  + value
                  + "\", using \"false\"");
      value = "false";
    }
    return value;
  }

  /**
   * Prefix string for "preferred" URLs (when used as title or related links).
   *
   * This is likely to be the proxy information for the direct URL.
   *
   * @return The prefix String (null if none)
   */
  public synchronized String getSiteConfigPreferredUrlPrefix()
  {
    return getConfigurationParameter("direct-url-prefix");
  }

  /**
   * Enable/disable Citations Helper by default
   * @param state true to set default 'On'
   */
  public void setCitationsEnabledByDefault(boolean citationsEnabledByDefault)
  {
    m_citationsEnabledByDefault = citationsEnabledByDefault;
  }

  /**
   * Is the Citations Helper [potentially] available for all sites?
   * @return true if so
   */
  public boolean isCitationsEnabledByDefault()
  {
    String state = getConfigurationParameter("citations-enabled-by-default");

    if (state != null)
    {
      log.debug("Citations enabled by default (1): " + state.equals("true"));
      return state.equals("true");
    }
    log.debug("Citations enabled by default (2): " + m_citationsEnabledByDefault);
    return m_citationsEnabledByDefault;
  }

  /**
   * Enable/disable site by site Citations Helper override
   * @param state true to enable site by site Citations Helper
   */
  public void setAllowSiteBySiteOverride(boolean allowSiteBySiteOverride)
  {
    m_allowSiteBySiteOverride = allowSiteBySiteOverride;
  }

  /**
   * Is the Citations Helper enabled site-by-site?
   * @return true if so
   */
  public boolean isAllowSiteBySiteOverride()
  {
    String state = getConfigurationParameter("citations-enabled-site-by-site");

    if (state != null)
    {
      log.debug("Citations enabled site-by-site (1): " + state.equals("true"));
      return state.equals("true");
    }
    log.debug("Citations enabled site-by-site (2): " + m_allowSiteBySiteOverride);
    return m_allowSiteBySiteOverride;
  }

  /**
   * Enable/disable Google support (no support for site specific XML configuration)
   * @param state true to enable Google support
   */
  public void setGoogleScholarEnabled(boolean state)
  {
    String enabled = state ? "true" : "false";

    setGoogleSearchEnabled(enabled);
  }

  /**
   * Is Google search enabled? (no support for site specific XML configuration)
   * @return true if so
   */
  public boolean isGoogleScholarEnabled()
  {
    String state = getConfigurationParameter("google-scholar-enabled");

    if (state == null)
    {
      state = getGoogleSearchEnabled();
    }
    log.debug("Google enabled: " + state.equals("true"));
    return state.equals("true");
  }

  /**
   * Enable/disable library search support (no support for site specific XML configuration)
   * @param state true to enable support
   */
  public void setLibrarySearchEnabled(boolean state)
  {
    String enabled = state ? "true" : "false";

    setLibrarySearchEnabled(enabled);
  }

  /**
   * Is library search enabled for any users? (no support for site specific XML configuration)
   * @return true if so
   */
  public boolean isLibrarySearchEnabled()
  {
    String state = getConfigurationParameter("library-search-enabled");

    if (state == null)
    {
      state = getLibrarySearchEnabled();
    }
    log.debug("Library Search enabled: " + state.equals("true"));
    return state.equals("true");
  }

  /*
   * Helpers
   */

  /**
   * Get a named value from the site-specific XML configuration file
   * @param parameter Configuration parameter to lookup
   * @return Parameter value (null if none [or error])
   */
  protected String getConfigurationParameter(String parameter)
  {
    Map<String, String> parameterMap  = null;

    try
    {
      SiteOsidConfiguration siteConfig;
      String configXml, configXmlRef;
      /*
       * Fetch the configuration XML resource name
       */
      siteConfig = getSiteOsidConfiguration();
      if (siteConfig == null)
      {
        return null;
      }

      if ((configXml = siteConfig.getConfigurationXml()) == null)
      {
        return null;
      }
      /*
       * Construct the full reference and look up the requested configuration
       */
      configXmlRef = this.getConfigFolderReference() + configXml;
      synchronized (this)
      {
        parameterMap = m_configMaps.get(configXmlRef);
      }
    }
    catch (OsidConfigurationException exception)
    {
      log.warn("Failed to get a dynamic XML value for "
              +  parameter
              +  ": "
              +  exception);
    }
    /*
     * Finally, return the requested configuration parameter
     */
    return (parameterMap == null) ? null : parameterMap.get(parameter);
  }

  /**
   * Load and initialize the site-specific OSID configuration code
   * @return The initialized, site-specific OSID configuration
   *         object (null on error)
   */
  protected SiteOsidConfiguration getSiteOsidConfiguration()
  {
    SiteOsidConfiguration siteConfig;

    try
    {
      siteConfig = getConfigurationHandler(m_osidConfig);
      siteConfig.init();
    }
    catch (Exception exception)
    {
      log.warn("Failed to get " + m_osidConfig + ": " + exception);
      siteConfig = null;
    }
    return siteConfig;
  }

  /**
   * Return a SiteOsidConfiguration instance
   * @return A SiteOsidConfiguration
   */
  public synchronized
         SiteOsidConfiguration getConfigurationHandler(String osidConfigHandler)
                                      throws  java.lang.ClassNotFoundException,
                                              java.lang.InstantiationException,
                                              java.lang.IllegalAccessException
  {
    if (m_siteConfigInstance == null)
    {
      Class configClass = Class.forName(osidConfigHandler);

      m_siteConfigInstance = (SiteOsidConfiguration) configClass.newInstance();
    }
    return m_siteConfigInstance;
  }

  /**
   * Populate cached values from a configuration XML resource.  We always try
   * to parse the resource, regardless of any prior success or failure.
   *
   * @param configurationXml Configuration resource name (this doubles as a
   *                         unique key into the configuration cache)
   */
  public void populateConfig(String configurationXml, InputStream stream)
  {
    org.w3c.dom.Document  document;
    String                value;

    /*
     * Parse the XML - if that fails, give up now
     */
    if ((document = parseXmlFromStream(stream)) == null)
    {
      return;
    }

    synchronized (this)
    {
      Map<String, String> parameterMap;

      /*
       * Successful parse - save the values
       */
      if ((parameterMap = m_configMaps.get(configurationXml)) == null)
      {
        parameterMap = new HashMap<String, String>();
      }
      parameterMap.clear();

      saveParameter(document, parameterMap, "citations-enabled-by-default");
      saveParameter(document, parameterMap, "citations-enabled-site-by-site");

      saveParameter(document, parameterMap, "google-scholar-enabled");
      saveParameter(document, parameterMap, "library-search-enabled");

      saveParameter(document, parameterMap, "osid-impl");
      saveParameter(document, parameterMap, "extended-repository-id");

      saveParameter(document, parameterMap, "metasearch-username");
      saveParameter(document, parameterMap, "metasearch-password");
      saveParameter(document, parameterMap, "metasearch-baseurl");
      saveParameter(document, parameterMap, "metasearch-enabled");

      saveParameter(document, parameterMap, "openurl-label");
      saveParameter(document, parameterMap, "openurl-resolveraddress");

      saveParameter(document, parameterMap, "provide-direct-url");
      saveParameter(document, parameterMap, "direct-url-prefix");

      saveParameter(document, parameterMap, "google-baseurl");
      saveParameter(document, parameterMap, "sakai-serverkey");

      saveParameter(document, parameterMap, "searchable-databases");

      saveParameter(document, parameterMap, "config-id");               // obsolete?
      saveParameter(document, parameterMap, "database-xml");            // obsolete?
      
      saveParameter(document, parameterMap, PARAM_SECONDS_BETWEEN_SAVECITE_REFRESHES);
      
      saveServletClientMappings(document);

      m_configMaps.put(configurationXml, parameterMap);
    }
  }

  protected void saveServletClientMappings(Document document) {
	  
	  Element clientElement = document.getElementById("saveciteClients");
	  
	  if(clientElement == null) {
		  NodeList mapNodes = document.getElementsByTagName("map");
		  if(mapNodes != null) {
			  for(int i = 0; i < mapNodes.getLength(); i++) {
				  Element mapElement = (Element) mapNodes.item(i);
				  if(mapElement.hasAttribute("id") && mapElement.getAttribute("id").equals("saveciteClients")) {
					  clientElement = mapElement;
					  break;
				  }
			  }
		  }
	  }
	  
	  if(clientElement != null) {
		  try {
			  XStream xstream = new XStream();
			  TransformerFactory transFactory = TransformerFactory.newInstance();
			  Transformer transformer = transFactory.newTransformer();
			  StringWriter buffer = new StringWriter();
			  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			  transformer.transform(new DOMSource(clientElement),
			        new StreamResult(buffer));
			  String str = buffer.toString();
//			  DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
//			  LSSerializer serializer = domImplLS.createLSSerializer();
//			  String str = serializer.writeToString(clientElement);
			  this.saveciteClients = (Map<String, List<Map<String, String>>>) xstream.fromXML(str);
		  } catch(Exception e) {
			  log.warn("Exception trying to read saveciteClients from config XML", e);
		  }
	  }
	
  }

  /**
   * Lookup and save one dynamic configuration parameter
   * @param Configuration XML
   * @param parameterMap Parameter name=value pairs
   * @param name Parameter name
   */
  protected void saveParameter(org.w3c.dom.Document document,
                             Map parameterMap, String name)
  {
    String value;

    if ((value = getText(document, name)) != null)
    {
      parameterMap.put(name, value);
    }
  }

  /*
   * XML helpers
   */

  /**
   * Parse an XML resource
   * @param filename The filename (or URI) to parse
   * @return DOM Document (null if parse fails)
   */
  protected Document parseXmlFromStream(InputStream stream)
  {
    try
    {
      DocumentBuilder documentBuilder = getXmlDocumentBuilder();

      if (documentBuilder != null)
      {
        return documentBuilder.parse(stream);
      }
    }
    catch (Exception exception)
    {
      log.warn("XML parse on \"" + stream + "\" failed: " + exception);
    }
    return null;
  }

  /**
   * Get a DOM Document builder.
   * @return The DocumentBuilder
   */
  protected DocumentBuilder getXmlDocumentBuilder()
  {
    try
    {
      DocumentBuilderFactory factory;

      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

      return factory.newDocumentBuilder();
    }
    catch (Exception exception)
    {
      log.warn("Failed to get XML DocumentBuilder: " + exception);
    }
    return null;
  }

  /**
   * "Normalize" XML text node content to create a simple string
   * @param original Original text
   * @param update Text to add to the original string (a space separates the two)
   * @return Concatenated contents (trimmed)
   */
  protected String normalizeText(String original, String update)
  {
    StringBuilder  result;

    if (original == null)
    {
      return (update == null) ? "" : update.trim();
    }

    result = new StringBuilder(original.trim());
    result.append(' ');
    result.append(update.trim());

    return result.toString();
  }

  /**
   * Get the text associated with this element
   * @param root The document containing the text element
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  protected String getText(Document root, String elementName)
  {
    return getText(root.getDocumentElement(), elementName);
  }

  /**
   * Get the text associated with this element
   * @param root The root node of the text element
   * @return Text (trimmed of leading/trailing whitespace, null if none)
   */
  protected String getText(Element root, String elementName)
  {
    NodeList  nodeList;
    Node      parent;
    String    text;

    nodeList = root.getElementsByTagName(elementName);
    if (nodeList.getLength() == 0)
    {
      return null;
    }

    text = null;
    parent = (Element) nodeList.item(0);

    for (Node child = parent.getFirstChild();
              child != null;
              child = child.getNextSibling())
    {
      switch (child.getNodeType())
      {
        case Node.TEXT_NODE:
          text = normalizeText(text, child.getNodeValue());
          break;

        default:
          break;
      }
    }
    return text == null ? text : text.trim();
  }

  /*
   * Inititialize and destroy
   */
  public void init()
  {
    log.info("init()");

    m_eventTrackingService.addObserver(this);

    SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
    ContentHostingService contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);

    log.info("init() site m_adminSiteName == \"" + this.m_adminSiteName + "\"");
    if(isNull(this.m_adminSiteName))
    {
      // can't create
        log.info("init() m_adminSiteName is null");
    }
    else if(siteService.siteExists(this.m_adminSiteName))
    {
      // no need to create
        log.info("init() site " + this.m_adminSiteName + " already exists");
    }
    else if(!m_serverConfigurationService.getBoolean("citationsAdmin.autocreate", false)) {
     	//do not autocreate
     	log.info("init() skipping autocreate of citationsAdmin site");
    }
    else
    {
    	SecurityAdvisor pushed = null;
    	// need to create
    	try
    	{
    		pushed = enableSecurityAdvisor();
    		Session s = m_sessionManager.getCurrentSession();
    		s.setUserId(UserDirectoryService.ADMIN_ID);

			Site adminSite = siteService.addSite(this.m_adminSiteName, "project");
			adminSite.setTitle("Citations Admin");
			adminSite.setPublished(true);
			adminSite.setJoinable(false);

			// add Resources tool
			SitePage page = adminSite.addPage();
			page.setTitle("Resources");
			page.addTool("sakai.resources");

			siteService.save(adminSite);
	        log.debug("init() site " + this.m_adminSiteName + " has been created");
		}
		catch (IdInvalidException e)
		{
		  // TODO Auto-generated catch block
		  log.warn("IdInvalidException ", e);
		}
		catch (IdUsedException e)
		{
		  // we've already verified that the site doesn't exist but
		  // this can occur if site was created by another server
		  // in a cluster that is starting up at the same time.
		  log.warn("IdUsedException ", e);
		}
		catch (PermissionException e)
		{
		  // TODO Auto-generated catch block
		  log.warn("PermissionException ", e);
		}
		catch (IdUnusedException e)
		{
		  // TODO Auto-generated catch block
		  log.warn("IdUnusedException ", e);
		}
		finally {
			if(pushed != null) {
				boolean found = false;
				while(m_securityService.hasAdvisors() && ! found) {
					SecurityAdvisor popped = m_securityService.popAdvisor();
					found = pushed == popped;
				}
			}
		}
	}

	for(String config : this.m_configs)
	{
	  String configFileRef = this.getConfigFolderReference() + config;

	  updateConfig(configFileRef);
	}
  }

  public void destroy()
  {
    log.info("destroy()");
  }

  /*
   * Getters/setters for components.xml parameters
   */

  /**
   * @return the extended Repository ID
   */
  public String getExtendedRepositoryId()
  {
    return m_extendedRepositoryId;
  }

  /**
   * @param extendedRepositoryId the extended Repository ID
   */
  public void setExtendedRepositoryId(String extendedRepositoryId)
  {
    m_extendedRepositoryId = extendedRepositoryId;
  }

  /**
   * @return the m_metasearchUsername
   */
  public String getMetasearchUsername()
  {
    return m_metasearchUsername;
  }

  /**
   * @param username the m_metasearchUsername to set
   */
  public void setMetasearchUsername(String username)
  {
    m_metasearchUsername = username;
  }

  /**
   * @return the m_metasearchBaseUrl
   */
  public String getMetasearchBaseUrl()
  {
    return m_metasearchBaseUrl;
  }

  /**
   * @param baseUrl the m_metasearchBaseUrl to set
   */
  public void setMetasearchBaseUrl(String baseUrl)
  {
    m_metasearchBaseUrl = baseUrl;
  }

  /**
   * @return the m_metasearchPassword
   */
  public String getMetasearchPassword()
  {
    return m_metasearchPassword;
  }

  /**
   * @param password the m_metasearchPassword to set
   */
  public void setMetasearchPassword(String password)
  {
    m_metasearchPassword = password;
  }

  /**
   * @return the Google base URL
   */
  public String getGoogleBaseUrl()
  {
    return m_googleBaseUrl;
  }

  /**
   * @param googleBaseUrl the base URL to set
   */
  public void setGoogleBaseUrl(String googleBaseUrl)
  {
    m_googleBaseUrl = googleBaseUrl;
  }

  /**
   * @return the sakaiServerKey
   */
  public String getSakaiServerKey()
  {
    return m_sakaiServerKey;
  }

  /**
   * @param sakaiServerKey the sakaiServerKey to set
   */
  public void setSakaiServerKey(String sakaiId)
  {
    m_sakaiServerKey = sakaiId;
  }

  /**
   * @return the OpenURL label
   */
  public String getOpenUrlLabel()
  {
    return m_openUrlLabel;
  }

  /**
   * @param set the OpenURL label
   */
  public void setOpenUrlLabel(String openUrlLabel)
  {
    m_openUrlLabel = openUrlLabel;
  }

  /**
   * @return the OpenURL resolver address
   */
  public String getOpenUrlResolverAddress()
  {
    return m_openUrlResolverAddress;
  }

  /**
   * @param set the OpenURL resolver address
   */
  public void setOpenUrlResolverAddress(String openUrlResolverAddress)
  {
    m_openUrlResolverAddress = openUrlResolverAddress;
  }

  /**
   * @return the database hierarchy XML filename/URI
   */
  public String getDatabaseXml()
  {
    return m_databaseXml;
  }

  /**
   * @param set the database hierarchy XML filename/URI
   */
  public void setDatabaseXml(String databaseXml)
  {
    m_databaseXml = databaseXml;
  }

  /**
   * @return the configuration XML filename/URI
   */
  public String getSiteConfigXml()
  {
    return m_siteConfigXml;
  }

  /**
   * @param set the configuration XML filename/URI
   */
  public void setSiteConfigXml(String siteConfigXml)
  {
    m_siteConfigXml = siteConfigXml;
  }



  /**
   * @return the site specific "OSID configuration" package name
   */
  public String getOsidConfig()
  {
    return m_osidConfig;
  }

  /**
   * @param osidConfig the site specific "OSID configuration" package name
   */
  public void setOsidConfig(String osidConfig)
  {
    m_osidConfig = osidConfig;
  }

  /**
   * @return Google search support status
   */
  public String getGoogleSearchEnabled()
  {
    return m_googleSearchEnabled;
  }

  /**
   * @param googleSearchEnabled ("true" or "false")
   */
  public void setGoogleSearchEnabled(String googleSearchEnabled)
  {
    if (googleSearchEnabled.equalsIgnoreCase("true") ||
        googleSearchEnabled.equalsIgnoreCase("false"))
    {
      m_googleSearchEnabled = googleSearchEnabled;
      return;
    }

    log.warn("Invalid Google support setting \""
          +    googleSearchEnabled
          +    "\", disabling Google search");

    m_googleSearchEnabled = "false";
  }

  /**
   * @return library search support status
   */
  public String getLibrarySearchEnabled()
  {
    return m_librarySearchEnabled;
  }

  /**
   * @param librarySearchEnabled ("true" or "false")
   */
  public void setLibrarySearchEnabled(String librarySearchEnabled)
  {
    if (librarySearchEnabled.equalsIgnoreCase("true") ||
        librarySearchEnabled.equalsIgnoreCase("false"))
    {
      m_librarySearchEnabled = librarySearchEnabled;
      return;
    }

    log.warn("Invalid library support setting \""
          +    librarySearchEnabled
          +    "\", disabling library search");

    m_librarySearchEnabled = "false";
  }

  /**
   * @return the adminSiteName
   */
  public String getAdminSiteName()
  {
    return m_adminSiteName;
  }

  /**
   * @param adminSiteName the adminSiteName to set
   */
  public void setAdminSiteName(String adminSiteName)
  {
    this.m_adminSiteName = adminSiteName;
  }

  /**
   * @return the configFolder
   */
  public String getConfigFolder()
  {
    return m_configFolder;
  }

  /**
   * @param configFolder the configFolder to set
   */
  public void setConfigFolder(String configFolder)
  {
    this.m_configFolder = configFolder;
  }

  /**
   * @return the configFile
   */
    public String getConfigXmlCache()
    {
      StringBuilder buf = new StringBuilder();

      for(Iterator<String> it = this.m_configs.iterator(); it.hasNext();)
      {
        String str = it.next();
        buf.append(str);
        if(it.hasNext())
        {
          buf.append(',');
        }
      }
      return buf.toString();
    }

  /**
   * @param configFile the configFile to set
   */
  public void setConfigXmlCache(String configXml)
  {
    this.m_configs = new TreeSet<String>();

    if(!isNull(configXml))
    {
      String[] configs = configXml.split("\\s*,\\s*");
      for(String config : configs)
      {
            this.m_configs.add(config);
          }
      }
    }

  /**
   * @return the categoriesXml resource names
   */
  public String getDatabaseXmlCache()
  {
    StringBuilder buf = new StringBuilder();

    for(Iterator<String> it = this.m_categories.iterator(); it.hasNext();)
    {
      String str = it.next();

      buf.append(str);
      if(it.hasNext())
      {
        buf.append(',');
      }
    }
    return buf.toString();
  }

  /**
   * @param categoriesXml the categoriesXml to set
   */
  public void setDatabaseXmlCache(String categoriesXml)
  {
    this.m_categories = new TreeSet<String>();

    if(!isNull(categoriesXml))
    {
      String[] categories = categoriesXml.split("\\s*,\\s*");
      for(String category : categories)
      {
        this.m_categories.add(category);
      }
    }
  }

  /**
   * @return the saveciteClients
   */
  public Map<String, List<Map<String,String>>> getSaveciteClients() {
	return saveciteClients;
  }

  /**
   * @param saveciteClients the saveciteClients to set
   */
  public void setSaveciteClients(Map<String, List<Map<String,String>>> saveciteClients) {
	log.info("saveciteClients updated");
	this.saveciteClients = saveciteClients;
	if(log.isDebugEnabled()) {
		if(this.saveciteClients == null) {
			log.debug("setSaveciteClients() called but saveciteClients is null");
			return;
		}  
		StringBuilder buf = new StringBuilder("setSaveciteClients()\n");
		buf.append('\n');
		buf.append('\n');
		addMapToStringBuilder(buf,this.saveciteClients,4,4);
		buf.append('\n');
		buf.append('\n');
		log.debug(buf.toString());
	}
  }
  
	public List<Map<String, String>> getSaveciteClientsForLocale(Locale locale) {
		List<Map<String, String>> clients = null;
		if(this.saveciteClients == null || this.saveciteClients.isEmpty()) {
			clients = new ArrayList<Map<String, String>>();
		} else if(this.saveciteClients.containsKey(locale.toString())) {
			clients = this.saveciteClients.get(locale.toString());
		} else if(this.saveciteClients.containsKey(locale.getLanguage() + "_" + locale.getCountry())) {
			clients = this.saveciteClients.get(locale.getLanguage() + "_" + locale.getCountry());
		} else if(this.saveciteClients.containsKey(locale.getLanguage())) {
			clients = this.saveciteClients.get(locale.getLanguage());
		} else if(this.saveciteClients.containsKey(SERVER_DEFAULT_LOCALE)) {
			clients = this.saveciteClients.get(SERVER_DEFAULT_LOCALE);
		} else {
			clients = new ArrayList<Map<String, String>>();
		}
		return clients;
	}

  protected void addMapToStringBuilder(StringBuilder buf,
			Map map, int indent, int indentIncrement) {
		
			for(Entry<String,Object> entry : ((Map<String,Object>) map).entrySet()) {
				String key = entry.getKey();
				for(int i = 0; i < indent; i++) {
					buf.append(' ');
				}
				buf.append(key);
				Object val = entry.getValue();
				if(val instanceof Map) {
					buf.append('\n');
					addMapToStringBuilder(buf,(Map) val,indent + indentIncrement, indentIncrement);
				} else if(val instanceof List) {
					addListToStringBuilder(buf, (List) val, indent + indentIncrement, indentIncrement);
				} else {
					buf.append(" == ");
					buf.append(val);
					buf.append('\n');
				}
			}
		
	  }

  protected void addListToStringBuilder(StringBuilder buf,
			List list, int indent, int indentIncrement) {
		
			buf.append('\n');
			for(Object val : list) {
				if(val instanceof Map) {
					addMapToStringBuilder(buf,(Map) val,indent + indentIncrement, indentIncrement);
				} else if(val instanceof List) {
					for(int i = 0; i < indent; i++) {
						buf.append(' ');
					}
					buf.append("----------\n");
					for(int i = 0; i < indent; i++) {
						buf.append(' ');
					}
					addListToStringBuilder(buf, (List) val, indent + indentIncrement, indentIncrement);
				} else {
					for(int i = 0; i < indent; i++) {
						buf.append(' ');
					}
					buf.append(val);
					buf.append('\n');
				}
			}
		
	  }

public Collection<String> getAllCategoryXml()
  {
    return new TreeSet<String>(this.m_categories);
  }
  

  /*
   * Configuration update
   */

  /**
   * Called when an observed object chnages (@see java.util.Observer#update)
   * @param arg0 - The observed object
   * @param arg1 - Event argument
   */
  public void update(Observable arg0, Object arg1)
  {
    if (arg1 instanceof Event)
    {
      Event event = (Event) arg1;
      /*
       * Modified?  If so (and this is our file) update the the configuration
       */
      if (event.getModify())
      {
        String  refstr = event.getResource();

        if (this.m_updatableResources.contains(refstr))
        {
          log.debug("Updating configuration from " + refstr);
          updateConfig(refstr);
        }
      }
    }
  }

  /**
   * Update configuration data from an XML resource
   * @param configFileRef XML configuration reference (/content/...)
   */
  protected void updateConfig(String configFileRef)
  {
    Reference ref = m_entityManager.newReference(configFileRef);
    
    SecurityAdvisor pushed = null;
    if (ref != null)
    {
      try
      {
        ContentResource resource;
        /*
         * Fetch configuration details from our XML resource
         */
        pushed = enableSecurityAdvisor();

        resource = org.sakaiproject.content.cover.ContentHostingService.getResource(ref.getId());
        if (resource != null)
        {
          populateConfig(configFileRef, resource.streamContent());
        }
      }
      catch (PermissionException e)
      {
        log.warn("Exception: " + e + ", continuing");
      }
      catch (IdUnusedException e)
      {
        log.warn("Citations configuration XML is missing ("
              +    configFileRef
              +    "); Citations ConfigurationService will watch for its creation");
      }
      catch (TypeException e)
      {
        log.warn("Exception: " + e + ", continuing");
      }
      catch (ServerOverloadException e)
      {
        log.warn("Exception: " + e + ", continuing");
      } finally {
    	  if(pushed != null) {
    		  boolean found = false;
    		  while(m_securityService.hasAdvisors() && ! found) {
    			  SecurityAdvisor popped = m_securityService.popAdvisor();
    			  found = popped == pushed;
    		  }
    	  }
      }
      
    }
    /*
     * Always add our reference to the list of observed resources
     */
    m_updatableResources.add(configFileRef);
  }


  /*
   * Miscellanous helpers
   */

  /**
   * Does the specified configuration/hierarchy resource exist?
   * @param resourceName Resource name
   * @return true If we can access this content
   */
  private boolean exists(String resourceName)
  {
    SecurityAdvisor pushed = null;
    try
    {
    	String configFolderRef  = getConfigFolderReference();


     	if (!isNull(configFolderRef) && !isNull(resourceName))
    	{
        String referenceName = configFolderRef + resourceName;

        Reference reference = m_entityManager.newReference(referenceName);
    		if (reference == null) return false;

    		pushed = enableSecurityAdvisor();
    		ContentResource resource = org.sakaiproject.content.cover.ContentHostingService.getResource(reference.getId());

        return (resource != null);
    	}
    }
    catch (IdUnusedException exception)
    {
      log.debug("exists() failed find resource: " + exception);
    }
    catch (Exception exception)
    {
      log.warn("exists() failed find resource: ", exception);
    }
    finally {
    	  if(pushed != null) {
    		  boolean found = false;
    		  while(m_securityService.hasAdvisors() && ! found) {
    			  SecurityAdvisor popped = m_securityService.popAdvisor();
    			  found = popped == pushed;
    		  }
    	  }
      }
   	return false;
  }

  /**
   * Establish a security advisor to allow the "embedded" azg work to occur
   * with no need for additional security permissions.
   * @return the advisor
   */
  protected SecurityAdvisor enableSecurityAdvisor()
  {
    SecurityAdvisor advisor = new SecurityAdvisor() {
        public SecurityAdvice isAllowed(String userId, String function, String reference)
        {
          return SecurityAdvice.ALLOWED;
        }
      };
	// put in a security advisor so we can create citationAdmin site without need
    // of further permissions
    m_securityService.pushAdvisor(advisor );
    return advisor;
  }

  /**
   * Null (or empty) String?
   * @param string String to check
   * @return true if so
   */
  private boolean isNull(String string)
  {
    return (string == null) || (string.trim().equals(""));
  }

    /**
     * Is library search enabled for the current user?
     * @return true if so
     */
  	public boolean librarySearchEnabled()
	{
		return isLibrarySearchEnabled() && isConfigurationXmlAvailable() && isDatabaseHierarchyXmlAvailable();
	}

  public void setExternalSearchEnabled(boolean state)
  {
    this.m_externalSearchEnabled = state;
  }

  public boolean isExternalSerarchEnabled()
  {
    boolean enabled = m_externalSearchEnabled;
    String state = getConfigurationParameter("external-search-enabled");
   
    if (state != null)
    {
      enabled = "true".equals(state);
    }
   
    log.debug("External Search enabled: " + enabled);
    return enabled && getExternalSearchUrl() != null;
  }

  public void setExternalSearchUrl(String url) {
    this.m_externalSearchUrl = url;
  }

  public String getExternalSearchUrl() {
    String url = getConfigurationParameter("external-search-url");
    if (url == null)
    {
      url = m_externalSearchUrl;
    }
    return url;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.citation.api.ConfigurationService#getSecondsBetweenSaveciteRefreshes()
   */
  public String getSecondsBetweenSaveciteRefreshes() {
	  String secondsBetweenRefreshes = this.getConfigurationParameter(PARAM_SECONDS_BETWEEN_SAVECITE_REFRESHES);
	  if(secondsBetweenRefreshes == null) {
		// no stored value for secondsBetweenRefreshes; use default
		  secondsBetweenRefreshes = DEFAULT_SECONDS_BETWEEN_SAVECITE_REFRESHES;
	  } else {
		  try {
			  int num = Integer.parseInt(secondsBetweenRefreshes);
			  if(num < 1) {
				  // stored value of secondsBetweenRefreshes is too small; use default
				  secondsBetweenRefreshes = DEFAULT_SECONDS_BETWEEN_SAVECITE_REFRESHES;
			  } else if(num > MAXIMUM_SECONDS_BETWEEN_SAVECITE_REFRESHES) {
				  // stored value of secondsBetweenRefreshes is too big; use max
				  secondsBetweenRefreshes = Integer.toString(MAXIMUM_SECONDS_BETWEEN_SAVECITE_REFRESHES);
			  }
		  } catch(NumberFormatException e) {
			  // stored value of secondsBetweenRefreshes is not a number; use default
			  secondsBetweenRefreshes = DEFAULT_SECONDS_BETWEEN_SAVECITE_REFRESHES;
		  }
	  }
	  return secondsBetweenRefreshes ;
  }
}
