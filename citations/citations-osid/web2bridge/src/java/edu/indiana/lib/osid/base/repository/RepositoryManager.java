package edu.indiana.lib.osid.base.repository;

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
public abstract class RepositoryManager
implements org.osid.repository.RepositoryManager
{
    public org.osid.OsidContext getOsidContext()
    throws org.osid.repository.RepositoryException
    {
	 		throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
   }

    public void assignOsidContext(org.osid.OsidContext context)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void assignConfiguration(java.util.Properties configuration)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
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
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RepositoryIterator getRepositories()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RepositoryIterator getRepositoriesByType(org.osid.shared.Type repositoryType)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Repository getRepository(org.osid.shared.Id repositoryId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Asset getAsset(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Asset getAssetByDate(org.osid.shared.Id assetId
                                                  , long date)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.LongValueIterator getAssetDates(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.AssetIterator getAssetsBySearch(org.osid.repository.Repository[] repositories
                                                             , java.io.Serializable searchCriteria
                                                             , org.osid.shared.Type searchType
                                                             , org.osid.shared.Properties searchProperties)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id copyAsset(org.osid.repository.Repository repository
                                      , org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getRepositoryTypes()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void osidVersion_2_0()
    throws org.osid.repository.RepositoryException
    {
    	/*
    	 * What should this do?  The "real" implememtation does nothing.
    	 */
    }
}
