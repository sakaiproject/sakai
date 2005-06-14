/*
 * SerializableSerializableIterator.java
 *
 * Copyright 2004, Stanford University Trustees
 */
package org.sakaiproject.tool.assessment.osid.shared.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.osid.shared.SharedException;
import org.osid.shared.ObjectIterator;

/**
 * An object iterator implementation.
 *
 * @author Rachel Gollub <rgollub@stanford.edu>
 */

public class ObjectIteratorImpl implements ObjectIterator
{
  private Iterator objects;

  public ObjectIteratorImpl(Iterator iterator)
  {
    objects = iterator;
  }

  public boolean hasNextObject() throws SharedException
  {
    return objects.hasNext();
  }

  public Serializable nextObject() throws SharedException
  {
    try
      {
        return (Serializable) objects.next();
      }
    catch (Exception e)
      {
        throw new SharedException("No objects to return.");
      }
  }
}

