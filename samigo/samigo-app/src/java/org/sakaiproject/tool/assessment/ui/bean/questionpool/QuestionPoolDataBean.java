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



package org.sakaiproject.tool.assessment.ui.bean.questionpool;

//import org.sakaiproject.tool.assessment.business.entity.questionpool.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.util.FormattedText;

/**
 * This holds question pool information.
 *
 * Used to be org.navigoproject.ui.web.form.questionpool.QuestionPoolBean
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class QuestionPoolDataBean implements Serializable {

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 418920360211039758L;
  private Long id;
  private Long parentPoolId;
  private String numberOfSubpools;
  private String numberOfQuestions;
  private boolean showSubpools;
  private boolean showQuestions;
  private boolean showParentPools;

  private String displayName;
  private String owner;
  private String organizationName;
  private String description;
  private String objectives;
  private String keywords;
  private Date lastModified;
  private Date dateCreated;

  private Map parentPools = new HashMap();
  private List parentPoolsArray = new ArrayList();

  public Date getLastModified() {
	return lastModified;
  }

  public void setLastModified(Date param)
  {
    lastModified = param;
  }

  public Date getDateCreated() {
	return dateCreated;
  }

  public void setDateCreated(Date param)
  {
	  dateCreated = param;
  }
  
  public String getDisplayName()
  {
    return FormattedText.convertFormattedTextToPlaintext(displayName);
  }

  public void setDisplayName(String newName)
  {
    displayName = newName;
  }

  public String getOwner()
  {
    return owner;
  }

  public void setOwner(String param)
  {
    owner= param;
  }

  public String getDescription()
  {
    return FormattedText.convertFormattedTextToPlaintext(description);
  }

  public void setDescription(String newDesc)
  {
    description = newDesc;
  }

  public String getOrganizationName()
  {
    return FormattedText.convertFormattedTextToPlaintext(organizationName);
  }

  public void setOrganizationName(String param)
  {
    organizationName= param;
  }

  public String getKeywords()
  {
    return FormattedText.convertFormattedTextToPlaintext(keywords);
  }

  public void setKeywords(String param)
  {
    keywords= param;
  }

  public String getObjectives()
  {
    return FormattedText.convertFormattedTextToPlaintext(objectives);
  }

  public void setObjectives(String param)
  {
    objectives = param;
  }

  public Long getParentPoolId()
  {
    return parentPoolId;
  }

  public void setParentPoolId(Long newId)
  {
    parentPoolId = newId;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long newId)
  {
    id = newId;
  }

  public String getNumberOfQuestions()
  {
    	return numberOfQuestions;
  }

  public void setNumberOfQuestions(String newNumber)
  {
    numberOfQuestions= newNumber;
  }

  public String getNumberOfSubpools()
  {
    	return numberOfSubpools ;
  }

  public void setNumberOfSubpools(String newNumber)
  {
    numberOfSubpools = newNumber;
  }

  public boolean getShowParentPools()
  {
        if ( parentPools.isEmpty())
        {
                return false;
        }
        return true;

  }

  public Map getParentPools()
  {
    return parentPools;
  }

  public void setParentPools(List newpools)
  {
	parentPools = new HashMap();
   Iterator iter = newpools.iterator();
      while(iter.hasNext())
      {
	QuestionPoolFacade qpool = (QuestionPoolFacade)  iter.next();
        parentPools.put(qpool.getDisplayName(), "jsf/questionpool/editPool.faces?qpid=" + qpool.getQuestionPoolId());
	}

  }

  public List getParentPoolsArray()
  {
    return parentPoolsArray;
  }

  public void setParentPoolsArray(List newpools)
  {
    parentPoolsArray = newpools;
  }
}
