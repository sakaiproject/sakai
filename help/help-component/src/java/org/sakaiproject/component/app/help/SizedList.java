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

package org.sakaiproject.component.app.help;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * size list
 * @version $Id$
 */
public class SizedList extends ArrayList
{
  private int size = -1;

  /**
   * constructor
   */
  public SizedList()
  {
    super();
  }

  /**
   * overloaded constructor
   * @param size
   */
  public SizedList(int size)
  {
    super();
    this.size = size;
  }

  /** 
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(Object item)
  {
    if (this.contains(item))
    {
      this.remove(item);
    }
    super.add(0, item);
    if (this.size() > size)
    {
      this.remove(this.size() - 1);
    }
    return true;
  }

  /** 
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection c)
  {
    for (Iterator i = c.iterator(); i.hasNext();)
    {
      add(i.next());
    }
    return true;
  }

}


