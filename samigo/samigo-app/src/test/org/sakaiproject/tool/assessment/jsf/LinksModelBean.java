/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.assessment.jsf;
import java.io.Serializable;

/**
 * <p> test bean</p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class LinksModelBean implements Serializable
{
  private java.util.ArrayList linkListList;

  public LinksModelBean()
  {
    linkListList = new java.util.ArrayList();
    for (int i = 0; i < 20; i++) {
      linkListList.add(new TestLinksBean());
    }
  }

  public java.util.ArrayList getLinkListList()
  {
    return linkListList;
  }
  public void setLinkListList(java.util.ArrayList linkListList)
  {
    this.linkListList = linkListList;
  }

  public static void main(String args[])
  {
    LinksModelBean bean = new LinksModelBean();
    java.util.ArrayList list = bean.getLinkListList();

    for (int i = 0; i < list.size(); i++) {
    }
  }


}