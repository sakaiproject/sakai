package org.sakaiproject.tool.assessment.osid.shared.impl;

import org.osid.shared.Properties;
import org.osid.shared.ObjectIterator;
import org.osid.shared.Type;

import java.io.Serializable;
import java.util.HashMap;

public class PropertiesImpl implements Properties
{
  HashMap hashmap = null;

  public PropertiesImpl(HashMap map)
  {
    hashmap = map;
  }

  public Type getType() throws org.osid.shared.SharedException
  {
    return null;
  }

  public Serializable getProperty(java.io.Serializable key)
        throws org.osid.shared.SharedException
  {
    return (Serializable) hashmap.get(key);
  }

  public ObjectIterator getKeys() throws org.osid.shared.SharedException
  {
    return new ObjectIteratorImpl(hashmap.keySet().iterator());
  }
}
