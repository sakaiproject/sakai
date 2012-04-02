/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

