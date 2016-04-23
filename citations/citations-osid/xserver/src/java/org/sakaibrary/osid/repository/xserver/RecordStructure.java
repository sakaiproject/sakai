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
 * @author Massachusetts Institute of Techbology, Sakai Software Development
 * Team
 * @version
 */
@Slf4j
public class RecordStructure
  implements org.osid.repository.RecordStructure
{
  private org.osid.shared.Id id = null;
  private String idString = "af106d4f201080006d751920168000100";
  private String displayName = "Citation";
  private String description = "Encapsulates citation information for " +
  		"resources from scholarly electronic databases or metasearch engines.";
  private String format = "";
  private String schema = "";
  private org.osid.shared.Type type = new Type( "sakaibrary", "recordStructure",
      "citation", "Citation for Scholarly Resources" );
  private boolean repeatable = false;
  
  private static RecordStructure recordStructure = new RecordStructure();

  protected static RecordStructure getInstance()
  {
    return recordStructure;
  }

  protected RecordStructure()
  {
    try
    {
      this.id = Managers.getIdManager().getId(this.idString);
    }
    catch (Throwable t)
    {
      log.warn(t.getMessage());
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

  public String getFormat()
    throws org.osid.repository.RepositoryException
  {
    return this.format;
  }

  public String getSchema()
    throws org.osid.repository.RepositoryException
  {
    return this.schema;
  }

  public org.osid.shared.Type getType()
    throws org.osid.repository.RepositoryException
  {
    return this.type;
  }

  public boolean isRepeatable()
    throws org.osid.repository.RepositoryException
  {
    return this.repeatable;
  }

  public org.osid.shared.Id getId()
    throws org.osid.repository.RepositoryException
  {
    return this.id;
  }

  public void updateDisplayName(String displayName)
    throws org.osid.repository.RepositoryException
  {
    throw new org.osid.repository.RepositoryException(org.osid.OsidException.UNIMPLEMENTED);
  }

  public org.osid.repository.PartStructureIterator getPartStructures()
    throws org.osid.repository.RepositoryException
  {
    java.util.Vector results = new java.util.Vector();
    try
    {
      results.addElement(CreatorPartStructure.getInstance());
      results.addElement(DatePartStructure.getInstance());
      results.addElement(DateRetrievedPartStructure.getInstance());
      results.addElement(DOIPartStructure.getInstance());
      results.addElement(EditionPartStructure.getInstance());
      results.addElement(EndPagePartStructure.getInstance());
      results.addElement(InLineCitationPartStructure.getInstance());
      results.addElement(IssuePartStructure.getInstance());
      results.addElement(LanguagePartStructure.getInstance());
      results.addElement(LocIdentifierPartStructure.getInstance());
      results.addElement(NotePartStructure.getInstance());
      results.addElement(IsnIdentifierPartStructure.getInstance());
      results.addElement(OpenUrlPartStructure.getInstance());
      results.addElement(PagesPartStructure.getInstance());
      results.addElement(PublicationLocationPartStructure.getInstance());
      results.addElement(PublisherPartStructure.getInstance());
      results.addElement(RightsPartStructure.getInstance());
      results.addElement(SourceTitlePartStructure.getInstance());
      results.addElement(StartPagePartStructure.getInstance());
      results.addElement(SubjectPartStructure.getInstance());
      results.addElement(TypePartStructure.getInstance());
      results.addElement(URLPartStructure.getInstance());
      results.addElement(URLLabelPartStructure.getInstance());
      results.addElement(URLFormatPartStructure.getInstance());
      results.addElement(VolumePartStructure.getInstance());
      results.addElement(YearPartStructure.getInstance());
    }
    catch (Throwable t)
    {
      throw new org.osid.repository.RepositoryException(org.osid.OsidException.OPERATION_FAILED);
    }
    return new PartStructureIterator(results);
  }

  public boolean validateRecord(org.osid.repository.Record record)
    throws org.osid.repository.RepositoryException
  {
    return true;
  }
}
