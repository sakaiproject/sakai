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
public class EndPagePartStructure implements org.osid.repository.PartStructure
{
	private org.osid.shared.Id END_PAGE_PART_STRUCTURE_ID = null;
	private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
			"endPage", "Page number that the resource content ends on" );
	private String displayName = "End Page";
	private String description = "Page number that the resource content ends on";
	private boolean mandatory = false;
	private boolean populatedByRepository = false;
	private boolean repeatable = false;
	
	private static EndPagePartStructure endPagePartStructure;

	private EndPagePartStructure()
	{
		try {
			this.END_PAGE_PART_STRUCTURE_ID = Managers.getIdManager().getId(
			"x323sd84h8kr98239823h543tg0982340");
		} catch (Throwable t) {
			log.warn( "EndPagePartStructure() failed to get partStructure id: "
					+ t.getMessage() );
		}
	}
	
	protected static synchronized EndPagePartStructure getInstance()
	{
		if( endPagePartStructure == null ) {
			endPagePartStructure = new EndPagePartStructure();
		}
		return endPagePartStructure;
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
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.Id getId()
	throws org.osid.repository.RepositoryException
	{
		return this.END_PAGE_PART_STRUCTURE_ID;
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
