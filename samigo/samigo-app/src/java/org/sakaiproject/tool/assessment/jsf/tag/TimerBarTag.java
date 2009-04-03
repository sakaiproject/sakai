/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>timerBar</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TimerBarTag
  extends UIComponentTag
{

  private String id = null;
  private String expireScript;
  private String expireMessage1;
  private String expireMessage2;
  private String fiveMinutesMessage1;
  private String fiveMinutesMessage2;
  

  //private TagUtil util;
  private String elapsed;
  private String height;
  private String wait;
  private String width;


  public void setHeight(String height)
  {
    this.height = height;
  }

  public String getHeight()
  {
    return height;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "TimerBar";
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);

    //FacesContext context = getFacesContext();
    TagUtil.setInteger(component, "height", height);
    TagUtil.setInteger(component, "width", width);
    TagUtil.setInteger(component, "wait", wait);
    TagUtil.setInteger(component, "elapsed", elapsed);
    TagUtil.setString(component, "expireScript", expireScript);
    TagUtil.setString(component, "expireMessage1", expireMessage1);
    TagUtil.setString(component, "expireMessage2", expireMessage2);
    TagUtil.setString(component, "fiveMinutesMessage1", fiveMinutesMessage1);
    TagUtil.setString(component, "fiveMinutesMessage2", fiveMinutesMessage2);
  }
  public String getWidth()
  {
    return width;
  }
  public void setWidth(String width)
  {
    this.width = width;
  }
  public String getWait()
  {
    return wait;
  }
  public void setWait(String wait)
  {
    this.wait = wait;
  }
  public String getElapsed()
  {
    return elapsed;
  }
  public void setElapsed(String elapsed)
  {
    this.elapsed = elapsed;
  }
  public String getExpireScript()
  {
    return expireScript;
  }
  public void setExpireScript(String expireScript)
  {
    this.expireScript = expireScript;
  }
  public String getExpireMessage1()
  {
    return expireMessage1;
  }
  public void setExpireMessage1(String expireMessage1)
  {
    this.expireMessage1 = expireMessage1;
  }
  
  public String getExpireMessage2()
  {
    return expireMessage2;
  }
  public void setExpireMessage2(String expireMessage2)
  {
    this.expireMessage2 = expireMessage2;
  }
  
  public String getFiveMinutesMessage1()
  {
    return fiveMinutesMessage1;
  }
  public void setFiveMinutesMessage1(String fiveMinutesMessage1)
  {
    this.fiveMinutesMessage1 = fiveMinutesMessage1;
  }

  public String getFiveMinutesMessage2()
  {
    return fiveMinutesMessage2;
  }
  public void setFiveMinutesMessage2(String fiveMinutesMessage2)
  {
    this.fiveMinutesMessage2 = fiveMinutesMessage2;
  }
  
}
