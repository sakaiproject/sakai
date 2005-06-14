/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

package org.sakaiproject.tool.assessment.business.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import osid.shared.Id;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id: AAMTree.java,v 1.2 2005/05/31 19:14:31 janderse.umich.edu Exp $
 */
public interface AAMTree
  extends Serializable
{
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Id getCurrentId();

  /**
   * DOCUMENTATION PENDING
   *
   * @param id DOCUMENTATION PENDING
   */
  public void setCurrentId(Id id);

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public boolean currentObjectIsParent();

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Object getCurrentObject();

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
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
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Map getAllObjects();

  /**
   * A collection of objects in proper sorted order for a tree.
   */
  public Collection getSortedObjects();
  /**
   * A collection of objects in proper sorted order for a subpool tree.
   */
  public Collection getSortedObjects(Id parentId);

  /**
   * DOCUMENTATION PENDING
   *
   * @param parentID DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Map getChildren(Id parentID);

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Map getChildren();

  /**
   * DOCUMENTATION PENDING
   *
   * @param parentID DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public List getChildList(Id parentID);

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public List getChildList();

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
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
   * THis checks to see if given two pools have a common ancestor
   */
  public boolean haveCommonRoot(Id poolIdA,Id poolIdB);
 
 /**
   * Is a pool a descendant of the other?
   */
  public boolean isDescendantOf(Id poolA,Id poolB);


 /**
   * This returns the level of the pool inside a pool tree, Root being 0. 
   */
  public int poolLevel(Id poolId);


}
