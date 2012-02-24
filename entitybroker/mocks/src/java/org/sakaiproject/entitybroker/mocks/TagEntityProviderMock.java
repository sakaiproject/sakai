/**
 * $Id$
 * $URL$
 * TagEntityProviderMock.java - entity-broker - Aug 8, 2008 1:54:21 PM - azeckoski
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

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;


/**
 * This is an entity provider which uses the internal tagging service
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class TagEntityProviderMock extends CRUDableEntityProviderMock implements Taggable {

   public TagEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   
   // nothing else to do here

}
