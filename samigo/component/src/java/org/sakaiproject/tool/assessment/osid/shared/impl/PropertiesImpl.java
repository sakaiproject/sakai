/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
