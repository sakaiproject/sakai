/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.util;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;

/**
 * Allows a bean to report the classloader that is appropriate for it and will be used
 * for dispatching into this beans environment, this is only needed in advanced
 * cases and should not normally be implemented<br/>
 * The primary use case here is to allow someone to set their classloader when they
 * are using a proxied bean or the implementation class is in the wrong classloader<br/>
 * This is primarily used in the case of the {@link HttpServletAccessProvider}
 * or {@link EntityViewAccessProvider} 
 * and the implementations of those should also implement this interface 
 * to be able to specify the classloader
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ClassLoaderReporter {

   /**
    * @return the classloader that is appropriate for executing methods against this bean
    */
   public ClassLoader getSuitableClassLoader();

}
