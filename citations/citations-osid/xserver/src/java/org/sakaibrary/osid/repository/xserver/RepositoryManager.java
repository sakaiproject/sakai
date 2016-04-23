/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaibrary.osid.repository.xserver;

import lombok.extern.slf4j.Slf4j;

/**
 * RepositoryManager manages Repositories.
 *
 * @author Gaurav Bhatnagar (gbhatnag@umich.edu)
 * @version
 */
@Slf4j
public class RepositoryManager implements org.osid.repository.RepositoryManager
{
	// constants
	private static final long serialVersionUID = 1L;
	public static final String REPOSITORY_DISPLAY_NAME = "MetaLib X-Server";
	public static final String REPOSITORY_DESCRIPTION = "UM metasearch engine for searching library licensed digital content";
	public static final String REPOSITORY_ID = "XSERVER01";

	private org.osid.id.IdManager idManager = null;
	private org.osid.OsidContext context = null;
	private java.util.Vector<Repository> repositoryVector = new java.util.Vector<Repository>();

	public org.osid.OsidContext getOsidContext()
	throws org.osid.repository.RepositoryException {
		return context;
	}

	public void assignOsidContext(org.osid.OsidContext context)
	throws org.osid.repository.RepositoryException {
		this.context = context;
	}

	public void assignConfiguration(java.util.Properties configuration)
	throws org.osid.repository.RepositoryException
	{
		try
		{
			// get idManager TODO get from config
			this.idManager = (org.osid.id.IdManager)
			org.sakaibrary.osid.loader.OsidLoader.getManager(
					"org.osid.id.IdManager",
					"org.sakaiproject.component.osid.id",
					this.context,
					new java.util.Properties() );

			Managers.setIdManager(this.idManager);

			// supported search types TODO get from config
			java.util.Vector<Type> searchTypes = new java.util.Vector<Type>();
			searchTypes.add( new Type( "sakaibrary", "search", "asynchMetasearch" ) );

			repositoryVector.add( new Repository( REPOSITORY_DISPLAY_NAME,
					REPOSITORY_DESCRIPTION,
					REPOSITORY_ID,
					searchTypes,
					this.idManager ) );
		}
		catch (Throwable t)
		{
			log.warn( "RepositoryManager.assignConfiguration() failed in reading " +
					"configuration properties or creating a new Repository: " +
					t.getMessage(), t );

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

	public org.osid.repository.Repository createRepository(String displayName
			, String description
			, org.osid.shared.Type repositoryType)
	throws org.osid.repository.RepositoryException
	{
		throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
	}

	public void deleteRepository(org.osid.shared.Id repositoryId)
	throws org.osid.repository.RepositoryException
	{
		if (repositoryId == null)
		{
			throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
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
			log.warn(t.getMessage());
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
			log.warn(t.getMessage());
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
			log.warn(t.getMessage());
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
				org.osid.shared.LongValueIterator longValueIterator = nextRepository.getAssetDates(assetId);
				while (longValueIterator.hasNextLongValue())
				{
					result.addElement(new Long(longValueIterator.nextLongValue()));
				}
			}
			return new LongValueIterator(result);
		}
		catch (Throwable t)
		{
			log.warn(t.getMessage());
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

		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED );
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
			results.addElement( new Type( "sakaibrary", "repository", "metasearch" ) );
			return new TypeIterator( results );
		}
		catch( Throwable t )
		{
			log.warn( t.getMessage() );
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED );
		}
	}

	public void osidVersion_2_0()
	throws org.osid.repository.RepositoryException
	{
		log.debug( "osidVersion_2_0() called" );
	}
}
