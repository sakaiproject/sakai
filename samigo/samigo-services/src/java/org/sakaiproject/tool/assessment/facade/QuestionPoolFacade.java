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

package org.sakaiproject.tool.assessment.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.osid.shared.Id;
import org.osid.shared.SharedException;

import org.sakaiproject.tool.assessment.business.questionpool.QuestionPool;
import org.sakaiproject.tool.assessment.business.questionpool.QuestionPoolException;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolData;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.questionpool.QuestionPoolItemIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.AgentDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.questionpool.impl.QuestionPoolImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;

/**
 * @author Ed Smiley <esmiley@stanford.edu>
 */
@Slf4j
public class QuestionPoolFacade
    implements java.io.Serializable, QuestionPoolDataIfc, Cloneable
{
  private static final long serialVersionUID = 7526471155622776147L;

  public static final Long ACCESS_DENIED =  Long.valueOf(30);
  public static final Long READ_ONLY = Long.valueOf(31);
  public static final Long READ_COPY = Long.valueOf(32);
  public static final Long READ_WRITE = Long.valueOf(33);
  public static final Long ADMIN = Long.valueOf(34);
  public static final Long DEFAULT_TYPEID = Long.valueOf(0);
  public static final Long DEFAULT_INTELLECTUAL_PROPERTYID = Long.valueOf(0);
  public static final Long ROOT_POOL = Long.valueOf(0);

  private QuestionPool questionPool;
  // We have 2 sets of properties:
  // #1) properties according to
  // org.sakaiproject.tool.assessment.business.entity.questionpool.QuestionPool.
  private String displayName;
  private String description;
  private QuestionPoolDataIfc data;
  private Id id;
  private TypeIfc questionPoolType;
  private Id parentId;
  // remove parentPool and the code to set it. It isn't actually
  // used, and building it involves an unnecessary database
  // transaction.
  //  private QuestionPoolFacade parentPool;
  // #2) set of properties of QuestionPoolDataIfc
  private Long questionPoolId;
  private Long parentPoolId;
  private String ownerId;
  private AgentDataIfc owner;
  private String title; // same as displayName
  private Date dateCreated;
  private Date lastModified;
  private String lastModifiedById;
  private AgentDataIfc lastModifiedBy;
  private Long accessTypeId;
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

  /**
   * Creates a new QuestionPoolFacade object.
   */
  public QuestionPoolFacade(){
    // need to hook QuestionPoolFacade.data to QuestionPoolData,
    // our POJO for Hibernate persistence
    this.data = new QuestionPoolData();
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); //<-- place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
 }

  /**
   * Constructor.
   * Each question pool has a unique Id object and owns the Id of
   * its parent. See getId(), getParentId()
   *
   * @param newId the id
   * @param newParentId the id of its parent
   */
  public QuestionPoolFacade(Id id, Id parentId)
  {
    this.id = id;
    this.parentId = parentId;
    this.data = new QuestionPoolData();
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); //<-- place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
      setQuestionPoolId(new Long(id.getIdString()));
      setParentPoolId(new Long(parentId.getIdString()));
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    catch (SharedException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    catch (NumberFormatException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   * Constructor.
   * Each question pool has a unique Id object and owns the Id of
   * its parent. See getId(), getParentId()
   *
   * @param newId the id
   * @param newParentId the id of its parent
   */
  public QuestionPoolFacade(Long id, Long parentId)
  {
    QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries =
      PersistenceService.getInstance().getQuestionPoolFacadeQueries();
    this.id = questionPoolFacadeQueries.getQuestionPoolId(id);
    this.parentId = questionPoolFacadeQueries.getQuestionPoolId(parentId);
    this.data = new QuestionPoolData();
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); //<-- place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    setQuestionPoolId(id);
    setParentPoolId(parentId);
  }

  public QuestionPoolFacade(QuestionPoolDataIfc data){
    this.data = data;
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); // place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.id = getId();
    this.displayName = getTitle();
    this.description = getDescription();
    this.questionPoolType = getType();
    this.ownerId = getOwnerId();
    this.owner = getOwner();
    this.dateCreated = getDateCreated();
    this.lastModified = getLastModified();
    this.lastModifiedBy = getLastModifiedBy();
    this.lastModifiedById = getLastModifiedById();
    this.accessTypeId = getAccessTypeId();
    this.accessType = getAccessType();
    this.objectives = getObjectives();
    this.keywords = getKeywords();
    this.rubric = getRubric();
    this.typeId = getTypeId();
    this.type = getType();
    this.intellectualPropertyId = getIntellectualPropertyId();
    this.organizationName = getOrganizationName();
    this.questionPoolItems = getQuestionPoolItems();
    this.items = getQuestions();
    try {
    	// parentPool isn't actually need
    	// this.parentPool = findParentPool();
    	// if (this.parentPool != null)  // => ROOT POOL
        this.parentId = getParentId();
    }
    catch (Exception ex1) {
      throw new DataFacadeException(ex1.getMessage());
    }
  }

  /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding questionPoolId, Title
   * for displaying purpose (used by the pulldown list in authoring).
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   */
    public QuestionPoolFacade(Long id, String title) {
    this.questionPoolId = id;
    this.title= title;
    this.data = new QuestionPoolData();
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); //<-- place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
      setQuestionPoolId(id);
      setTitle(title);
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }

  }

 /**
   * IMPORTANT: this constructor do not have "data", this constructor is
   * merely used for holding questionPoolId, Title, parentId
   * for validation question pool
   * This constructor does not persist data (which it has none) to DB
   * @param id
   * @param title
   * @param parentid
   */
    public QuestionPoolFacade(Long id, String title, Long parentId) {
    this.questionPoolId = id;
    this.title= title;
    this.parentPoolId=parentId;
    this.data = new QuestionPoolData();
    QuestionPoolImpl questionPoolImpl = new QuestionPoolImpl(); //<-- place holder
    questionPool = (QuestionPool)questionPoolImpl;
    try {
      questionPool.updateData(this.data);
      setParentPoolId(parentId);
      setQuestionPoolId(id);
      setTitle(title);
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }

  }




  /**
   * Get the Id for this QuestionPoolFacade.
   * @return org.osid.shared.Id
   */
  org.osid.shared.Id getId(){
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries =
      PersistenceService.getInstance().getQuestionPoolFacadeQueries();
    return questionPoolFacadeQueries.getQuestionPoolId(this.data.getQuestionPoolId());
  }

  /**
   *
   * @return the display name for the question pool
   * @throws DataFacadeException
   */
  public String getDisplayName()
    throws DataFacadeException
  {
    return getTitle();
  }

  /**
   *
   * @param pdisplayName the display name for the question pool
   * @throws DataFacadeException
   */
  public void updateDisplayName(String displayName)
    throws DataFacadeException
  {
    setDisplayName(displayName);
  }

  private void setDisplayName(String displayName)
    throws DataFacadeException
  {
    this.displayName = displayName;
    this.data.setTitle(displayName);
  }

  public String getDescription()
  {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getDescription();
  }

  /**
   *
   * @param pdescription the description for the question pool
   * @throws DataFacadeException
   */
  public void updateDescription(String description)
    throws DataFacadeException
  {
    setDescription(description);
  }

  public void setDescription(String description)
  {
    this.description = description;
    this.data.setDescription(description);
  }

  /**
   * Get the data for this QuestionPoolFacade.
   * @return QuestionPoolDataIfc
   */
  public QuestionPoolDataIfc getData(){
    return this.data;
  }

  /**
   * Call setDate() to update data in ItemFacade
   * @param data
   */
  public void updateData(QuestionPoolDataIfc data) {
      setData(data);
  }

  /**
   * Set data for ItemFacade
   * @param data
   */
  public void setData(QuestionPoolDataIfc data) {
      this.data = data;
  }

  /**
   *
   * @return the type of pool for the question pool
   * @throws DataFacadeException
   */
  public TypeIfc getQuestionPoolType()
    throws DataFacadeException
  {
    return getType();
  }

  public void updateQuestionPoolType(TypeIfc questionPoolType)
    throws DataFacadeException
  {
    setQuestionPoolType(questionPoolType);
  }
  private void setQuestionPoolType(TypeIfc questionPoolType)
    throws DataFacadeException
  {
    this.questionPoolType = questionPoolType;
    setType(questionPoolType);
  }

  /**
   *
   * @return the id object for the question pool
   * @throws QuestionPoolException
   */
  public Id getParentId()
    throws DataFacadeException
  {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
      if (this.data != null){
        QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries =
          PersistenceService.getInstance().getQuestionPoolFacadeQueries();
        return questionPoolFacadeQueries.getQuestionPoolId(this.data.getParentPoolId());
      }
      else  // implies ROOT_POOL
        return null;
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }

  /**
   *
   * Sets the parent id object for the question pool
   * @throws DataFacadeException
   */
  public void setParentId(Id parentId)
    throws DataFacadeException
  {
    this.parentId = parentId;
    try {
      setParentPoolId(new Long(parentId.getIdString()));
    }
    catch (SharedException ex) {
      log.warn(ex.getMessage());
    }
    catch (NumberFormatException ex) {
      log.warn(ex.getMessage());
    }
  }

  // no longer needed
  /*
  public QuestionPoolFacade getParentPool()
    throws DataFacadeException
  {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
      if (this.data != null){
        QuestionPoolFacadeQueriesAPI questionPoolFacadeQueries = PersistenceService.getInstance().getQuestionPoolFacadeQueries();
        return questionPoolFacadeQueries.getPoolById(this.data.getParentPoolId());
      }
      else // implies ROOT_ROOL
        return null;
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
  }
  */

  // the following methods implements
  // org.sakaiproject.tool.assessment.ifc.questionpool.QuestionPoolDataIfc
  public Long getQuestionPoolId() throws DataFacadeException {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getQuestionPoolId();
  }

  /**
   * Set itemId for ItemFacade
   * @param itemId
   */
  public void setQuestionPoolId(Long questionPoolId) {
    this.questionPoolId = questionPoolId;
    this.data.setQuestionPoolId(questionPoolId);
  }


  public String getTitle() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTitle();
  }

  public void setTitle(String title) {
    this.title = title;
    this.data.setTitle(title);
  }

  public Long getParentPoolId() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    if (this.data != null )
      return this.data.getParentPoolId();
    else  // implies ROOT_POOL
      return null;
  }

  public void setParentPoolId(Long parentPoolId) {
    this.parentPoolId = parentPoolId;
    this.data.setParentPoolId(parentPoolId);
  }

  public String getOwnerId() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getOwnerId();
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    this.data.setOwnerId(ownerId);
  }

  public AgentDataIfc getOwner() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getOwner();
  }

  public void setOwner(AgentDataIfc owner) {
    this.owner = owner;
    this.data.setOwner(owner);
  }

  public Date getDateCreated() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getDateCreated();
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
    this.data.setDateCreated(dateCreated);
  }

  public Date getLastModified() {
    //this.lastModified = lastModified;
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModified();
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
    this.data.setLastModified(lastModified);
  }

  public String getLastModifiedById() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedById();
  }

  public void setLastModifiedById(String lastModifiedById) {
    this.lastModifiedById = lastModifiedById;
    this.data.setLastModifiedById(lastModifiedById);
  }

  public AgentDataIfc getLastModifiedBy() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedBy();
  }

  public void setLastModifiedBy(AgentDataIfc lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
    this.data.setLastModifiedBy(lastModifiedBy);
  }

  public Long getAccessTypeId() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAccessTypeId();
  }

  public void setAccessTypeId(Long accessTypeId) {
    this.accessTypeId = accessTypeId;
    this.data.setAccessTypeId(accessTypeId);
  }

  public TypeIfc getAccessType() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getAccessType();
  }

  public void setAccessType(TypeIfc accessType) {
    this.accessType = accessType;
    this.data.setAccessType(accessType);
  }

  public String getObjectives() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getObjectives();
  }

  public void setObjectives(String objectives) {
    this.objectives = objectives;
    this.data.setObjectives(objectives);
  }

  public String getKeywords() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getKeywords();
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
    this.data.setKeywords(keywords);
  }

  public String getRubric() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getRubric();
  }

  public void setRubric(String rubric) {
    this.rubric = rubric;
    this.data.setRubric(rubric);
  }

  public Long getTypeId() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTypeId();
  }

  public void setTypeId(Long typeId) {
    this.typeId = typeId;
    this.data.setTypeId(typeId);
  }

  public TypeIfc getType() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getType();
  }

  public void setType(TypeIfc type) {
    this.type = type;
    this.data.setType(type);
  }

  public Long getIntellectualPropertyId() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getIntellectualPropertyId();
  }

  public void setIntellectualPropertyId(Long intellectualPropertyId) {
    this.intellectualPropertyId = intellectualPropertyId;
    this.data.setIntellectualPropertyId(intellectualPropertyId);
  }

  public String getOrganizationName() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getOrganizationName();
  }

  public void setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
    this.data.setOrganizationName(organizationName);
  }

  /**
   * This is a list of association between an item (question) and a pool.
   * This does not represent a list of items. use getQuestions()
   * to get a list of items
   */
  public Set getQuestionPoolItems() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getQuestionPoolItems();
  }

  /*
  public void setQuestionPoolItems(Set questionPoolItems) {
    this.questionPoolItems = questionPoolItems;
    this.data.setQuestions(questionPoolItems);
    this.data.setQuestionPoolItems(questionPoolItems);
  }
  */
  
  public void setQuestionPoolItems(Set questionPoolItems) {
	this.questionPoolItems = questionPoolItems;
	this.data.setQuestionPoolItems(questionPoolItems);
  }

  public void addQuestionPoolItem(QuestionPoolItemIfc queestionPoolItem){
    Set questionPoolItemSet = getQuestionPoolItems();
    questionPoolItemSet.add(queestionPoolItem);
    setQuestionPoolItems(questionPoolItemSet);
  }

  public Collection getQuestions() {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getQuestions();
  }

  public void setQuestions(Collection items) {
    this.items = items;
    this.data.setQuestions(items);
  }

  public Integer getQuestionSize()
  {
    return  Integer.valueOf(items.size());
  }

  public void setSubPoolSize(Long subPoolSize)
  {
    this.subPoolSize = subPoolSize;
    this.data.setSubPoolSize(subPoolSize);
  }

  public Long getSubPoolSize()
  {
    try {
      this.data = (QuestionPoolDataIfc) questionPool.getData();
    }
    catch (QuestionPoolException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getSubPoolSize();
  }

  /* this was not used. 
  private ItemIteratorFacade getItemIterator() {
    return new ItemIteratorFacade(items);
  }
  */

  public Object clone(){
    QuestionPoolFacade newPool = new QuestionPoolFacade((QuestionPoolData)data.clone());
    return newPool;
  }

  
  public String getOwnerDisplayName() {
    String ownerIdString = this.getOwnerId();
    String ownerDisplayName= AgentFacade.getDisplayName(ownerIdString);
    return ownerDisplayName;
  }

}
