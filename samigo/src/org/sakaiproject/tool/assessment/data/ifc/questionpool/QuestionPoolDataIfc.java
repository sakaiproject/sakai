/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.data.ifc.questionpool;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.osid.agent.Agent;
import org.sakaiproject.tool.assessment.data.dao.questionpool.QuestionPoolItemData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public interface QuestionPoolDataIfc
      extends java.io.Serializable{

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

  Agent getOwner();

  void setOwner(Agent owner);

  Date getDateCreated();

  void setDateCreated(Date dateCreated);

  Date getLastModified();

  void setLastModified(Date lastModified);

  String getLastModifiedById();

  void setLastModifiedById(String lastModifiedById);

  Agent getLastModifiedBy();

  void setLastModifiedBy(Agent lastModifiedBy);

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

  void addQuestionPoolItem(QuestionPoolItemData questionPoolItem);

  Collection getQuestions();

  void setQuestions(Collection items);

  // for JSF, sigh - daisyf
  Integer getQuestionSize();

  // for JSF, sigh - daisyf
  void setSubPoolSize(Integer subPoolSize);

  // for JSF, sigh - daisyf
  Integer getSubPoolSize();

}
