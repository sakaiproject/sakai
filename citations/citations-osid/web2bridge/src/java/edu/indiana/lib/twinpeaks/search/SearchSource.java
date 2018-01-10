/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.indiana.lib.twinpeaks.util.DomException;
import edu.indiana.lib.twinpeaks.util.DomUtils;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.StringUtils;

@Slf4j
public class SearchSource {
  /**
   * This source is enabled (available for use)
   */
  private static final int  	ENABLED							= (1 << 0);
	/**
	 * Global configuration parameters
	 */
	private static HashMap _globalMap;
  /*
   * Display name & id, description, handlers, flags
   */
  private String  _name;
  private String  _id;
  private String  _description;

  private String  _queryClassName;
  private Class   _queryClass;

  private String  _searchResultClassName;
  private Class   _searchResultClass;

	private String	_authority;
	private String	_domain;
	private String	_searchType;
	private String	_typeDescription;

	private HashMap	_parameterMap;

  private int     _flags;

  /**
   * SearchSource instance
   */
  private static ArrayList	_sourceList = null;
  /**
   * SearchSource synchronization
   */
  private static Object	_sourceSync	= new Object();
  /**
   * Private constructor
   */
  private SearchSource() {
  }

	/**
	 * Constructor
	 * @param name Search source name (used internally and for the pulldown menu)
	 * @param queryClassName Query handler
	 * @param searchResultClassName Search response handler
	 * @param resultPageClassName User result renderer
	 * @param url Base URL for query
	 * @param parameterMap Custom parameters
	 * @param flags Enabled, disabled, etc.
	 */
	private SearchSource(String 	name,
											 String		description,
											 String		id,
											 String		authority,
											 String		domain,
											 String		searchType,
											 String		typeDescription,
											 String 	queryClassName,
											 String 	searchResultClassName,
											 HashMap	parameterMap,
											 int 			flags) {

		_name 									= name;
		_description						= description;
		_id											= id;
		_authority							= authority;
		_domain									= domain;
		_searchType							= searchType;
		_typeDescription				= typeDescription;
		_queryClassName 				= queryClassName;
		_searchResultClassName 	= searchResultClassName;
		_parameterMap						= parameterMap;
		_flags 									= flags;

		log.debug("*************** name + parameters = {}", _parameterMap);
	}

  /**
   * Return the search source (repository) name
   * @return The name of this source (eg Academic Search, ERIC)
   */
  public String getName() {
    return _name;
  }

  /**
   * Return the search source id (a unique String)
   * @return The name of this source (eg Academic Search
   */
  public String getId() {
    return _id;
  }

  /**
   * Return authority information
   * @return The authority for this source
   */
  public String getAuthority() {
    return _authority;
  }

  /**
   * Return search domain
   * @return The domain for this source (eg search)
   */
  public String getDomain() {
    return _domain;
  }

  /**
   * Return the search type
   * @return The type of search (eg keyword)
   */
  public String getSearchType() {
    return _searchType;
  }

  /**
   * Return the search type description
   * @return The description (eg "keyword search")
   */
  public String getTypeDescription() {
    return _typeDescription;
  }

  /**
   * Return the search source description
   * @return A description of this repository
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Is this source available?
   * @return true (if available)
   */
  public boolean isEnabled() {
    return (_flags & ENABLED) == ENABLED;
  }

  /**
   * Return a new QueryBase object for the specified search source.
   * Class loading is defered until request time.
   * @return A QueryBase object for this source
   */
  public QueryBase getQueryHandler() throws java.lang.ClassNotFoundException,
  																					java.lang.InstantiationException,
  																					java.lang.IllegalAccessException {
    synchronized (this) {
      if (_queryClass == null) {
        _queryClass = Class.forName(_queryClassName);
      }
      return (QueryBase) _queryClass.newInstance();
    }
  }

  /**
   * Return the query handler class name.
   * @return Query handler class name
   */
  public String getQueryHandlerClassName() {
 	  synchronized (this) {
	    return _queryClassName;
	  }
  }

  /**
   * Return a new SearchResultBase object for the specified search source.
   * Class loading is defered until request time.
   * @return A SearchResultBase object for this source
   */
  public SearchResultBase getSearchResultHandler()
  																	throws 	java.lang.ClassNotFoundException,
  																					java.lang.InstantiationException,
  																					java.lang.IllegalAccessException {
    synchronized (this) {
      if (_searchResultClass == null) {
        _searchResultClass = Class.forName(_searchResultClassName);
      }
      return (SearchResultBase) _searchResultClass.newInstance();
    }
  }

  /**
   * Return the search result handler class name.
   * @return Result handler class name
   */
  public String getSearchResultHandlerClassName() {
 	  synchronized (this) {
	    return _searchResultClassName;
	  }
  }

