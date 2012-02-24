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
 * This class is the tag handler for a next/previous control for a pager attached to a dataTable.</p>
* <p>Usage:
<code><pre>
 <h:form id="questionpool">
...
<samigo:pagerButtonControl controlId="test" formId="questionpool" />

<h:dataTable id="TreeTable" value="#{questionpool.testPools}"...
...
</h:dataTable>

<samigo:pager controlId="test" dataTableId="TreeTable" showLinks="true"
  showpages="999" styleClass="rtEven" selectedStyleClass="rtOdd"/>
 </h:form>
</pre></code>
</p>

 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class PagerButtonControlTag
  extends UIComponentTag
{

  private String formId = null;
  private String controlId = null;

  public void setFormId(String formId)
  {
    this.formId = formId;
  }

  public String getFormId()
  {
    return formId;
  }

  public void setControlId(String controlId)
  {
    this.controlId = controlId;
  }

  public String getControlId()
  {
    return controlId;
  }

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "PagerButtonControl";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);

    if (controlId != null)
    {
      component.getAttributes().put("controlId", controlId);
    }

    if (formId != null)
    {
      component.getAttributes().put("formId", formId);
    }

  }

}
