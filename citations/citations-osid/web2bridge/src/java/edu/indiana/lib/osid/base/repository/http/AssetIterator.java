package edu.indiana.lib.osid.base.repository.http;

import edu.indiana.lib.twinpeaks.net.*;
import edu.indiana.lib.twinpeaks.search.*;
import edu.indiana.lib.twinpeaks.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2007, 2008 The Sakai Foundation
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
/**
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */
@Slf4j
public class AssetIterator extends edu.indiana.lib.osid.base.repository.AssetIterator
{
	private org.osid.shared.Properties 		searchProperties;
	private org.osid.shared.Id 						repositoryId;
	/*
	 * HTTP transaction information
	 */
	private QueryBase						queryBase;
	private SearchResultBase		searchResult;
	private SessionContext			sessionContext;
	private String							database;
	/*
	 * Asset vector (a queue) and various details
	 */
	private AssetIterator	assetIterator;

  private Vector	assetVector;
  private int 		index;
	private int 		populated;
	private int			startRecord;
	private int			pageSize;

	/**
	 * Unused constructor
	 */
	protected AssetIterator(Vector vector)
	{
	}

	/**
	 * Constructor
	 *
	 * @param database The database (or target) for this search
	 * @queryBase Query handler (QueryBase implementation)
	 * @searchResult Search result handler (SearchResultBase implementation)
	 * @param searchProperties Property list (search characteristics, provided by our caller)
	 * @param repositoryId Unique Repository ID
	 * @param sessionContext Context data for the current user/caller
	 */
  protected AssetIterator(String database,
  												QueryBase queryBase,
  												SearchResultBase searchResult,
  												org.osid.shared.Properties searchProperties,
  												org.osid.shared.Id repositoryId,
  												SessionContext sessionContext)
  												throws org.osid.repository.RepositoryException
  {
  	try
  	{
    	this.database 				= database;
    	this.queryBase 				= queryBase;
    	this.searchResult			= searchResult;
    	this.repositoryId			= repositoryId;
    	this.sessionContext		= sessionContext;

			this.assetIterator		= null;

			initialize(searchProperties);
    }
    catch (Throwable throwable)
    {
   		log.error("AssetIterator() ", throwable);
    	throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.OPERATION_FAILED);
  	}
	}

	/**
	 * Initialize
	 * @param searchProperties Property list (search characteristics, provided by our caller)
	 */
	protected void initialize(org.osid.shared.Properties searchProperties) throws org.osid.shared.SharedException
	{
		try
		{
    	this.assetVector  = new Vector();
    	this.index				= 0;
    	this.populated		= 0;
			/*
			 * Save starting record number, page size, properties pointer
			 */
			startRecord	= getIntegerProperty(searchProperties, "startRecord").intValue();
			pageSize		= getIntegerProperty(searchProperties, "pageSize").intValue();

    	this.searchProperties	= searchProperties;

    	log.debug("AssetIterator max = " + getMaximumRecords() + ", page = " + pageSize + ", start = " + startRecord);
    }
    catch (Throwable throwable)
    {
    	log.error("initialize() " + throwable);
    	throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.OPERATION_FAILED);
  	}
	}

	/**
	 * Is another Asset available?  If so, set the search status as "complete".
	 * @return true if an Asset is available, false if not
	 */
  public boolean hasNextAsset()
  throws org.osid.repository.RepositoryException
  {
		try
		{
		  boolean moreRecords;

    log.debug("hasNextAsset: index=" + index
            +  ", maximum records=" + getMaximumRecords()
            +  ", async init=" + StatusUtils.doingAsyncInit(sessionContext));
      /*
       * During asynchronous initialization, we assume there are more assets
       */
      if (StatusUtils.doingAsyncInit(sessionContext))
      {
        return true;
      }
      /*
       * Normal use, are there any more assets?
       */
			moreRecords = (index < getMaximumRecords());
    	log.debug("AssetIterator.hasNext() = " + moreRecords);

    	if (!moreRecords)
    	{
    		StatusUtils.setAllComplete(sessionContext);
    	}
      return moreRecords;
    }
    catch (Throwable throwable)
    {
    	log.error("hasNextAsset() " + throwable);
    	throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.OPERATION_FAILED);
  	}
  }

	/**
	 * Fetch the next available search result (from the "Asset queue")
	 * @return The next search result (as an Asset)
	 */
  public org.osid.repository.Asset nextAsset()
  throws org.osid.repository.RepositoryException
  {
		org.osid.repository.Asset asset;

		/*
		 * End-of-file?
		 */
    if (!StatusUtils.doingAsyncInit(sessionContext))
    {
      if (index >= getMaximumRecords())
      {
      	StatusUtils.setAllComplete(sessionContext);
      	throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
      }
    }
		/*
		 * Additional assets should be available from the server
		 */
    log.debug("nextAsset: index=" + index
            +  ", populated=" + populated
            +  ", async init=" + StatusUtils.doingAsyncInit(sessionContext));

    if ((index >= populated) || (populated == 0))
    {
      if (!StatusUtils.doingAsyncInit(sessionContext))
      {	/*
      	 * The cache is depleted - we need to fetch more assets
      	 */
      	if (sessionContext.getInt("active") == 0)
      	{	/*
  				 * Every search has been marked "complete" (unexepected).
  				 */
          throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
  			}
      }
			/*
			 * Populate the Asset queue with new results
			 */
			try
			{
      	populateAssetQueue();
      }
			catch (SessionTimeoutException sessionTimeoutException)
			{
	    	log.error("nextAsset() session timeout: " + sessionTimeoutException);
	    	throw new MetasearchException(MetasearchException.SESSION_TIMED_OUT);
			}
			catch (SearchException searchException)
			{ /*
			   * No assets ready?
			   */
			  if (searchException.getMessage().equals(SearchException.ASSET_NOT_READY))
			  {
			    throw new MetasearchException(MetasearchException.ASSET_NOT_FETCHED);
			  }
			  /*
			   * Unexpected error
			   */
	    	log.error("nextAsset() search exception: " + searchException);
	    	throw new MetasearchException(MetasearchException.METASEARCH_ERROR);
			}
			catch (Throwable throwable)
			{
	    	log.error("nextAsset() general: ", throwable);
				throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
			}
		}
		/*
		 * Finally, return the next Asset from the queue
		 */
    asset = getAsset();
  	log.debug("AssetIterator.nextAsset() returns asset at index " + index + ", vector size = " + assetVectorSize());
    return asset;
  }

	/*
	 * Helpers
	 */

  private int getMaximumRecords()
  {
    return sessionContext.getInt("maxRecords");
  }

	/**
	 * Fetch an Integer property value
	 * @param searchProperties Property list (search characteristics, provided by our caller)
	 * @name Property name to lookup
	 * @return Property value (cast as an Integer)
	 */
	private Integer getIntegerProperty(org.osid.shared.Properties searchProperties,
																 		 String name)
																 		 throws org.osid.shared.SharedException
	{
		return (Integer) searchProperties.getProperty(name);
	}

	/*
	 * Asset queue
	 */

	/**
	 * Get the next asset from the queue, increment the index
	 * @return The next available asset (the asset is removed from the queue)
	 */
	private synchronized org.osid.repository.Asset getAsset()
	{
 		org.osid.repository.Asset asset = (org.osid.repository.Asset) assetVector.elementAt(0);

 		assetVector.removeElementAt(0);
		index++;

 		return asset;
 	}

	/**
	 * Add an Asset to the end of the queue
	 * @param asset Asset to add
	 * @return the logical "size" of the queue
	 */
	private synchronized int addAsset(org.osid.repository.Asset asset)
	{
		assetVector.addElement(asset);
		return ++populated;
 	}

	/**
	 * Get the size of the "physical" queue (the size() of the vector)
	 * @return Queue size (count of queued Asset elements)
	 */
	private synchronized int assetVectorSize()
	{
		return assetVector.size();
 	}

	/*
	 * Populate the Asset queue
	 */
	private void populateAssetQueue()
	throws org.osid.repository.RepositoryException, org.osid.shared.SharedException

  {
  	HashMap		parameterMap;
  	int				assetsAdded;

		/*
		 * Search properties
		 */
  	parameterMap = new HashMap();
  	parameterMap.put("searchString", "");
  	parameterMap.put("database", database);

  	parameterMap.put("guid", searchProperties.getProperty("guid"));
  	parameterMap.put("url", searchProperties.getProperty("baseUrl"));

  	parameterMap.put("sortBy", searchProperties.getProperty("sortBy"));
  	parameterMap.put("maxRecords", getIntegerProperty(searchProperties, "maxRecords"));
		/*
		 * Search type (internal use only)
		 */
  	parameterMap.put("action", "requestResults");
		/*
		 * Starting record, page size
		 */
		sessionContext.putInt("startRecord", startRecord);
		sessionContext.putInt("pageSize", pageSize);
		/*
		 * Send the "more results" request, parse the server response
		 */
		queryBase.parseRequest(parameterMap);
		queryBase.doQuery();

  	searchResult.initialize(queryBase);
		searchResult.doParse();
		/*
		 * Save the assets (matching records) returned frm the server
		 *
		 * These have been stored in an intermediate list of MatchItem objects,
		 * largely as "PartPairs", a part id/part content pair.
		 */
		assetsAdded = 0;
	  for (Iterator iterator = searchResult.iterator(); iterator.hasNext(); )
	  {
  		org.osid.repository.Asset 		asset;
  		org.osid.repository.Record		record;
  		org.osid.repository.Part			part;

  		MatchItem item;
  		Iterator	partPairIterator;

			item 	= (MatchItem) iterator.next();
			/*
			 * Create a new Asset (what "content"?)
			 */
			asset = new Asset(item.getDisplayName(), item.getDescription(),
  											getId(), repositoryId);
			asset.updateContent("");
			/*
			 * and Record
			 */
			record = asset.createRecord(RecordStructure.getInstance().getId());
			/*
			 * Populate the Record with all available Parts
			 */
			partPairIterator = item.partPairIterator();
			while (partPairIterator.hasNext())
			{
				MatchItem.PartPair partPair = (MatchItem.PartPair) partPairIterator.next();
				record.createPart(partPair.getId(), partPair.getValue());
			}
			/*
			 * Save this asset
			 */
			addAsset(asset);
			assetsAdded++;

//		log.debug("populate() Added " + asset
//					+  	 ", vector size = "  + assetVectorSize()
//					+    ", populated = "    + populated);

			if (populated >= getMaximumRecords())
			{
				break;
			}
		}
		/*
		 * Update the starting record number
		 */
		startRecord += assetsAdded; // WAS: Math.min(pageSize, assetsAdded);
		sessionContext.putInt("startRecord", startRecord);
		sessionContext.putInt("pageSize", pageSize);
  }

	private int idCount = 1;
	private synchronized String getId()
	{
		return String.valueOf(idCount++);
	}
}
