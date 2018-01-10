package edu.indiana.lib.osid.base.repository.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import edu.indiana.lib.twinpeaks.search.QueryBase;
import edu.indiana.lib.twinpeaks.search.SearchResultBase;
import edu.indiana.lib.twinpeaks.search.SearchSource;
import edu.indiana.lib.twinpeaks.util.SearchException;
import edu.indiana.lib.twinpeaks.util.SessionContext;
import edu.indiana.lib.twinpeaks.util.StatusUtils;
import edu.indiana.lib.twinpeaks.util.StringUtils;

@Slf4j
public class Repository extends edu.indiana.lib.osid.base.repository.Repository
{
	private java.util.Vector assetVector = new java.util.Vector();

	private org.osid.id.IdManager idManager = null;
	private org.osid.shared.Id id = null;
	private String idString = null;

	private String displayName = null;
	private String description = null;

	private String url = null;

	private org.osid.shared.Type repositoryType = new Type("sakaibrary", "repository", "metasearch");
	private org.osid.shared.Type assetType = new Type("mit.edu","asset","library_content");
	private org.osid.shared.Type thumbnailType = new Type("mit.edu","partStructure","thumbnail");
	private org.osid.shared.Type urlType = new Type("mit.edu","partStructure","URL");

	private java.util.Vector searchTypeVector = new java.util.Vector();
	private java.util.Vector queryHandlerVector = new java.util.Vector();
	private java.util.Vector responseHandlerVector = new java.util.Vector();

	private AssetIterator	assetIterator = null;

  private org.osid.shared.Type searchPropertiesType = new Type("sakaibrary", "properties", "asynchMetasearch");
  private org.osid.shared.Type searchStatusPropertiesType = new Type("sakaibrary", "properties", "metasearchStatus");

  private org.osid.shared.Properties searchStatusProperties = null;
  private org.osid.shared.Properties searchProperties = null;


