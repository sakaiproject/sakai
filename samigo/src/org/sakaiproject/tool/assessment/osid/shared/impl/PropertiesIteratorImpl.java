/*
 * PropertiesIteratorImpl.java
 *
 * Copyright 2004, Stanford University Trustees
 */
package org.sakaiproject.tool.assessment.osid.shared.impl;

import java.util.Iterator;

import org.osid.shared.Properties;
import org.osid.shared.PropertiesIterator;
import org.osid.shared.SharedException;

/**
 * A properties iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */

public class PropertiesIteratorImpl implements PropertiesIterator
{
  private Iterator objects;

  public PropertiesIteratorImpl(Iterator iterator)
  {
    objects = iterator;
  }

  public boolean hasNextProperties() throws SharedException
  {
    return objects.hasNext();
  }

  public Properties nextProperties() throws SharedException
  {
    try
      {
        return (Properties) objects.next();
      }
    catch (Exception e)
      {
        throw new SharedException("No objects to return.");
      }
  }
}

