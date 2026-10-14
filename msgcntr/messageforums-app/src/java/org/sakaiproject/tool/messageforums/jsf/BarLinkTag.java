/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/BarLinkTag.java $
 * $Id: BarLinkTag.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.tool.messageforums.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentTag;
import lombok.Setter;
import org.sakaiproject.jsf2.util.TagUtil;

/**
 * @author Chen Wen
 * @version $Id$
 * 
 */
@Setter
public class BarLinkTag extends UIComponentTag
{
  private String action;
  private String value;
  private String title;
  private String disabled;
  private String immediate;

  @Override
  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setAction(component, action);
    TagUtil.setObject(component, "value", value);
    TagUtil.setString(component, "title", title);
    TagUtil.setBoolean(component, "disabled", disabled);
    TagUtil.setBoolean(component, "immediate", immediate);
  }

  @Override
  public void release()
  {
    super.release();
    action = null;
    value = null;
    title = null;
    disabled = null;
    immediate = null;
  }

  public String getComponentType()
  {
    return "BarLink";
  }

  public String getRendererType()
  {
    return "org.sakaiproject.tool.messageforums.jsf.BarLinkRenderer";
  }
}


