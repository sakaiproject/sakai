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

@Slf4j
public class Asset implements org.osid.repository.Asset
{
	private static final long serialVersionUID = 1L;

	private org.osid.id.IdManager idManager = Managers.getIdManager();

  // Types
  private org.osid.shared.Type assetType = new Type( "sakaibrary", "asset",
      "citation", "Citation for Scholarly Resources" );
  private org.osid.shared.Type recordStructureType = new Type( "sakaibrary",
      "recordStructure", "citation", "Citation for Scholarly Resources" );

  // Asset attributes
  private org.osid.shared.Id id = null;
  private org.osid.shared.Id repositoryId = null;
  private String displayName = null;
  private String description = null;
  private java.util.Vector recordVector = new java.util.Vector();
  private java.io.Serializable content = null;

  protected Asset(String displayName
      , String description
      , String idString
      , org.osid.shared.Id repositoryId)
    throws org.osid.repository.RepositoryException
  {
    this.displayName = displayName;
    this.description = description;
    this.repositoryId = repositoryId;

    try
    {
      this.id = idManager.getId(idString);
    }
    catch (Throwable t)
    {
    	log.warn( t.getMessage() );
    }

  }

  public String getDisplayName()
    throws org.osid.repository.RepositoryException
  {
    return this.displayName;
  }

  public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
  {
	  this.displayName = displayName;  
  }

  public String getDescription()
    throws org.osid.repository.RepositoryException
  {
    return this.description;
  }

  public void updateDescription(String description)
    throws org.osid.repository.RepositoryException
  {
	  this.description = description;
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
    this.content = content;    
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
    if (assetType == null)
    {
      throw new org.osid.repository.RepositoryException(org.osid.shared.SharedException.NULL_ARGUMENT);
    }
    throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
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
    	log.warn(t.getMessage());
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
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
    	log.warn(t.getMessage());
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
    	log.warn(t.getMessage());
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
  }

  public org.osid.shared.Type getAssetType()
    throws org.osid.repository.RepositoryException
  {
    return this.assetType;
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
    	log.warn(t.getMessage());
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
    	log.warn(t.getMessage());
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
    	log.warn(t.getMessage());
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
    	log.warn(t.getMessage());
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
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
    	log.warn(t.getMessage());
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
    	log.warn( t.getMessage() );
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

    if ( (!recordStructureType.isEqual(this.recordStructureType)) )
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
