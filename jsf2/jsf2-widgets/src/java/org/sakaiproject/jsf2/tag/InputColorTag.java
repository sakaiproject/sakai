/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.jsf2.tag;

import org.sakaiproject.jsf2.util.TagUtil;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


/**
 * @todo make default alt tag come from resource
 *
 *
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler custom for color picker control</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class InputColorTag extends UIComponentTag
{
  private String value;

  public String getComponentType()
  {
    return ("javax.faces.Input");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.InputColor";
  }

  /**
   * Set the properties.
   * @param component
   */
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    TagUtil.setString(component, "value", value);
  }
  public String getValue()
  {
    return value;
  }
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   *
   * @return String value
   */

}
