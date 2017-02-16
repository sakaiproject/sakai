/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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




package org.sakaiproject.tool.assessment.data.ifc.questionpool;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * @version $Id$
 */
public interface QuestionPoolDataIfc
    extends java.io.Serializable, Cloneable{

  Long getQuestionPoolId();

  void setQuestionPoolId(Long questionPoolId);

  String getTitle();

  void setTitle(String title);

  String getDescription();

  void setDescription(String description);

  Long getParentPoolId();

  void setParentPoolId(Long parentPoolId);

  String getOwnerId();

  void setOwnerId(String ownerId);

  AgentDataIfc getOwner();

  void setOwner(AgentDataIfc owner);

  Date getDateCreated();

  void setDateCreated(Date dateCreated);

  Date getLastModified();

  void setLastModified(Date lastModified);

  String getLastModifiedById();

  void setLastModifiedById(String lastModifiedById);

  AgentDataIfc getLastModifiedBy();

  void setLastModifiedBy(AgentDataIfc lastModifiedBy);

  Long getAccessTypeId();

  void setAccessTypeId(Long accessTypeId);

  TypeIfc getAccessType();

  void setAccessType(TypeIfc accessType);

  String getObjectives();

  void setObjectives(String objectives);

  String getKeywords();

  void setKeywords(String keywords);

  String getRubric();

  void setRubric(String rubric);

  Long getTypeId();

  void setTypeId(Long typeId);

  TypeIfc getType();

  void setType(TypeIfc type);

  Long getIntellectualPropertyId();

  void setIntellectualPropertyId(Long intellectualPropertyId);

  String getOrganizationName();

  void setOrganizationName(String organizationName);

  Set getQuestionPoolItems();

  void setQuestionPoolItems(Set questionPoolItems);

  void addQuestionPoolItem(QuestionPoolItemIfc questionPoolItem);

  Collection getQuestions();

  void setQuestions(Collection items);

  // for JSF, sigh - daisyf
  Integer getQuestionSize();

  // for JSF, sigh - daisyf
  void setSubPoolSize(Long subPoolSize);

  // for JSF, sigh - daisyf
  Long getSubPoolSize();

  Object clone();
}
