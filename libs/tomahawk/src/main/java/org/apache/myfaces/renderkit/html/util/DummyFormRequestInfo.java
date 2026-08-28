/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.renderkit.html.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Convenient class to store whether a dummyForm needs to be rendered and its params.
 * This class will be stored in the request when a dummyForm is needed to be rendered in the page.
 * AddResources will add it from a method called from the ExtensionsFilter.
 * <p>
 * All this replaces the old system based in a DummyFormResponseWriter
 *
 * @author Bruno Aranda (latest modification by $Author: grantsmith $)
 * @version $Revision: 472792 $ $Date: 2006-11-09 01:34:47 -0500 (Thu, 09 Nov 2006) $
 */
public class DummyFormRequestInfo
{

   private Set _dummyFormParams = null;

   public String getDummyFormName()
   {
       return DummyFormUtils.DUMMY_FORM_NAME;
   }

   public void addDummyFormParameter(String paramName)
   {
       if (_dummyFormParams == null)
       {
           _dummyFormParams = new HashSet();
       }
       _dummyFormParams.add(paramName);
   }

   public Set getDummyFormParams()
   {
       return _dummyFormParams;
   }
}
