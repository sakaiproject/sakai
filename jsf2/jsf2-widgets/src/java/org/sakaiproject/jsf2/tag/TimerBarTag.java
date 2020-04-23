/**********************************************************************************
Copyright (c) 2018 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************************/

package org.sakaiproject.jsf2.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import lombok.Data;

import org.sakaiproject.jsf2.util.TagUtil;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>timerBar</code>
 * custom tag.</p>
 */

 @Data
public class TimerBarTag
  extends UIComponentTag
{

  private String id = null;
  private String expireScript;
  private String expireMessage;

  private String elapsed;
  private String height;
  private String wait;
  private String width;
  private String elapsedColor;
  private String remainingColor;
  private String expireHandling;

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.TimerBar";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    TagUtil.setInteger(component, "height", height);
    TagUtil.setInteger(component, "width", width);
    TagUtil.setInteger(component, "wait", wait);
    TagUtil.setInteger(component, "elapsed", elapsed);
    TagUtil.setString(component, "expireScript", expireScript);
    TagUtil.setString(component, "expireMessage", expireMessage);
    TagUtil.setString(component, "elapsedColor", elapsedColor);
    TagUtil.setString(component, "remainingColor", remainingColor);
    TagUtil.setString(component, "expireHandling", expireHandling);
  }
}
