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
public abstract class Repository
implements org.osid.repository.Repository
{
    public String getDisplayName()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public String getDescription()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateDescription(String description)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Type getType()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Asset createAsset(String displayName
                                               , String description
                                               , org.osid.shared.Type assetType)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void deleteAsset(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.AssetIterator getAssets()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.AssetIterator getAssetsByType(org.osid.shared.Type assetType)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getAssetTypes()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordStructureIterator getRecordStructures()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordStructureIterator getMandatoryRecordStructures(org.osid.shared.Type assetType)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getSearchTypes()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getStatusTypes()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Type getStatus(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public boolean validateAsset(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void invalidateAsset(org.osid.shared.Id assetId)
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

    public org.osid.repository.AssetIterator getAssetsBySearch(java.io.Serializable searchCriteria
                                                             , org.osid.shared.Type searchType
                                                             , org.osid.shared.Properties searchProperties)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id copyAsset(org.osid.repository.Asset asset)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordStructureIterator getRecordStructuresByType(org.osid.shared.Type recordStructureType)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.PropertiesIterator getProperties()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Properties getPropertiesByType(org.osid.shared.Type propertiesType)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.TypeIterator getPropertyTypes()
    throws org.osid.repository.RepositoryException
   {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    protected void addAsset(org.osid.repository.Asset asset)
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public boolean supportsUpdate()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public boolean supportsVersioning()
    throws org.osid.repository.RepositoryException
    {
		  throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }
}
