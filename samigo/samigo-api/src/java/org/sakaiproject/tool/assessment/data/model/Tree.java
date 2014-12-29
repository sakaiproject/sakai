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



package org.sakaiproject.tool.assessment.data.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * originally Tree.java
 * @author esmiley@stanford.edu
 * @version $Id$
 */
public interface Tree extends Serializable
{
  /**
   *
   *
   * @return
   */
  public Long getCurrentId();

  /**
   *
   *
   * @param id
   */
  public void setCurrentId(Long id);

  /**
   *
   *
   * @return
   */
  public boolean currentObjectIsParent();

  /**
   *
   *
   * @return
   */
  public Object getCurrentObject();

  /**
   *
   *
   * @return
   */
  public Object getParent();

  /**
   * This is used to get the String id suitable for use in a
   * javascript tree.
   */
  public String getCurrentObjectHTMLId();

  /**
   * Get the current level.
   *
   * @return A String that represents the level we're on (1 is root node,
   * 2 is first level child, etc..
   */
  public String getCurrentLevel();

  /**
   * This returns a collection of String properties that can be
   * displayed in a table.  If (currentObjectId == null), returns
   * the list of column headers.
   */
  public Collection getCurrentObjectProperties();

  /**
   * This takes in an array of method names used to get the properties.
   * These are used to return a collection of String properties that
   * can then be displayed in a javascript tree.<p>  This does not
   * have to be implemented -- it will be a dummy method for most
   * trees.
   *
   * i.e.
   * <pre>
   * String[] methods = new String[3];
   * methods[0] = "getName";
   * methods[1] = "getNumberOfSubpools";
   * methods[2] = "getDescription";
   * </pre><p>
   *
   * Which might produce:<br>
   * { "Biology 101", "3", "Basic Biology Questions" }<br>
   * when getCurrentObjectProperties() is called.
   */
  public void setPropertyMethods(String[] methods);

  /**
   *
   *
   * @return
   */
  public Map getAllObjects();

  /**
   * A collection of objects in proper sorted order for a tree.
   */
  public Collection getSortedObjects();
  /**
   * A collection of objects in proper sorted order for a subpool tree.
   */
  public Collection getSortedObjects(Long parentId);

  /**
   *
   *
   * @param parentID
   *
   * @return
   */
  public Map getChildren(Long parentID);

  /**
   *
   *
   * @return
   */
  public Map getChildren();

  /**
   *
   *
   * @param parentID
   *
   * @return
   */
  public List getChildList(Long parentID);

  /**
   *
   *
   * @return
   */
  public List getChildList();

  /**
   *
   *
   * @return
   */
  public List getRootNodeList();

  /**
   * This gets the property by which siblings will be sorted.
   */
  public String getSortProperty();

  /**
   * This sets the property by which siblings will be sorted.
   */
  public void setSortProperty(String sortBy);

  /**
   * This sorts the tree by the property .
   */
  public void sortByProperty(String sortProperty,boolean sortAscending);

 /**
   * THis checks to see if given two pools have a common ancestor
   */
  public boolean haveCommonRoot(Long poolIdA,Long  poolIdB);

 /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Long poolA,Long poolB);


 /**
   * This returns the level of the pool inside a pool tree, Root being 0.
   */
  public int poolLevel(Long poolId);


}
