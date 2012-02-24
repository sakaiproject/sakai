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
public abstract class Record implements org.osid.repository.Record
{
    public String getDisplayName()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.Part createPart(org.osid.shared.Id partStructureId
                                             , java.io.Serializable value)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void deletePart(org.osid.shared.Id partId)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.PartIterator getParts()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.repository.RecordStructure getRecordStructure()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public boolean isMultivalued()
    throws org.osid.repository.RepositoryException
    {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }
}
