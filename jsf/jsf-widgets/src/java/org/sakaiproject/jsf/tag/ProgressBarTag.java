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


package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>timerBar</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ProgressBarTag
  extends UIComponentTag
{

  private String id = null;
  private String wait;


  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }


  public String getRendererType()
  {
    return "org.sakaiproject.ProgressBar";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setInteger(component, "wait", wait);
  }

  public String getWait()
  {
    return wait;
  }
  public void setWait(String wait)
  {
    this.wait = wait;
  }

  public String getComponentType() {
    return ("javax.faces.Output");
  }


}