	/**
	 * Set a global parameter from the configuration file
	 * @param name Parameter name
	 */
	private static void setGlobalConfiguationValue(Document document, String name)
	{
		Element element;

		element = DomUtils.getElement(document.getDocumentElement(), name);
		if (element != null)
		{
			String text = element.getAttribute("name");

			if (!StringUtils.isNull(text))
			{
				_globalMap.put(name, text);
			}
		}
	}

  /**
   * Return a global parameter
   * @param name Parameter name
   * @return Parameter value (null if none)
   */
  public static String getGlobalConfigurationValue(String name) {
  	return (_globalMap == null) ? null : (String) _globalMap.get(name);
  }

  /**
   * Return a mandatory global configuration value
   * @param name The name of the cglobal configuration item
   * @return The configured value
   */
  public static String getMandatoryGlobalConfigurationValue(String name) {
		String value = getGlobalConfigurationValue(name);

		if (value == null) {
			throw new ConfigurationException("Global configuration item \""
																		+  	name
																		+ 	"\" is not defined");
		}
		return value;
  }

  /**
   * Return a custom parameter configured for this source
   * @param name Parameter name
   * @return Parameter value (null if none)
   */
  public synchronized String getConfiguredParameter(String name) {
  	return (_parameterMap == null) ? null : (String) _parameterMap.get(name);
  }

  /**
   * Return a custom parameter configured for this source
   * @param name The source name (eg ERIC)
   * @param parameterName Parameter to fetech
   * @return The parameter value (null if none)
   */
  public static String getConfiguredParameter(String name, String parameterName) {
    SearchSource	source	= SearchSource.getSourceByName(name);

  	return source.getConfiguredParameter(parameterName);
  }

  /**
   * Return a mandatory parameter for this source
   * @param name The source name (eg ERIC)
   * @param parameterName Parameter to fetech
   * @return The parameter value
   */
  public static String getMandatoryParameter(String name, String parameterName) {
    SearchSource	source	= SearchSource.getSourceByName(name);
		String				value		= source.getConfiguredParameter(parameterName);

		if (value == null) {
			throw new ConfigurationException("\""
																		+  	parameterName
																		+ 	"\" parameter undefined for search source: "
																		+ 	name);
		}
		return value;
  }

  /**
   * Lookup a search source by name
   * @param name Source name
   * @return SearchSource object
   */
  public static SearchSource getSourceByName(String name) {

		verifyList();

		synchronized (_sourceSync) {
	    for (Iterator i = _sourceList.iterator(); i.hasNext(); ) {
				SearchSource source = (SearchSource) i.next();

				if (source.getName().equalsIgnoreCase(name)) {
        	return source;
      	}
      }
	  }
    throw new ConfigurationException("Unknown search source: " + name);
  }

	/**
	 * Get the default search source
	 * @return The search source name
	 */
	public static String getDefaultSourceName() {
		verifyList();

		synchronized (_sourceSync) {
			return ((SearchSource) _sourceList.get(0)).getName();
		}
	}

	/**
	 * Return an Iterator to the source list
	 * @return Source list Iterator
	 */
	public static Iterator getSearchListIterator() {
		verifyList();

		synchronized (_sourceSync) {
			return _sourceList.iterator();
		}
	}

