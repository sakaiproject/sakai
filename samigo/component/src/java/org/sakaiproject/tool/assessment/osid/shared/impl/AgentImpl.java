/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.osid.shared.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.osid.agent.Agent;
import org.osid.shared.Id;
import org.osid.shared.Type;
import org.osid.shared.TypeIterator;
import org.osid.agent.AgentException;
import org.osid.shared.PropertiesIterator;
import org.osid.shared.Properties;
/**
 * A Stanford implementation of Agent for AAM/Navigo.
 *
 * @author Rachel Gollub
 */
public class AgentImpl implements Agent
{
  private Id id;
  private String displayName;
  private Type type;
  private PropertiesIterator propertiesIterator;
  private Collection properties;

  public AgentImpl(String pname, Type ptype, Id pid) {
    displayName = pname;
    type = ptype;
    id = pid;
    properties = new ArrayList();
    // need to load it from somewhere later. -daisyf 10/11/04
    propertiesIterator = new PropertiesIteratorImpl(properties.iterator());
  }

  public Id getId()
  {
    return id;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public Type getType()
  {
    return type;
  }

  public PropertiesIterator getProperties()
  {
    return propertiesIterator;
  }

  public TypeIterator getPropertiesTypes() throws AgentException
  {
    throw new AgentException(AgentException.UNIMPLEMENTED);
  }


  public Properties getPropertiesByType(Type propertiesType) throws AgentException
  {
    throw new AgentException(AgentException.UNIMPLEMENTED);
  }

  public TypeIterator getPropertyTypes() throws AgentException
  {
    throw new AgentException(AgentException.UNIMPLEMENTED);
  }

}
