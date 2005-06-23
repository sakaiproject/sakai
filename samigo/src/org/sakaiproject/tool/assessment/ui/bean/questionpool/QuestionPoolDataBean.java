/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.bean.questionpool;

//import org.sakaiproject.tool.assessment.business.entity.questionpool.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;

/**
 * This holds question pool information.
 *
 * Used to be org.navigoproject.ui.web.form.questionpool.QuestionPoolBean
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */
public class QuestionPoolDataBean
  implements Serializable
{

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


  private static Log log = LogFactory.getLog(QuestionPoolDataBean.class);

  private Map parentPools = new HashMap();
  private ArrayList parentPoolsArray = new ArrayList();




  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Date getLastModified() {
	return lastModified;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setLastModified(Date param)
  {
    lastModified = param;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setDisplayName(String newName)
  {
    displayName = newName;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getOwner()
  {
    return owner;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newName DOCUMENTATION PENDING
   */
  public void setOwner(String param)
  {
    owner= param;
  }





  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newDesc DOCUMENTATION PENDING
   */
  public void setDescription(String newDesc)
  {
    description = newDesc;
  }
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getOrganizationName()
  {
    return organizationName ;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newDesc DOCUMENTATION PENDING
   */
  public void setOrganizationName(String param)
  {
    organizationName= param;
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
   * @param newDesc DOCUMENTATION PENDING
   */
  public void setKeywords(String param)
  {
    keywords= param;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getObjectives()
  {
    return objectives ;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newDesc DOCUMENTATION PENDING
   */
  public void setObjectives(String param)
  {
    objectives = param;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Long getParentPoolId()
  {
    return parentPoolId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newId DOCUMENTATION PENDING
   */
  public void setParentPoolId(Long newId)
  {
    parentPoolId = newId;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Long getId()
  {
    return id;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newId DOCUMENTATION PENDING
   */
  public void setId(Long newId)
  {
    id = newId;
  }


  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */

  public String getNumberOfQuestions()
  {
    	return numberOfQuestions;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newNumber DOCUMENTATION PENDING
   */
  public void setNumberOfQuestions(String newNumber)
  {
    numberOfQuestions= newNumber;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getNumberOfSubpools()
  {
    	return numberOfSubpools ;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param newNumber DOCUMENTATION PENDING
   */
  public void setNumberOfSubpools(String newNumber)
  {
    numberOfSubpools = newNumber;
  }


/*
  public boolean getShowQuestions()
  {
	if ( (new Integer(numberOfQuestions)).intValue() >0)
        {
                return true;
        }
	return false;

  }

  public boolean getShowSubpools()
  {
	if ( (new Integer(numberOfSubpools)).intValue() >0)
	{
		return true;
	}
       	return false;

  }
*/

  public boolean getShowParentPools()
  {
        if ( parentPools.isEmpty())
        {
                return false;
        }
        return true;

  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Map getParentPools()
  {
    return parentPools;
  }

  public void setParentPools(ArrayList newpools)
  {
	parentPools = new HashMap();
   Iterator iter = newpools.iterator();
      while(iter.hasNext())
      {
	QuestionPoolFacade qpool = (QuestionPoolFacade)  iter.next();
        parentPools.put(qpool.getDisplayName(), "jsf/questionpool/editPool.faces?qpid=" + qpool.getQuestionPoolId());
	}

  }

  public ArrayList getParentPoolsArray()
  {
    return parentPoolsArray;
  }

  public void setParentPoolsArray(ArrayList newpools)
  {
    parentPoolsArray = newpools;
  }






  /**
   * This checks to see if the question titles are unique.  If they're
   * all the same, or null, the titles aren't displayed.
   */
/*
 // not used
  public boolean getShowTitles()
  {
    String title = null;

    if(properties.getQuestions() == null)
    {
      return true;
    }

    Iterator iter = properties.getQuestions().iterator();
    try
    {
      while(iter.hasNext())
      {
        Item item = (Item) iter.next();

        // If we've found at least two different titles, show titles
        if(
          (item.getDisplayName() != null) && (title != null) &&
            ! item.getDisplayName().equals(title))
        {
          return true;
        }

        if((title == null) && (item.getDisplayName() != null))
        {
          title = item.getDisplayName();
        }
      }
    }
    catch(Exception e)
    {
      throw new Error(e);
    }

    return true;
  }
*/


}
