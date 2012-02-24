/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.api.app.help.HelpManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.util.ResourceLoader;

/**
 * search tool
 * @version $Id$
 */
public class SearchTool
{

  private ResourceLoader msgs = new ResourceLoader("org.sakaiproject.tool.help.bundle.Messages");
   
  private HelpManager helpManager;
  private List searchResults;
  private String searchString = msgs.getString("search_term");
  private String numberOfResult = "";
  private String showLinkToQuestionTool;
  private String emailAddress;

  /**
   * get email address
   * @return Returns the emailAddress.
   */
  public String getEmailAddress()
  {
    return emailAddress;
  }

  /**
   * get search string
   * @return Returns the searchString.
   */
  public String getSearchString()
  {
    return searchString;
  }

  /**
   * set search string
   * @param searchString The searchString to set.
   */
  public void setSearchString(String searchString)
  {
    this.searchString = searchString;
  }

  /**
   * get search results
   * @return Returns the searchResults.
   */
  public List getSearchResults()
  {
    return searchResults;
  }

  /**
   * set search results
   * @param searchResults The searchResults to set.
   */
  public void setSearchResults(List searchResults)
  {
    this.searchResults = searchResults;
  }

  /**
   * process action search
   * @return view
   */
  public String processActionSearch()
  {
    //if (searchString != null && searchString.equals(getHelpManager().getRestConfiguration().getRestCredentials())){
    //  getHelpManager().reInitialize();
    //  return "main";
    //}

    EventTrackingService.post(EventTrackingService.newEvent("help.search", this.searchString, false));
    
    searchResults = new ArrayList();

    Set resultSet = getHelpManager().searchResources(this.searchString);
    if (resultSet != null)
    {
       TreeSet treeSet = new TreeSet(resultSet);
       searchResults.addAll(treeSet);
    }

    String searchStr = this.searchString;
    this.setNumberOfResult(searchResults.size());
    return "main";
  }

  /**
   * submit cancel
   * @return view
   */
  public String submitCancel()
  {
    this.searchString = "";
    return "main";
  }

  /**
   * return help manager
   * @return Returns the helpManager.
   */
  public HelpManager getHelpManager()
  {
    return helpManager;
  }

  /**
   * set help manager
   * @param helpManager The helpManager to set.
   */
  public void setHelpManager(HelpManager helpManager)
  {
    this.helpManager = helpManager;
  }

  /**
   * get number of results
   * @return Returns the numberOfResult.
   */
  public String getNumberOfResult()
  {
    return numberOfResult;
  }

  /**
   * set number of results
   * @param numberOfResult The numberOfResult to set.
   */
  public void setNumberOfResult(int numberOfResultInt)
  {
    if (numberOfResultInt == 0)
    {
      this.numberOfResult = msgs.getString("no_results");
    }
    else
    {
       this.numberOfResult = numberOfResultInt + " " + msgs.getString("results_found");
    }
  }

  /**
   * get show link question tool
   * @return Returns the showLinkToQuestionTool.
   */
  public String getShowLinkToQuestionTool()
  {
    emailAddress = helpManager.getSupportEmailAddress();
    if (!"".equals(emailAddress) && emailAddress != null)
    {
      showLinkToQuestionTool = "true";
    }
    else
    {
      showLinkToQuestionTool = "false";
    }
    return showLinkToQuestionTool;
  }
  
  /**
   * get value of REST configuration
   * @return true if REST is enabled, false otherwise
   */
  public boolean getIsRestEnabled(){
    if ("sakai".equals(getHelpManager().getRestConfiguration().getOrganization())){
      return false;
    }
    else{
      return true;
    }
  }
}
