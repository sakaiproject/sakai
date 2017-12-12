package edu.indiana.lib.osid.base.repository.http;

import lombok.extern.slf4j.Slf4j;

import org.osid.OsidException;
import org.osid.repository.PartStructure;
import org.osid.repository.RepositoryException;
import org.osid.shared.Id;

/*******************************************************************************
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
*******************************************************************************/
@Slf4j
public class OpenUrlPartStructure implements PartStructure
{
    private Id OPENURL_PART_STRUCTURE_ID = null;
    private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
    		"openUrl", "OpenURL" );
    private String displayName = "OpenURL";
    private String description = "An OpenURL is a URL conforming to the OpenURL" +
    		" standard for describing contextual information about electronic" +
    		" resources.";
    private boolean mandatory = false;
    private boolean populatedByRepository = false;
    private boolean repeatable = false;

	private static OpenUrlPartStructure openUrlPartStructure;

    private OpenUrlPartStructure()
    {
        try
        {
            this.OPENURL_PART_STRUCTURE_ID = Managers.getIdManager().getId(
            		"2c7464123410800d6d751h2016821340s");
        }
        catch (Throwable t)
        {
        	log.error(t.getMessage(), t);
        }
    }

		protected static synchronized OpenUrlPartStructure getInstance()
		{
			if( openUrlPartStructure == null ) {
				openUrlPartStructure = new OpenUrlPartStructure();
			}
			return openUrlPartStructure;
		}

		/**
		 * Public method to fetch the PartStructure ID
		 */
		public static Id getPartStructureId()
		{
			Id id = null;

			try
			{
				id = getInstance().getId();
			}
			catch (RepositoryException ignore) { }

 			return id;
		}

    public String getDisplayName()
    throws RepositoryException
    {
        return this.displayName;
    }

    public String getDescription()
    throws RepositoryException
    {
        return this.description;
    }

    public boolean isMandatory()
    throws RepositoryException
    {
        return this.mandatory;
    }

    public boolean isPopulatedByRepository()
    throws RepositoryException
    {
        return this.populatedByRepository;
    }

    public boolean isRepeatable()
    throws RepositoryException
    {
        return this.repeatable;
    }

    public void updateDisplayName(String displayName)
    throws RepositoryException
    {
        throw new RepositoryException(OsidException.UNIMPLEMENTED);
    }

    public Id getId()
    throws RepositoryException
    {
        return this.OPENURL_PART_STRUCTURE_ID;
    }

    public org.osid.shared.Type getType()
    throws RepositoryException
    {
        return this.type;
    }

    public RecordStructure getRecordStructure()
    throws RepositoryException
    {
        return RecordStructure.getInstance();
    }

    public boolean validatePart(org.osid.repository.Part part)
    throws RepositoryException
    {
        return true;
    }

    public org.osid.repository.PartStructureIterator getPartStructures()
    throws RepositoryException
    {
        return new PartStructureIterator(new java.util.Vector());
    }
}
