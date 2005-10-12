/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/helper/standalone/AgentHelperImpl.java $
 * $Id: AgentHelperImpl.java 2217 2005-09-30 19:12:26Z esmiley@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
 *
 **********************************************************************************/
package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

/**
 *
 * <p>Description:
 * This is a singleton bean with default properties and factory method.
 * It provides miscellaneous Agent state information in one place.</p>
 * <p>Accessed by DeliveryBean, as well as facade and integration packages. </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class AgentState implements Serializable
{
  public static final String UNASSIGNED = "UNASSIGNED";
  private static AgentState instance = null;

  private boolean accessViaUrl;
  private String agentAccessString;

  private AgentState()
  {
    accessViaUrl = false;
    agentAccessString = UNASSIGNED;
  }

  public static AgentState getInstance()
  {
    if (instance==null)
    {
      instance = new AgentState();
    }
    return instance;
  }
  public boolean isAccessViaUrl()
  {
    return accessViaUrl;
  }
  public void setAccessViaUrl(boolean accessViaUrl)
  {
    this.accessViaUrl = accessViaUrl;
  }
  public String getAgentAccessString()
  {
    return agentAccessString;
  }
  public void setAgentAccessString(String agentAccessString)
  {
    this.agentAccessString = agentAccessString;
  }
}