/**
 * $Id$
 * $URL$
 * Saveable.java - entity-broker - Apr 12, 2008 1:57:05 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Convenience interface to indicates that an entity is can be saved,
 * i.e. it is creatable and updateable
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Saveable extends EntityProvider, Createable, Updateable {

   // this space left blank intentionally

}
