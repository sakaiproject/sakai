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

