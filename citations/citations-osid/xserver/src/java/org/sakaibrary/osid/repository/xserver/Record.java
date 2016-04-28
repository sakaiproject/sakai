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
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */
@Slf4j
public class Record
implements org.osid.repository.Record
{
    private java.util.Vector partVector = new java.util.Vector();
    private org.osid.id.IdManager idManager = null;
    private org.osid.shared.Id recordStructureId = null;
    private String displayName = null;
    private org.osid.shared.Id id = null;

    public String getDisplayName()
    throws org.osid.repository.RepositoryException
    {
        return this.displayName;
    }

    public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
    {
        return this.id;
    }

    protected Record(org.osid.shared.Id recordStructureId
                   , org.osid.id.IdManager idManager)
    throws org.osid.repository.RepositoryException
    {
        try
        {
            this.idManager = idManager;
            this.recordStructureId = recordStructureId;
            this.id = idManager.createId();
        }
        catch (Throwable t)
        {
            log.warn(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.Part createPart(org.osid.shared.Id partStructureId
                                             , java.io.Serializable value)
    throws org.osid.repository.RepositoryException
    {
        try
        {
            org.osid.repository.Part part = new Part(partStructureId,value,this.idManager);                    
            this.partVector.addElement(part);
            return part;
        }
        catch (Throwable t)
        {
            log.warn(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public void deletePart(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
        try
        {
            for (int i=0, size = this.partVector.size(); i < size; i++)
            {
                org.osid.repository.Part part = (org.osid.repository.Part)this.partVector.elementAt(i);
                if (part.getId().isEqual(partId))
                {
                    this.partVector.removeElementAt(i);
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

    public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
    {
        throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.PartIterator getParts()
    throws org.osid.repository.RepositoryException
    {
        return new PartIterator(this.partVector);
    }

    public org.osid.repository.RecordStructure getRecordStructure()
    throws org.osid.repository.RepositoryException
    {
        try
        {
            if (this.recordStructureId.isEqual(RecordStructure.getInstance().getId()))
            {
                return new RecordStructure();
            }
            else
            {
                return null;
            }
        }
        catch (Throwable t)
        {
            log.warn(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
        }
    }

    public boolean isMultivalued()
    throws org.osid.repository.RepositoryException
    {
        return false;
    }
}
