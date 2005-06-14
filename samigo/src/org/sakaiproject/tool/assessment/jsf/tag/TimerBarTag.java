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
import javax.faces.context.FacesContext;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>timerBar</code>
 * custom tag.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: TimerBarTag.java,v 1.1 2004/12/15 00:59:02 esmiley.stanford.edu Exp $
 */

public class TimerBarTag
  extends UIComponentTag
{

  private String id = null;
  private String expireScript;
  private String expireMessage;

  private TagUtil util;
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

    FacesContext context = getFacesContext();
    util.setInteger(component, "height", height);
    util.setInteger(component, "width", width);
    util.setInteger(component, "wait", wait);
    util.setInteger(component, "elapsed", elapsed);
    util.setString(component, "expireScript", expireScript);
    util.setString(component, "expireMessage", expireMessage);
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
  public String getExpireMessage()
  {
    return expireMessage;
  }
  public void setExpireMessage(String expireMessage)
  {
    this.expireMessage = expireMessage;
  }

}
