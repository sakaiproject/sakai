package edu.indiana.lib.osid.base.repository.http;

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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */
@Slf4j
public class Asset extends edu.indiana.lib.osid.base.repository.Asset
{
    private org.osid.shared.Type assetType = new Type("mit.edu", "asset", "library_content");
    private org.osid.shared.Type recordStructureType = new Type("mit.edu", "recordStructure", "library_content");
    private org.osid.shared.Type dcRecordStructureType = new Type("mit.edu", "recordStructure", "dublinCore");
    private org.osid.shared.Type vueRecordStructureType = new Type("tufts.edu", "recordStructure", "vue");

		private org.osid.id.IdManager idManager = Managers.getIdManager();
    private org.osid.shared.Id id = null;
    private org.osid.shared.Id repositoryId = null;
    private String idString = null;
    private String displayName = null;
    private String description = null;
    private org.osid.shared.Type type = null;
    private java.util.Vector recordVector = new java.util.Vector();
    private String content = null;


    protected Asset(String displayName
                  , String description
                  , String idString
                  , org.osid.shared.Id repositoryId)
    								throws org.osid.repository.RepositoryException
    {
        this.displayName = displayName;
        this.description = description;
        this.repositoryId = repositoryId;
        this.type = new Type("mit.edu", "asset", "library_content");

        try
        {
            this.id = idManager.getId(idString);
         }
        catch (Throwable t)
        {
            log.error(t.getMessage());
        }

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

    public org.osid.shared.Id getRepository()
    throws org.osid.repository.RepositoryException
    {
        return this.repositoryId;
    }

    public java.io.Serializable getContent()
    throws org.osid.repository.RepositoryException
    {
        return this.content;
    }

    public void updateContent(java.io.Serializable content)
    throws org.osid.repository.RepositoryException
    {
			this.content = (String) content;
    }

    public org.osid.repository.AssetIterator getAssets()
    throws org.osid.repository.RepositoryException
    {
        return new AssetIterator(new java.util.Vector());
    }

    public org.osid.repository.AssetIterator getAssetsByType(org.osid.shared.Type assetType)
    throws org.osid.repository.RepositoryException
    {
        if (assetType == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        return new AssetIterator(new java.util.Vector());
    }

    public org.osid.repository.Record createRecord(org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
        if (recordStructureId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            org.osid.repository.Record record = new Record(recordStructureId,this.idManager);
            this.recordVector.addElement(record);
            return record;
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public void deleteRecord(org.osid.shared.Id recordId)
    throws org.osid.repository.RepositoryException
    {
        if (recordId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            for (int i=0, size = this.recordVector.size(); i < size; i++)
            {
                org.osid.repository.Record record = (org.osid.repository.Record)this.recordVector.elementAt(i);
                if (record.getId().isEqual(recordId))
                {
                    this.recordVector.removeElementAt(i);
                    return;
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

    public org.osid.repository.RecordIterator getRecords()
    throws org.osid.repository.RepositoryException
    {
        return new RecordIterator(this.recordVector);
    }

    public org.osid.repository.RecordIterator getRecordsByRecordStructure(org.osid.shared.Id recordStructureId)
    throws org.osid.repository.RepositoryException
    {
        if (recordStructureId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            return new RecordIterator(this.recordVector);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.shared.Type getAssetType()
    throws org.osid.repository.RepositoryException
    {
        return this.type;
    }

    public org.osid.repository.RecordStructureIterator getRecordStructures()
    throws org.osid.repository.RepositoryException
    {
        java.util.Vector results = new java.util.Vector();
        results.addElement(new RecordStructure());
        return new RecordStructureIterator(results);
    }

    public org.osid.repository.RecordStructure getContentRecordStructure()
    throws org.osid.repository.RepositoryException
    {
        return new RecordStructure();
    }

    public org.osid.repository.Record getRecord(org.osid.shared.Id recordId)
    throws org.osid.repository.RepositoryException
    {
        if (recordId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            for (int i=0, size = this.recordVector.size(); i < size; i++)
            {
                org.osid.repository.Record record = (org.osid.repository.Record)this.recordVector.elementAt(i);
                if (record.getId().isEqual(recordId))
                {
                    return record;
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

    public org.osid.repository.Part getPart(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
        if (partId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            for (int i=0, size = this.recordVector.size(); i < size; i++)
            {
                org.osid.repository.Record record = (org.osid.repository.Record)this.recordVector.elementAt(i);
                org.osid.repository.PartIterator partIterator = record.getParts();
                while (partIterator.hasNextPart())
                {
                    org.osid.repository.Part part = partIterator.nextPart();
                    if (part.getId().isEqual(partId))
                    {
                        return part;
                    }
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

    public java.io.Serializable getPartValue(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
        org.osid.repository.Part part = getPart(partId);
        return part.getValue();
    }

    public org.osid.repository.PartIterator getPartByPart(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
        if (partStructureId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            java.util.Vector results = new java.util.Vector();
            for (int i=0, size = this.recordVector.size(); i < size; i++)
            {
                org.osid.repository.Record record = (org.osid.repository.Record)this.recordVector.elementAt(i);
                org.osid.repository.PartIterator partIterator = record.getParts();
                while (partIterator.hasNextPart())
                {
                    org.osid.repository.Part part = partIterator.nextPart();
                    if (part.getPartStructure().getId().isEqual(partStructureId))
                    {
                        results.addElement(part);
                    }
                }
            }
            return new PartIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.shared.ObjectIterator getPartValueByPart(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
        java.util.Vector results = new java.util.Vector();
        org.osid.repository.PartIterator partIterator = getPartByPart(partStructureId);
        while (partIterator.hasNextPart())
        {
            results.addElement(partIterator.nextPart().getValue());
        }
        try
        {
            return new ObjectIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.shared.ObjectIterator getPartValuesByPartStructure(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
        if (partStructureId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            java.util.Vector results = new java.util.Vector();
            org.osid.repository.PartIterator partIterator = getPartsByPartStructure(partStructureId);
            while (partIterator.hasNextPart())
            {
                org.osid.repository.Part part = partIterator.nextPart();
                results.addElement(part.getValue());
            }
            return new ObjectIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.PartIterator getPartsByPartStructure(org.osid.shared.Id partStructureId)
    throws org.osid.repository.RepositoryException
    {
        if (partStructureId == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }
        try
        {
            java.util.Vector results = new java.util.Vector();
            org.osid.repository.RecordIterator recordIterator = getRecords();
            while (recordIterator.hasNextRecord())
            {
                org.osid.repository.Record record = recordIterator.nextRecord();
                org.osid.repository.PartIterator partIterator = record.getParts();
                while (partIterator.hasNextPart())
                {
                    org.osid.repository.Part part = partIterator.nextPart();
                    if (part.getPartStructure().getId().isEqual(partStructureId))
                    {
                        results.addElement(part);
                    }
                }
            }
            return new PartIterator(results);
        }
        catch (Throwable t)
        {
            log.error(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.RecordIterator getRecordsByRecordStructureType(org.osid.shared.Type recordStructureType)
    throws org.osid.repository.RepositoryException
    {
        if (recordStructureType == null)
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
        }

        if ( (!recordStructureType.isEqual(this.recordStructureType)) &&
             (!recordStructureType.isEqual(this.dcRecordStructureType)) &&
             (!recordStructureType.isEqual(this.vueRecordStructureType)) )
        {
            throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.UNKNOWN_TYPE);
        }

        java.util.Vector results = new java.util.Vector();
        for (int i=0, size = this.recordVector.size(); i < size; i++)
        {
            org.osid.repository.Record r = (org.osid.repository.Record)this.recordVector.elementAt(i);
            if (r.getRecordStructure().getType().isEqual(recordStructureType))
            {
                results.addElement(r);
            }
        }
        return new RecordIterator(results);
    }
}