	protected Repository(String displayName,
											 String description,
											 String idString,
											 java.util.Vector searchTypeVector,
											 java.util.Vector queryHandlerVector,
											 java.util.Vector responseHandlerVector,
											 org.osid.id.IdManager idManager)
											 throws org.osid.repository.RepositoryException
	{
		this.displayName = displayName;
		this.description = description;
		this.idString = idString;
		this.searchTypeVector = searchTypeVector;
		this.queryHandlerVector = queryHandlerVector;
		this.responseHandlerVector = responseHandlerVector;
		this.idManager = idManager;
		this.id = null;

		try
		{
			this.id = idManager.getId(this.idString);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
		
		if (this.id == null)
		{
			log.debug("Could not set HTTP Repository() " + displayName);
			return;
		}
		
		log.debug("new HTTP Repository(): " + displayName + ", id: " + this.id);

		try
		{
		  log.debug("    HTTP Repository(): " + this.id.getIdString() + ", is equal? " + this.id.isEqual(idManager.getId(this.idString)));
		}
		catch (Throwable ignore) { }
	}

	public String getDisplayName()
	throws org.osid.repository.RepositoryException
	{
		return this.displayName;
	}

	public String getDescription()
	throws org.osid.repository.RepositoryException
	{
		return this.description;
	}

	public org.osid.shared.Id getId()
	throws org.osid.repository.RepositoryException
	{
		return this.id;
	}

	public org.osid.shared.Type getType()
	throws org.osid.repository.RepositoryException
	{
		return this.repositoryType;
	}

	public org.osid.shared.TypeIterator getAssetTypes()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		try
		{
			results.addElement(this.assetType);
			return new TypeIterator(results);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.repository.RecordStructureIterator getRecordStructures()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		results.addElement(RecordStructure.getInstance());
		return new RecordStructureIterator(results);
	}

	public org.osid.repository.RecordStructureIterator getMandatoryRecordStructures(org.osid.shared.Type assetType)
	throws org.osid.repository.RepositoryException
	{
		if (assetType == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		if (assetType.isEqual(this.assetType))
		{
			java.util.Vector results = new java.util.Vector();
			results.addElement(RecordStructure.getInstance());
			return new RecordStructureIterator(results);
		}
		throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_TYPE);
	}

	public org.osid.shared.TypeIterator getSearchTypes()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		try
		{
			return new TypeIterator(this.searchTypeVector);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.shared.TypeIterator getStatusTypes()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		try
		{
			results.addElement(new Type("mit.edu","asset","valid"));
			return new TypeIterator(results);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.shared.Type getStatus(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		return new Type("mit.edu","asset","valid");
	}

	public boolean validateAsset(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		return true;
	}

	public org.osid.repository.AssetIterator
				getAssetsBySearch(java.io.Serializable searchCriteria
												,	org.osid.shared.Type searchType
												,	org.osid.shared.Properties searchProperties)
												throws org.osid.repository.RepositoryException
	{
		QueryBase					queryBase;
		SearchResultBase	resultHandler;
		SessionContext		sessionContext;


		if (searchCriteria == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}

		if (!(searchCriteria instanceof String))
		{
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}

		if (searchType == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}

		if (searchProperties == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}

		try
		{
			org.osid.shared.Type 	type;
			String 			criteria;
			String			sessionId;
			boolean			doRange;

			sessionId = (String) searchProperties.getProperty("guid");
			if (StringUtils.isNull(sessionId))
			{
				throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
			}

	/*
			doRange	= "rangeRequest".equalsIgnoreCase((String) searchProperties.getProperty("action"));
			if ((doRange) && (assetIterator == null))
			{
				throw new org.osid.repository.RepositoryException("Initial search cannot be a range request");
			}
	*/
			doRange = false;

			type = (org.osid.shared.Type)(this.searchTypeVector.elementAt(0));
    	if (!type.isEqual(searchType))
			{
				throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_TYPE);
			}

			this.searchProperties = searchProperties;

			criteria 				= (String) searchCriteria;
			sessionContext	= SessionContext.getInstance(sessionId);
			queryBase 			= doQuery(criteria, this.getDisplayName(), searchProperties, sessionContext);
    	resultHandler		= getResponseHandler(queryBase);

			if (doRange)
			{
				assetIterator.initialize(searchProperties);
			}
			else
			{
				assetIterator = new AssetIterator(this.getDisplayName(),
																					queryBase, resultHandler,
																					searchProperties, this.getId(),
																					sessionContext);
			}
		}
		catch (Throwable throwable)
		{
			log.error(throwable.getMessage(), throwable);

			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}

		return assetIterator;
	}

	public org.osid.repository.RecordStructureIterator getRecordStructuresByType(org.osid.shared.Type recordStructureType)
	throws org.osid.repository.RepositoryException
	{
		if (recordStructureType == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		if (recordStructureType.isEqual(new Type("mit.edu","recordStructure","wellFormed")))
		{
			java.util.Vector results = new java.util.Vector();
			// don't return the content's strucutre even if it matches, since this that is a separate and special case
			results.addElement(RecordStructure.getInstance());
			return new RecordStructureIterator(results);
		}
		throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_TYPE);
	}

  public org.osid.shared.PropertiesIterator getProperties()
  throws org.osid.repository.RepositoryException
  {
	  java.util.Vector results = new java.util.Vector();

    try
    {
		  if (searchProperties != null)
		  {
		  	searchStatusProperties = getStatusProperties((String) searchProperties.getProperty("guid"));

		  	results.addElement(searchProperties);
			  results.addElement(searchStatusProperties);
			}

      return new PropertiesIterator(results);
    }
    catch (Throwable throwable)
    {
      log.error(throwable.getMessage());
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
  }

  public org.osid.shared.Properties getPropertiesByType(org.osid.shared.Type propertiesType )
  throws org.osid.repository.RepositoryException
  {
    if (propertiesType == null)
    {
      throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
    }

    try
    {
	    org.osid.shared.PropertiesIterator iterator = getProperties();

    	while (iterator.hasNextProperties())
    	{
    		org.osid.shared.Properties properties = iterator.nextProperties();

    		if (properties.getType().isEqual(propertiesType))
    		{
    			return properties;
    		}
    	}
    }
    catch (org.osid.shared.SharedException exception)
    {
    	throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
    throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_TYPE);
  }

  public org.osid.shared.TypeIterator getPropertyTypes()
  throws org.osid.repository.RepositoryException
  {
	  java.util.Vector results = new java.util.Vector();

	  results.addElement(searchPropertiesType);
	  results.addElement(searchStatusPropertiesType);

    try
    {
      return new TypeIterator(results);
    }
    catch (Throwable t)
    {
      log.error(t.getMessage());
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
  }

	protected void addAsset(org.osid.repository.Asset asset)
	throws org.osid.repository.RepositoryException
	{
		this.assetVector.addElement(asset);
	}

	public boolean supportsUpdate()
	throws org.osid.repository.RepositoryException
	{
		return false;
	}

	public boolean supportsVersioning()
	throws org.osid.repository.RepositoryException
	{
		return false;
	}

	/*
	 * Local Helpers
	 */

	/**
	 * Returns a new result set name for this transaction
	 * @param sessionId Session ID (guid)
	 * @return A shared Properties object reflecting the current status
	 */
	private org.osid.shared.Properties getStatusProperties(String sessionId)
	throws org.osid.repository.RepositoryException
	{
		SessionContext	sessionContext;

		ArrayList	dbList			= new ArrayList();
		HashMap		statusMap		= new HashMap();
		String		status			= null;
		int				active			= 0;
		int				hits				= 0;
		int				estimate		= 0;


		try
		{
			sessionContext = SessionContext.getInstance(sessionId);

			for (Iterator iterator = StatusUtils.getStatusMapEntrySetIterator(sessionContext); iterator.hasNext(); )
			{
				Map.Entry entry 			= (Map.Entry) iterator.next();
				HashMap		targetMap 	= (HashMap) entry.getValue();
				HashMap		singleMap;
				boolean 	targetActive;

				dbList.add(entry.getKey());
				targetActive = false;

				status = (String) targetMap.get("STATUS");
				if (status.equals("ACTIVE"))
				{
					active++;
					targetActive = true;
				}
				hits 			+= Integer.parseInt((String) targetMap.get("HITS"));
				estimate 	+= Integer.parseInt((String) targetMap.get("ESTIMATE"));

				singleMap = new HashMap();

				singleMap.put("status", status);
				singleMap.put("statusMessage", (String) targetMap.get("STATUS_MESSAGE"));
				singleMap.put("numRecordsFetched", new Integer((String) targetMap.get("HITS")));
				singleMap.put("numRecordsFound", new Integer((String) targetMap.get("ESTIMATE")));
				singleMap.put("numRecordsMerged", new Integer(0));

				statusMap.put(entry.getKey(), singleMap);
			}

			log.debug(this.getDisplayName() + ": " + active + " searches active");

			statusMap.put("databaseNames", dbList);

			statusMap.put("status", sessionContext.get("STATUS"));
			statusMap.put("statusMessage", sessionContext.get("STATUS_MESSAGE"));

			statusMap.put("numRecordsFetched", new Integer(hits));
			statusMap.put("numRecordsFound", new Integer(estimate));
			statusMap.put("numRecordsMerged", new Integer(0));

			statusMap.put("delayHint", new Integer((active * 2 * 1024)));

			return new SharedProperties(statusMap, searchStatusPropertiesType);
		}
		catch (Throwable throwable)
		{
			log.error(throwable.getMessage());
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
		}
	}

  /**
   * Select a query handler for the requested database
   * @param sessionContext Session context
   * @param name Database name (eg ERIC)
   * @return Query handler
   */
  private QueryBase selectQueryHandler(SessionContext   sessionContext,
  																		 String 				  name) {
    SearchSource	source 	= SearchSource.getSourceByName(name);
		QueryBase			handler;

		try {
			handler = source.getQueryHandler();
		} catch (Exception exception) {
			throw new SearchException(exception.toString());
		}
    /*
     * Initialize and return the query handler
     */
    handler.initialize(sessionContext);
    return handler;
  }

  /**
   * Execute an HTTP query for a standard search operation
   * @param request The servlet request block for this transaction
   * @param sessionContext Session context for this user
   * @param database Database name
   */
  private QueryBase doQuery(String searchString, String database,
  													org.osid.shared.Properties searchProperties,
  													SessionContext sessionContext)
  													throws org.osid.repository.RepositoryException,
  													       org.osid.shared.SharedException
	{
  	QueryBase         query;
  	HashMap		        parameterMap;
  	ArrayList<String> targetList;
  	StringBuilder      targetBuffer;

  	parameterMap = new HashMap();
  	parameterMap.put("searchString", searchString);
  	parameterMap.put("database", database);

  	parameterMap.put("guid", searchProperties.getProperty("guid"));
  	parameterMap.put("url", searchProperties.getProperty("baseUrl"));
  	parameterMap.put("username", searchProperties.getProperty("username"));
  	parameterMap.put("password", searchProperties.getProperty("password"));
  	parameterMap.put("sortBy", searchProperties.getProperty("sortBy"));
  	parameterMap.put("pageSize", searchProperties.getProperty("pageSize"));
  	parameterMap.put("startRecord", searchProperties.getProperty("startRecord"));
  	parameterMap.put("maxRecords", searchProperties.getProperty("maxRecords"));

  	parameterMap.put("action", "startSearch");    // or "requestRange"
    /*
     * Add in the target database list
     */
    targetBuffer  = new StringBuilder();
    targetList    = (java.util.ArrayList<String>)
                               searchProperties.getProperty("databaseIds");

    for (int i = 0; i < targetList.size(); i++)
    {
      if (i > 0)
      {
        targetBuffer.append(' ');
      }
      targetBuffer.append(targetList.get(i));
    }
    parameterMap.put("targets", targetBuffer.toString());
    /*
     * Allow the embedded configuration to override the URL, username and password
     */
    if (SearchSource.getConfiguredParameter(database, "url") != null)
    {
      parameterMap.put("url",
                       SearchSource.getConfiguredParameter(database, "url"));
    }

    if (SearchSource.getConfiguredParameter(database, "username") != null)
    {
      parameterMap.put("username",
                       SearchSource.getConfiguredParameter(database, "username"));
    }

    if (SearchSource.getConfiguredParameter(database, "password") != null)
    {
      parameterMap.put("password",
                       SearchSource.getConfiguredParameter(database, "password"));
    }

		query = selectQueryHandler(sessionContext, database);
		query.parseRequest(parameterMap);
    query.doQuery();

    return query;
  }

	/**
   * Select a search result handler for this source
   * @param name Source name
   * @return Search result handler
   */
  private SearchResultBase selectSearchResultHandler(String name) {
    SearchSource			source = SearchSource.getSourceByName(name);
		SearchResultBase	handler;

		try
		{
			handler = source.getSearchResultHandler();
		} catch (Exception exception) {
			throw new SearchException(exception.toString());
		}
		return handler;
  }

  /**
   * Locate the appropriate response handler
   * @param query Query handler
   */
  private SearchResultBase getResponseHandler(QueryBase query)
	{
    SearchResultBase searchResult;

    searchResult = selectSearchResultHandler(query.getRequestParameter("database"));
    return searchResult;
  }
}
