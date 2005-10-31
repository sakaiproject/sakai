/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.api.app.help.HelpManager;

/**
 * search tool
 * @version $Id$
 */
public class SearchTool
{

  private HelpManager helpManager;
  private List searchResults;
  private String searchString = "enter search term";
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
    
    Set resultSet = getHelpManager().searchResources(this.searchString);
    TreeSet treeSet = new TreeSet(resultSet);
    searchResults = new ArrayList();
    searchResults.addAll(treeSet);
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
      this.numberOfResult = "No results were found for your search";
    }
    else
    {
      this.numberOfResult = numberOfResultInt + " results found";
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