package edu.indiana.lib.osid.base.repository.http;

import java.io.*;
import java.util.*;

import edu.indiana.lib.twinpeaks.search.*;
import edu.indiana.lib.twinpeaks.util.*;
import lombok.extern.slf4j.Slf4j;


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
public class RepositoryManager extends edu.indiana.lib.osid.base.repository.RepositoryManager
{
    private org.osid.repository.Repository repository = null;
    private org.osid.id.IdManager idManager = null;
    private org.osid.OsidContext context = null;
    private java.util.Properties configuration = null;
    private java.util.Vector repositoryVector = new java.util.Vector();

		public RepositoryManager()
		{
		}

    public org.osid.OsidContext getOsidContext()
    throws org.osid.repository.RepositoryException
    {
        return context;
    }

		private java.util.Vector asVector(Object value)
		{
			java.util.Vector vector = new java.util.Vector(1);

			vector.addElement(value);
			return vector;
		}


    public void assignOsidContext(org.osid.OsidContext context)
    throws org.osid.repository.RepositoryException
    {
        this.context = context;
    }

    /*
     * Phase two configuration
     */
    public void assignConfiguration(java.util.Properties configuration)
    throws org.osid.repository.RepositoryException
    {
				java.io.InputStream		configStream 	= null;
				org.osid.OsidContext 	osidContext		= this.context;
				String								contextName;
				/*
				 * Establish our configuration
				 */
				try
				{
						configStream = getConfigStream("/searchsource.xml");
						SearchSource.populate(configStream);
      	}
      	catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
        finally
        {
        	if (configStream != null) try { configStream.close(); } catch (Throwable ignore) { }
        }
				/*
				 * Verify mandatory configuration values
				 */
        try
        {
            String idImplementation = SearchSource.getMandatoryGlobalConfigurationValue("osid_20_Id_Implementation");

						/*
						 * Get our unique OSID context object
						 */
						osidContext = this.context;
						/*
						 * Load the ID manager
						 */
            this.idManager = (org.osid.id.IdManager)
            											edu.indiana.lib.osid.base.loader.OsidLoader.getManager(
																                "org.osid.id.IdManager",
																                idImplementation,
																                osidContext,
																                new java.util.Properties());
						Managers.setIdManager(this.idManager);
						/*
						 * Set up our Repositories
						 */
					  if (!SearchSource.isSourceListPopulated())
					  {
	  					return;
	  				}
						/*
						 * Add in the enabled choices
						 */
						for (Iterator i = SearchSource.getSearchListIterator(); i.hasNext(); )
						{
							SearchSource  ss = (SearchSource) i.next();
							Type 					searchType;

	           	if (!ss.isEnabled())
	           	{
	          		continue;
	           	}

              log.debug("name = " + ss.getName());
              log.debug("description = " + ss.getDescription());
              log.debug("id = " + ss.getId());
              log.debug("authority = " + ss.getAuthority());
              log.debug("domain = " + ss.getDomain());
              log.debug("searchType = " + ss.getSearchType());
              log.debug("query handler = " + ss.getQueryHandlerClassName());
              log.debug("result handler = " + ss.getSearchResultHandlerClassName());
              log.debug("osid context = " + this.context);
              /*
               * Set up the search-type and add the new Repository
               */
							searchType = new Type(ss.getAuthority(), ss.getDomain(), ss.getSearchType(), ss.getTypeDescription());

              this.repositoryVector.addElement(new Repository(ss.getName(),
                                                              ss.getDescription(),
                                                              ss.getId(),
                                                              asVector(searchType),
                                                              asVector(ss.getQueryHandlerClassName()),
                                                              asVector(ss.getSearchResultHandlerClassName()),
                                                              this.idManager));
						}
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());

            if (t instanceof org.osid.repository.RepositoryException)
            {
                throw new org.osid.repository.RepositoryException(t.getMessage());
            }
            else
            {
                throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
            }
        }
    }

    public org.osid.repository.RepositoryIterator getRepositories()
    throws org.osid.repository.RepositoryException
    {
        return new RepositoryIterator(this.repositoryVector);
    }

    public org.osid.repository.RepositoryIterator getRepositoriesByType(org.osid.shared.Type repositoryType)
    throws org.osid.repository.RepositoryException
    {
        if (repositoryType == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }

        java.util.Vector result = new java.util.Vector();
        org.osid.repository.RepositoryIterator repositoryIterator = getRepositories();

        while (repositoryIterator.hasNextRepository())
        {
            org.osid.repository.Repository nextRepository = repositoryIterator.nextRepository();

            if (nextRepository.getType().isEqual(repositoryType))
            {
                result.addElement(nextRepository);
            }
        }
        return new RepositoryIterator(result);
    }

    public org.osid.repository.Repository getRepository(org.osid.shared.Id repositoryId)
    throws org.osid.repository.RepositoryException
    {
        if (repositoryId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            org.osid.repository.RepositoryIterator repositoryIterator = getRepositories();
            while (repositoryIterator.hasNextRepository())
            {
                org.osid.repository.Repository nextRepository = repositoryIterator.nextRepository();
                if (nextRepository.getId().isEqual(repositoryId))
                {
                    return nextRepository;
                }
            }
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_ID);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.Asset getAsset(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
        if (assetId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            org.osid.repository.RepositoryIterator repositoryIterator = getRepositories();
            while (repositoryIterator.hasNextRepository())
            {
                org.osid.repository.Repository nextRepository = repositoryIterator.nextRepository();
                try
                {
                    org.osid.repository.Asset asset = nextRepository.getAsset(assetId);
                    return asset;
                }
                catch (Throwable t) {}
            }
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
        throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_ID);
    }

    public org.osid.repository.Asset getAssetByDate(org.osid.shared.Id assetId
                                                  , long date)
    throws org.osid.repository.RepositoryException
    {
        if (assetId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            org.osid.repository.RepositoryIterator repositoryIterator = getRepositories();
            while (repositoryIterator.hasNextRepository())
            {
                org.osid.repository.Repository nextRepository = repositoryIterator.nextRepository();
                try
                {
                    org.osid.repository.Asset asset = nextRepository.getAssetByDate(assetId,date);
                    return asset;
                }
                catch (Throwable t) {}
            }
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
        throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_ID);
    }

    public org.osid.shared.LongValueIterator getAssetDates(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
        if (assetId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        java.util.Vector result = new java.util.Vector();
        try
        {
            org.osid.repository.RepositoryIterator repositoryIterator = getRepositories();
            while (repositoryIterator.hasNextRepository())
            {
                org.osid.repository.Repository nextRepository = repositoryIterator.nextRepository();
                org.osid.shared.LongValueIterator longValueIterator = repository.getAssetDates(assetId);
                while (longValueIterator.hasNextLongValue())
                {
                    result.addElement(new Long(longValueIterator.nextLongValue()));
                }
            }
            return new LongValueIterator(result);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.AssetIterator getAssetsBySearch(org.osid.repository.Repository[] repositories
                                                             , java.io.Serializable searchCriteria
                                                             , org.osid.shared.Type searchType
                                                             , org.osid.shared.Properties searchProperties)
    throws org.osid.repository.RepositoryException
    {
        if (repositories == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            java.util.Vector results = new java.util.Vector();
            for (int j=0; j < repositories.length; j++)
            {
                org.osid.repository.Repository nextRepository = repositories[j];
                //optionally add a separate thread here
                try
                {
                    org.osid.repository.AssetIterator assetIterator =
                        nextRepository.getAssetsBySearch(searchCriteria,searchType,searchProperties);
                    while (assetIterator.hasNextAsset())
                    {
                        results.addElement(assetIterator.nextAsset());
                    }
                }
                catch (Throwable t)
                {
                    log.warn(t.getMessage());
                }
            }
            return new AssetIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.shared.Id copyAsset(org.osid.repository.Repository repository
                                      , org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
        if ((repository == null) || (assetId == null))
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getRepositoryTypes()
    throws org.osid.repository.RepositoryException
    {
        java.util.Vector results = new java.util.Vector();
        try
        {
            results.addElement(new Type("sakaibrary", "repository", "metasearch"));
            return new TypeIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public void osidVersion_2_0()
    throws org.osid.repository.RepositoryException
    {
    }

    public java.io.InputStream getConfigStream(String fileName)
		throws org.osid.repository.RepositoryException
    {
				InputStream fileIn = this.getClass().getResourceAsStream(fileName);

				if (fileIn == null)
				{
	        /*
	         * Not found
	         */
	        throw new org.osid.repository.RepositoryException(org.osid.OsidException.CONFIGURATION_ERROR);
	      }
	      return fileIn;
		}
}