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
public class SubjectPartStructure
implements org.osid.repository.PartStructure
{
    private org.osid.shared.Id SUBJECT_PART_STRUCTURE_ID = null;
    private org.osid.shared.Type type = new Type("mit.edu", "partStructure",
    		"subject", "Subject");
    private String displayName = "Subject";
    private String description = "Typically, Subject will be expressed as keywords, key phrases or classification codes that describe a topic of the resource.";
    private boolean mandatory = false;
    private boolean populatedByRepository = false;
    private boolean repeatable = true;
    
	private static SubjectPartStructure subjectPartStructure;

    private SubjectPartStructure()
    {
        try
        {
            this.SUBJECT_PART_STRUCTURE_ID = Managers.getIdManager().getId(
            		"a8a1541f201080006d751920168000100");
        }
        catch (Throwable t)
        {
        	log.warn( "SubjectPartStructure() failed to get partStructure id: "
					+ t.getMessage() );
        }
    }

	protected static synchronized SubjectPartStructure getInstance()
	{
		if( subjectPartStructure == null ) {
			subjectPartStructure = new SubjectPartStructure();
		}
		return subjectPartStructure;
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

    public boolean isMandatory()
    throws org.osid.repository.RepositoryException
    {
        return this.mandatory;
    }

    public boolean isPopulatedByRepository()
    throws org.osid.repository.RepositoryException
    {
        return this.populatedByRepository;
    }

    public boolean isRepeatable()
    throws org.osid.repository.RepositoryException
    {
        return this.repeatable;
    }

    public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
    {
        throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
    {
        return this.SUBJECT_PART_STRUCTURE_ID;
    }

    public org.osid.shared.Type getType()
    throws org.osid.repository.RepositoryException
    {
        return this.type;
    }

    public org.osid.repository.RecordStructure getRecordStructure()
    throws org.osid.repository.RepositoryException
    {
        return RecordStructure.getInstance();
    }

    public boolean validatePart(org.osid.repository.Part part)
    throws org.osid.repository.RepositoryException
    {
        return true;
    }

    public org.osid.repository.PartStructureIterator getPartStructures()
    throws org.osid.repository.RepositoryException
    {
        return new PartStructureIterator(new java.util.Vector());
    }
}
