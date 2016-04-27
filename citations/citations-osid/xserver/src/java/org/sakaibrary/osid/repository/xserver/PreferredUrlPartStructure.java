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
public class PreferredUrlPartStructure
implements org.osid.repository.PartStructure
{
    private org.osid.shared.Id URL_PART_STRUCTURE_ID = null;
    private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
  		                                          "preferredUrl", "PreferredURL" );
    private String displayName = "PreferredURL";
    private String description = "The preferred URL should be persistent, pointing "
                               + "to the same resource over time";
    private boolean mandatory = false;
    private boolean populatedByRepository = false;
    private boolean repeatable = true;
    
	private static PreferredUrlPartStructure urlPartStructure;

    private PreferredUrlPartStructure()
    {
        try
        {
            this.URL_PART_STRUCTURE_ID = Managers.getIdManager().getId(
            		"2c7464123410800d6d751h2016821340t");
        }
        catch (Throwable t)
        {
        	log.warn( "PreferredUrlPartStructure() failed to get partStructure id: "
					+ t.getMessage() );
        }
    }

	protected static synchronized PreferredUrlPartStructure getInstance()
	{
		if( urlPartStructure == null ) {
			urlPartStructure = new PreferredUrlPartStructure();
		}
		return urlPartStructure;
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
        return this.URL_PART_STRUCTURE_ID;
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
