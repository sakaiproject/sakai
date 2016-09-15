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
public class Part
implements org.osid.repository.Part
{
    private org.osid.repository.PartStructure partStructure = null;
    private org.osid.shared.Id partStructureId = null;
    private java.io.Serializable value = null;
    private org.osid.id.IdManager idManager = null;
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

    protected Part(org.osid.shared.Id partStructureId
                 , java.io.Serializable value
                 , org.osid.id.IdManager idManager)
    throws org.osid.repository.RepositoryException
    {
        this.idManager = idManager;
        this.partStructureId = partStructureId;
        this.value = value;
        
        try
        {
            this.id = this.idManager.createId();
			if (partStructureId.isEqual(DateRetrievedPartStructure.getInstance().getId())) {
				this.partStructure = DateRetrievedPartStructure.getInstance();
			} else if (partStructureId.isEqual(EditionPartStructure.getInstance().getId())) {
				this.partStructure = EditionPartStructure.getInstance();
			} else if (partStructureId.isEqual(CreatorPartStructure.getInstance().getId())) {
				this.partStructure = CreatorPartStructure.getInstance();
			} else if (partStructureId.isEqual(DatePartStructure.getInstance().getId())) {
				this.partStructure = DatePartStructure.getInstance();
			} else if (partStructureId.isEqual(DOIPartStructure.getInstance().getId())) {
				this.partStructure = DOIPartStructure.getInstance();
			} else if (partStructureId.isEqual(InLineCitationPartStructure.getInstance().getId())) {
				this.partStructure = InLineCitationPartStructure.getInstance();
			} else if (partStructureId.isEqual(LanguagePartStructure.getInstance().getId())) {
				this.partStructure = LanguagePartStructure.getInstance();
			} else if (partStructureId.isEqual(PublisherPartStructure.getInstance().getId())) {
				this.partStructure = PublisherPartStructure.getInstance();
			} else if (partStructureId.isEqual(IssuePartStructure.getInstance().getId())) {
				this.partStructure = IssuePartStructure.getInstance();
			} else if (partStructureId.isEqual(LocIdentifierPartStructure.getInstance().getId())) {
				this.partStructure = LocIdentifierPartStructure.getInstance();
			} else if (partStructureId.isEqual(SourceTitlePartStructure.getInstance().getId())) {
				this.partStructure = SourceTitlePartStructure.getInstance();
			} else if (partStructureId.isEqual(SubjectPartStructure.getInstance().getId())) {
				this.partStructure = SubjectPartStructure.getInstance();
			} else if (partStructureId.isEqual(TypePartStructure.getInstance().getId())) {
				this.partStructure = TypePartStructure.getInstance();
			} else if (partStructureId.isEqual(NotePartStructure.getInstance().getId())) {
				this.partStructure = NotePartStructure.getInstance();
			} else if (partStructureId.isEqual(IsnIdentifierPartStructure.getInstance().getId())) {
				this.partStructure = IsnIdentifierPartStructure.getInstance();
			} else if (partStructureId.isEqual(URLPartStructure.getInstance().getId())) {
				this.partStructure = URLPartStructure.getInstance();
			} else if (partStructureId.isEqual(PreferredUrlPartStructure.getInstance().getId())) {
				this.partStructure = PreferredUrlPartStructure.getInstance();
			} else if (partStructureId.isEqual(URLLabelPartStructure.getInstance().getId())) {
				this.partStructure = URLLabelPartStructure.getInstance();
			} else if (partStructureId.isEqual(URLFormatPartStructure.getInstance().getId())) {
				this.partStructure = URLFormatPartStructure.getInstance();
			} else if (partStructureId.isEqual(OpenUrlPartStructure.getInstance().getId())) {
				this.partStructure = OpenUrlPartStructure.getInstance();
			} else if (partStructureId.isEqual(PagesPartStructure.getInstance().getId())) {
				this.partStructure = PagesPartStructure.getInstance();
			} else if (partStructureId.isEqual(PublicationLocationPartStructure.getInstance().getId())) {
				this.partStructure = PublicationLocationPartStructure.getInstance();
			} else if (partStructureId.isEqual(RightsPartStructure.getInstance().getId())) {
				this.partStructure = RightsPartStructure.getInstance();
			} else if (partStructureId.isEqual(VolumePartStructure.getInstance().getId())) {
				this.partStructure = VolumePartStructure.getInstance();
			} else if (partStructureId.isEqual(YearPartStructure.getInstance().getId())) {
				this.partStructure = YearPartStructure.getInstance();
			} else if (partStructureId.isEqual(StartPagePartStructure.getInstance().getId())) {
				this.partStructure = StartPagePartStructure.getInstance();
			} else if (partStructureId.isEqual(EndPagePartStructure.getInstance().getId())) {
				this.partStructure = EndPagePartStructure.getInstance();
			}
			
			if (this.partStructure != null)
			{
				this.displayName = this.partStructure.getDisplayName();
			}
        }
        catch (Throwable t)
        {
            log.warn(t.getMessage());
            throw new org.osid.repository.RepositoryException(org.osid.repository.RepositoryException.OPERATION_FAILED);
        }
    }

    public org.osid.repository.Part createPart(org.osid.shared.Id partStructureId
                                             , java.io.Serializable value)
    throws org.osid.repository.RepositoryException
    {
        throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
    }

    public void deletePart(org.osid.shared.Id partStructureId)
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
        return new PartIterator(new java.util.Vector());
    }

    public org.osid.repository.PartStructure getPartStructure()
    throws org.osid.repository.RepositoryException
    {
		return this.partStructure;
    }

    public java.io.Serializable getValue()
    throws org.osid.repository.RepositoryException
    {
        return this.value;
    }

    public void updateValue(java.io.Serializable value)
    throws org.osid.repository.RepositoryException
    {
        this.value = value;
    }
}