  /**
   * Create a populated <code>SearchSource</code> list.
   * @param xmlStream Configuration file as an InputStream
   */
  public static void populate(InputStream xmlStream) throws DomException, SearchException {
		SearchSource 	source;
		NodeList			sourceNodeList;
		Document			document;
		int						length;

		/*
		 * Only set the configuration once
		 */
		log.debug("SearchSource.populate() starts --------------------------");

		synchronized (_sourceSync)
		{
			if (_sourceList != null)
			{
				log.debug("No action required");
				return;
			}
			_sourceList = new ArrayList();
			log.debug("Populating configuration");
			/*
			 * Parse the configuration file
			 */
			try
			{
				document = DomUtils.parseXmlStream(xmlStream);
				log.info(DomUtils.serialize(document));
			}
			catch (Exception exception)
			{
				log.error("DOM parse exception", exception);
				throw new RuntimeException("DOM error");
			}
			/*
			 * Fetch global settings - OSID version specific implementations
			 */
			_globalMap = new HashMap();

      setGlobalConfiguationValue(document, "osid_20_Id_Implementation");

			/*
			 * Find our search sources (each represents an OSID Repository)
			 */
			sourceNodeList 	= DomUtils.getElementList(document.getDocumentElement(), "source");
			length					= sourceNodeList.getLength();

			for (int i = 0; i < length; i++) {
				String		sourceName, description, id;
				String		authority, domain, searchType, typeDescription, url;
				String		queryHandler, searchResultHandler;
				Element		element, sourceElement;
				NodeList	parameterList;
				HashMap		parameterMap;
				int				flags;

				sourceElement	= (Element) sourceNodeList.item(i);
				sourceName 		= sourceElement.getAttribute("name");

				if (StringUtils.isNull(sourceName)) {
					log.warn("Skipping un-named <source> element");
					continue;
				}
				/*
				 * Search source (Repository) description and **unique** ID
				 */
				if ((description = parseHandler(sourceElement, "description")) == null) {
					log.warn("Missing <description> in source \"" + sourceName + "\"");
					continue;
				}

				if ((id = parseHandler(sourceElement, "id")) == null) {
					log.warn("Missing <id> in source \"" + sourceName + "\"");
					continue;
				}
				/*
				 * Query and result handler names
				 */
				if ((queryHandler = parseHandler(sourceElement, "queryhandler")) == null) {
					log.warn("Missing <queryhandler> in source \"" + sourceName + "\"");
					continue;
				}

				if ((searchResultHandler = parseHandler(sourceElement, "responsehandler")) == null) {
					log.warn("Missing <responsehandler> in source \"" + sourceName + "\"");
					continue;
				}
				/*
				 * Authority, domain, sarch type & description, URL
				 */
				if ((authority = parseHandler(sourceElement, "authority")) == null) {
					log.warn("Missing <authority> in source \"" + sourceName + "\"");
					continue;
				}

				if ((domain = parseHandler(sourceElement, "domain")) == null) {
					log.warn("Missing <domain> in source \"" + sourceName + "\"");
					continue;
				}

				if ((searchType = parseHandler(sourceElement, "searchtype")) == null) {
					log.warn("Missing <searchtype> in source \"" + sourceName + "\"");
					continue;
				}

				if ((typeDescription = parseHandler(sourceElement, "searchdescription")) == null) {
					log.warn("Missing <searchdescription> in source \"" + sourceName + "\"");
					continue;
				}
				/*
				 * Set options:
				 *	source enabled = [true | false]
				 */
				flags	= 0;
				if ((element = DomUtils.getElement(sourceElement, "options")) != null) {

					if ("true".equalsIgnoreCase(element.getAttribute("enabled"))) {
						flags |= ENABLED;
					}
				}
				/*
				 * Custom parameters?
				 */
				parameterMap = null;
				if ((parameterList = DomUtils.getElementList(sourceElement, "parameter")) != null) {

					for (int j = 0; j < parameterList.getLength(); j++) {
						String name, value;

						element = (Element) parameterList.item(j);
						name		= element.getAttribute("name");
						value		= element.getAttribute("value");

						if (StringUtils.isNull(name)) {
							throw new SearchException(
														"Invalid configuration parameter, source: \""
												+		sourceName
												+		"\", <parameter name=\""
												+ 	 name
												+ 	 "\" value=\""
												+ 	 value
												+ 	 "\">");
						}

						if (parameterMap == null) {
							parameterMap = new HashMap();
						}
						parameterMap.put(name, value);
					}
				}
				/*
				 * Save this source
				 */
				addSource(new SearchSource(sourceName,
											 						 description,
											 						 id,
											 						 authority,
											 						 domain,
											 						 searchType,
											 						 typeDescription,
											 						 queryHandler,
											 						 searchResultHandler,
											 						 parameterMap,
											 						 flags), _sourceList);
			}
		}

		log.debug("SearchSource.populate() ends --------------------------");

		if (_sourceList.size() == 0)
		{
			throw new SearchException("No Repositories were configured");
		}
  }

	/**
	 * Has source list has been populated?
	 * @return true if so
	 */
	public static boolean isSourceListPopulated() {
		synchronized (_sourceSync) {
			return !((_sourceList == null) || (_sourceList.isEmpty()));
		}
	}

	/*
	 * Helpers
	 */

	/**
	 * Locate a handler specification
	 * @param parent Parent element for this search
	 * @param handlerName Handler to look up
	 * @return Class name for this handler
	 */
	 private static String parseHandler(Element parent, String handlerName) {
	 	Element element;
	 	String	handler;

		if ((element = DomUtils.getElement(parent, handlerName)) == null) {
			return null;
		}

		handler = element.getAttribute("name");
		return (StringUtils.isNull(handler)) ? null : handler;
	}

	/**
	 * Verify the source list has been populated
	 */
	private static void verifyList() {
		if (!isSourceListPopulated()) {
			throw new SearchException("No search handlers have ben configured");
		}
	}

	/**
	 * Add a Search source to the appropriate list
	 * @param source SearceSource object
	 * @param list Source list
	 */
	private static void addSource(SearchSource source, ArrayList list)
	{
		list.add(source);
	}

	private static class ConfigurationException extends RuntimeException {
	  /**
	   * Thrown to indicate a configuration exception
	   * @param text Explainatory text
	   */
	  public ConfigurationException(String text) {
	    super(text);
	  }
	  /**
	   * Thrown to indicate a configuration exception
	   */
	  public ConfigurationException() {
	    super("");
	  }
	}
}
