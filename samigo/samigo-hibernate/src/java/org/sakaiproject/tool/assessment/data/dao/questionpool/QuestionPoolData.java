/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.data.dao.questionpool;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolItemIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class QuestionPoolData
    implements Serializable, QuestionPoolDataIfc, Cloneable
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = 9180085666292824370L;
  public static final Long ACCESS_DENIED =   Long.valueOf(30);
  public static final Long READ_ONLY =   Long.valueOf(31);
  public static final Long READ_COPY =   Long.valueOf(32);
  public static final Long READ_WRITE = Long.valueOf(33);
  public static final Long ADMIN = Long.valueOf(34);
  public static final Long DEFAULT_TYPEID = Long.valueOf(0);
  public static final Long DEFAULT_INTELLECTUAL_PROPERTYID = Long.valueOf(0);
  public static final Long ROOT_POOL = Long.valueOf(0);

  private Long questionPoolId;
  private String title;
  private String description;
  private Long parentPoolId = ROOT_POOL;

  private String ownerId = null;
  private AgentDataIfc owner;
  private Date dateCreated;
  private Date lastModified;
  private String lastModifiedById;
  private AgentDataIfc lastModifiedBy;
  private Long accessTypeId = null;
  private TypeIfc accessType;
  private String objectives;
  private String keywords;
  private String rubric;
  private Long typeId;
  private TypeIfc type;
  private Long intellectualPropertyId;
  private String organizationName;
  private Set questionPoolItems;
  private Collection items = new ArrayList();

  private Long subPoolSize;

  public QuestionPoolData(){
  }
  /**
   * This is a cheap object created for holding just the Id & title. This is
   * used by the pulldown list in authoring when we only need the Id & title
   * and nothing else. This object is not used for persistence.
   * @param poolId
   * @param title
   */
  public QuestionPoolData(Long poolId, String title){
    this.questionPoolId= poolId;
    this.title = title;
  }

    public QuestionPoolData(Long poolId, String title, Long parentPoolId){
    this.questionPoolId= poolId;
    this.parentPoolId=parentPoolId;
    this.title = title;
  }


  public Long getQuestionPoolId()
  {
    return questionPoolId;
  }

  public void setQuestionPoolId(Long questionPoolId)
  {
    this.questionPoolId = questionPoolId;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  // added to faciliate Hibernate POJO requirement, parentPool is not part of the orginal
  // QuestionPoolProperties. Will see if I can make it part of it later  - daisyf on 8/25/04
  public Long getParentPoolId()
  {
    return parentPoolId;
  }

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public void setParentPoolId(Long parentPoolId)
  {
    this.parentPoolId = parentPoolId;
  }

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public String getOwnerId()
  {
    return ownerId;
  }

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public void setOwnerId(String ownerId)
  {
    this.ownerId = ownerId;
  }

  public AgentDataIfc getOwner()
  {
    return owner;
  }

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public void setOwner(AgentDataIfc owner)
  {
    this.owner = owner;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getDateCreated()
  {
    return dateCreated;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param dateCreated DOCUMENTATION PENDING
   */
  public void setDateCreated(Date dateCreated)
  {
    this.dateCreated = dateCreated;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getLastModified()
  {
    return lastModified;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param lastModified DOCUMENTATION PENDING
   */
  public void setLastModified(Date lastModified)
  {
    this.lastModified = lastModified;
  }

  public String getLastModifiedById()
  {
    return lastModifiedById;
  }

  public void setLastModifiedById(String lastModifiedById)
  {
    this.lastModifiedById = lastModifiedById;
  }

  public AgentDataIfc getLastModifiedBy()
  {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(AgentDataIfc lastModifiedBy)
  {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public Long getAccessTypeId()
  {
    return this.accessTypeId;
  }

  // added to faciliate Hibernate POJO requirement, also for getting the Type without
  // making a trip to the DB - daisyf on 8/25/04
  public void setAccessTypeId(Long accessTypeId)
  {
    this.accessTypeId = accessTypeId;
  }

  public TypeIfc getAccessType()
  {
    return this.accessType;
  }

  public void setAccessType(TypeIfc accessType)
  {
    this.accessType = accessType;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getObjectives()
  {
    return objectives;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param objectives DOCUMENTATION PENDING
   */
  public void setObjectives(String objectives)
  {
    this.objectives = objectives;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getKeywords()
  {
    return keywords;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newKeywords DOCUMENTATION PENDING
   */
  public void setKeywords(String keywords)
  {
    this.keywords = keywords;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getRubric()
  {
    return rubric;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param rubric DOCUMENTATION PENDING
   */
  public void setRubric(String rubric)
  {
    this.rubric = rubric;
  }

  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public Long getTypeId()
  {
    return typeId;
  }

  // added to faciliate Hibernate POJO requirement, also for getting the Type
  // without making a trip to the DB - daisyf on 8/25/04
  public void setTypeId(Long typeId)
  {
    this.typeId = typeId;
  }

  public TypeIfc getType()
  {
    return this.type;
  }

  public void setType(TypeIfc type)
  {
    this.type = type;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Long getIntellectualPropertyId()
  {
    return intellectualPropertyId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param intellectualProperty DOCUMENTATION PENDING
   */
  public void setIntellectualPropertyId(Long intellectualPropertyId)
  {
    this.intellectualPropertyId = intellectualPropertyId;
  }

  public void setIntellectualProperty(String intellectualProperty)
  {
      setIntellectualPropertyId(new Long(intellectualProperty));
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getOrganizationName()
  {
    return organizationName;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param organizationName DOCUMENTATION PENDING
   */
  public void setOrganizationName(String organizationName)
  {
    this.organizationName = organizationName;
  }


  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public Set getQuestionPoolItems()
  {
    return questionPoolItems;
  }


  // added to faciliate Hibernate POJO requirement - daisyf on 8/25/04
  public void setQuestionPoolItems(Set questionPoolItems)
  {
    this.questionPoolItems = questionPoolItems;
  }

  public void addQuestionPoolItem(QuestionPoolItemIfc questionPoolItem){
    questionPoolItems.add(questionPoolItem);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return an ArrayList of org.sakaiproject.tool.assessment.data.dao.ItemData
   */
  public Collection getQuestions()
  {
    return items;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newItems DOCUMENTATION PENDING
   */
  public void setQuestions(Collection items)
  {
      this.items = items;
  }

  // for JSF, sigh - daisyf
  public Integer getQuestionSize()
  {
    return   Integer.valueOf(items.size());
  }

  // for JSF, sigh - daisyf
  public void setSubPoolSize(Long subPoolSize)
  {
    this.subPoolSize = subPoolSize;
  }

  // for JSF, sigh - daisyf
  public Long getSubPoolSize()
  {
    return subPoolSize;
  }

  public Object clone(){
    QuestionPoolData newQ = new QuestionPoolData();
    newQ.setQuestionPoolId(questionPoolId);
    newQ.setTitle(title);
    newQ.setDescription(description);
    newQ.setParentPoolId(parentPoolId);
    newQ.setOwnerId(ownerId);
    newQ.setOwner(owner);
    newQ.setDateCreated(dateCreated);
    newQ.setLastModified(lastModified);
    newQ.setLastModifiedById(lastModifiedById);
    newQ.setLastModifiedBy(lastModifiedBy);
    newQ.setAccessTypeId(accessTypeId);
    newQ.setAccessType(accessType);
    newQ.setObjectives(objectives);
    newQ.setKeywords(keywords);
    newQ.setRubric(rubric);
    newQ.setTypeId(typeId);
    newQ.setType(type);
    newQ.setIntellectualPropertyId(intellectualPropertyId);
    newQ.setOrganizationName(organizationName);
    
    return newQ;
  }
  
  public Integer getQuestionPoolItemSize()
  {
	  return  Integer.valueOf(questionPoolItems.size());
  }
}
