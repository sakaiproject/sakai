/*
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
 */

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
 * @version $Id: NavigationMapTag.java,v 1.4 2004/10/21 00:55:13 esmiley.stanford.edu Exp $
 */

public class NavigationMapTag
  extends UIComponentTag
{

  private String map;
  private String separator;
  private String style;
  private String linkStyle;

  private TagUtil util;

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

    util.setMap(component, "map", map);
    util.setString(component, "separator", separator);
    util.setString(component, "style", style);
    util.setString(component, "linkStyle", linkStyle);
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
