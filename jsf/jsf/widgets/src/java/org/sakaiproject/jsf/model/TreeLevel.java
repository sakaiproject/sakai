/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
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

package org.sakaiproject.jsf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

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
    System.out.println("testing flat");
    ArrayList treeList = new ArrayList();
    for (int i = 0; i < 10; i++)
    {
      treeList.add(new TreeLevel(i));
    }
    for (Iterator iter = treeList.iterator(); iter.hasNext(); ) {
      Object item = (Object)iter.next();
      System.out.println("LEVEL: " + item);
    }
      System.out.println("testing hierarchy");
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
        System.out.println("LEVEL: " + item);
      }
    }

}
