/*
 * AgentImpl.java
 *
 * Copyright 2003, Stanford University.
 */
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
