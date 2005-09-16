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

package org.sakaiproject.tool.assessment.ui.model;

import java.io.Serializable;

/**
 * <p>Models paging control </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PagingModel implements Serializable
{
  private int firstItem;
  private int lastItem;
  private int numItems;
  private int totalItems;

  public int getFirstItem()
  {
    return firstItem;
  }
  public void setFirstItem(int firstItem)
  {
    this.firstItem = firstItem;
  }
  public int getLastItem()
  {
    return lastItem;
  }
  public void setLastItem(int lastItem)
  {
    this.lastItem = lastItem;
  }
  public int getNumItems()
  {
    return numItems;
  }
  public void setNumItems(int numItems)
  {
    this.numItems = numItems;
  }
  public int getTotalItems()
  {
    return totalItems;
  }
  public void setTotalItems(int totalItems)
  {
    this.totalItems = totalItems;
  }
}