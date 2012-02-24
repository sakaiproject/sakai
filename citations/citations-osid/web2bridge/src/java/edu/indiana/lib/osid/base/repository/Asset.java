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
public abstract class Asset implements org.osid.repository.Asset
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

    public org.osid.shared.Id getRepository()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public java.io.Serializable getContent()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateContent(java.io.Serializable content)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void addAsset(org.osid.shared.Id assetId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void removeAsset(org.osid.shared.Id assetId
                          , boolean includeChildren)
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

    public org.osid.repository.Record createRecord(org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void inheritRecordStructure(org.osid.shared.Id assetId
                                     , org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void copyRecordStructure(org.osid.shared.Id assetId
                                  , org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void deleteRecord(org.osid.shared.Id recordId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordIterator getRecords()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordIterator getRecordsByRecordStructure(org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
	}

    public org.osid.repository.RecordStructureIterator getRecordStructures()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordStructure getContentRecordStructure()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Record getRecord(org.osid.shared.Id recordId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Part getPart(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public java.io.Serializable getPartValue(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.PartIterator getPartByPart(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.ObjectIterator getPartValueByPart(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public long getEffectiveDate()
    throws org.osid.repository.RepositoryException
    {
			 throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateEffectiveDate(long effectiveDate)
    throws org.osid.repository.RepositoryException
    {
			 throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public long getExpirationDate()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateExpirationDate(long expirationDate)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.ObjectIterator getPartValuesByPartStructure(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.PartIterator getPartsByPartStructure(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }
}
