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

package org.sakaiproject.jsf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreeLevel
{
  private TreeLevel parent = null;
  private ArrayList childList;
  private int index;
  boolean child;


  /**
   * "Parent" constructor.  This is a top level and is its own parent and is a
   * child of none.
   */
  public TreeLevel(int index)
  {
    this.childList = new ArrayList();
    this.parent = this;
    this.index = index;
    this.child = false;
  }

  /**
   * No-arg constructor.  "Parent" constructor equivalent, with index set to 0.
   */
  public TreeLevel()
   {
     this.childList = new ArrayList();
     this.parent = this;
     this.index = 0;
     this.child = false;
   }

   /**
    * "Child" constructor.  Create a child of "parent".
    * @param parent TreeLevel
    */
   public TreeLevel(TreeLevel parent)
  {
    this.childList = new ArrayList();
    this.parent = parent;
    this.index = parent.addChild(this);
    this.child = true;
  }

  /**
   * Get parent, or self if no parent.
   * @return TreeLevel
   */
  public TreeLevel getParent()
  {
    return parent;
  }

  /**
   * Get list of all child TreeLevels
   * @return List
   */
  public List getChildren()
  {
    return childList;
  }

  /**
   * True if this has a parent.
   * @return boolean
   */
  public boolean isChild()
  {
    return child;
  }

  /**
   * String representing 0-indexed parent-child relationships.
   * Example: 18_9_4 is the fourth child of the nineth child of 18
   * @return String
   */
  public String toString()
  {
    // recurse for children, done if parent
    if (this.isChild())
    {
      return this.getParent().toString() + "_" + index;
    }
    else
    {
      return "" + index;
    }
  }

  /**
   * Utility reciprocal method to add a child when it chooses this as its parent.
   * @param child TreeLevel
   * @return int
   */
  protected int addChild(TreeLevel child)
  {
    childList.add(child);
    return childList.size();
  }

  /**
   * test code, demonstrates usage.
   * @param args String[]
   */
  public static void main(String[] args) {
    log.debug("testing flat");
    ArrayList treeList = new ArrayList();
    for (int i = 0; i < 10; i++)
    {
      treeList.add(new TreeLevel(i));
    }
    for (Iterator iter = treeList.iterator(); iter.hasNext(); ) {
      Object item = (Object)iter.next();
      log.debug("LEVEL: {}", item);
    }
      log.debug("testing hierarchy");
      treeList = new ArrayList();
      TreeLevel t0 = new TreeLevel(0);
      treeList.add(t0);

      for (int i = 0; i < 10; i++)
      {
        TreeLevel t1 = new TreeLevel(t0);
        treeList.add(t1);
        for (int j = 0; j < 5; j++) {
          TreeLevel t2 = new TreeLevel(t1);
          treeList.add(t2);
        }
      }

      for (Iterator iter = treeList.iterator(); iter.hasNext(); )
      {
        Object item = (Object) iter.next();
        log.debug("LEVEL: {}", item);
      }
    }

}
