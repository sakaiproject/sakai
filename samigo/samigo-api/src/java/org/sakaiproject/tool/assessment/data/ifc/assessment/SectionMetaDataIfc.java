/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.ifc.assessment;

import lombok.Setter;
import lombok.Getter;

public abstract class SectionMetaDataIfc {

  public static final String KEYWORDS = "SECTION_KEYWORDS";
  public static final String OBJECTIVES = "SECTION_OBJECTIVES";
  public static final String RUBRICS = "SECTION_RUBRICS";
  public static final String ATTACHMENTS = "ATTACHMENTS";

  @Setter @Getter protected Long id;
  @Setter @Getter protected SectionDataIfc section;
  @Setter @Getter protected String label;
  @Setter @Getter protected String entry;

}
