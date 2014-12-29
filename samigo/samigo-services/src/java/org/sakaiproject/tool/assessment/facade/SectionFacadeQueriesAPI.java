/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface SectionFacadeQueriesAPI
{

  public IdImpl getId(String id);

  public IdImpl getId(Long id);

  public IdImpl getId(long id);

  //public Long addSection(Long assessmentId);

  //public void remove(Long sectionId);

  public SectionFacade get(Long sectionId);

  public SectionData load(Long sectionId);

  public void addSectionMetaData(Long sectionId, String label, String value);

  public void deleteSectionMetaData(Long sectionId, String label);

}
