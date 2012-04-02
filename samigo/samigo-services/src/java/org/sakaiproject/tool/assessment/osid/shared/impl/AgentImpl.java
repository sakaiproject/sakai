/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
