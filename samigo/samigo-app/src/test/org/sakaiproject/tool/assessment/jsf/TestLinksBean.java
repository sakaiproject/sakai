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
import java.util.ArrayList;

/**
 * <p> </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class TestLinksBean implements Serializable
{
  public TestLinksBean()
  {
    String[] actions = { "select", "author", "template" };
    links = new ArrayList();
    for (int i = 0; i < actions.length; i++)
    {
      TestLink link = new TestLink();
      link.setAction(actions[i]);
      link.setText("Link to " + actions[i]);
      links.add(link);
    }

  }
  private ArrayList links;
  public ArrayList getLinks()
  {
    return links;
  }
  public void setLinks(ArrayList links)
  {
    this.links = links;
  }
}