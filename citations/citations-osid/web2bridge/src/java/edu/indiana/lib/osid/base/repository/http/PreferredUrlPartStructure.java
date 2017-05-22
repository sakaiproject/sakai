package edu.indiana.lib.osid.base.repository.http;

/*******************************************************************************
 * Copyright (c) 2003, 2004, 2005, 2008 The Sakai Foundation
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

public class PreferredUrlPartStructure implements org.osid.repository.PartStructure
{
  private org.osid.shared.Id PREFERREDURL_PART_STRUCTURE_ID = null;

  private org.osid.shared.Type type = new Type( "sakaibrary", "partStructure",
  		                                          "preferredUrl", "PreferredURL" );
  private String displayName = "PreferredURL";
  private String description = "The preferred URL should be persistent, pointing "
                             + "to the same resource over time";
  private boolean mandatory = false;
  private boolean populatedByRepository = false;
  private boolean repeatable = false;

	private static PreferredUrlPartStructure preferredUrlPartStructure;

  private PreferredUrlPartStructure()
  {
    try
    {
      PREFERREDURL_PART_STRUCTURE_ID = Managers.getIdManager().getId("2c7464123410800d6d751h2016821340t");
    }
    catch (Throwable t)
    {
    	throw new RuntimeException(t.toString());
    }
  }

  protected static synchronized PreferredUrlPartStructure getInstance()
	{
		if (preferredUrlPartStructure == null)
	  {
		  preferredUrlPartStructure = new PreferredUrlPartStructure();
		}
		return preferredUrlPartStructure;
	}

	/**
	 * Public method to fetch the PartStructure ID
	 */
	public static org.osid.shared.Id getPartStructureId()
	{
		org.osid.shared.Id id = null;

		try
		{
			id = getInstance().getId();
		}
		catch (org.osid.repository.RepositoryException ignore) { }

 		return id;
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

  public boolean isRepeatable() throws org.osid.repository.RepositoryException
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
    return this.PREFERREDURL_PART_STRUCTURE_ID;
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
