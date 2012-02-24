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




package org.sakaiproject.tool.assessment.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 *
 * <p>Description:<br />
 * This class is the tag handler for a set of navigation links.</p>
 * <p>
 * attributes:
 *    map: a Map with key=link text value=url or javascript
 *    separator: a string used to separate links
 *    style: if present, the CSS style to be applied as a span tag</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class NavigationMapTag
  extends UIComponentTag
{

  private String map;
  private String separator;
  private String style;
  private String linkStyle;

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "NavigationMap";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    TagUtil.setMap(component, "map", map);
    TagUtil.setString(component, "separator", separator);
    TagUtil.setString(component, "style", style);
    TagUtil.setString(component, "linkStyle", linkStyle);
  }

  /**
   * Map of key=link text value=url or javascript entries
   * @return the Map
   */
  public String getMap()
  {
    return map;
  }

  /**
   * Map of key=link text value=url or javascript entries
   * @param map the Map
   */
  public void setMap(String map)
  {
    this.map = map;
  }

  /**
   * separator for links
   * @return a separator
   */
  public String getSeparator()
  {
    return separator;
  }

  /**
   * separator for links
   * @param separator separator for links
   */

  public void setSeparator(String separator)
  {
    this.separator = separator;
  }

  /**
   * display CSS style
   * @return CSS style
   */
  public String getStyle()
  {
    return style;
  }

  /**
   * CSS style to display
   * @param style CSS style
   */
  public void setStyle(String style)
  {
    this.style = style;
  }

  /**
   * CSS style to apply to the link
   * @return CSS style
   */
  public String getLinkStyle()
  {
    return linkStyle;
  }

  /**
   * CSS style to apply to the link
   * @param linkStyle CSS style
   */
  public void setLinkStyle(String linkStyle)
  {
    this.linkStyle = linkStyle;
  }


}
