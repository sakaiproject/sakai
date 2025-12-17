/**
 * $Id$
 * $URL$
 * RESTfulEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
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

import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link RedirectDefinable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations as well as allowing for internal data output processing<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 2 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RedirectDefineableEntityProviderMock extends RESTfulEntityProviderMock implements CoreEntityProvider, RESTful, 
      RedirectDefinable {

   public RedirectDefineableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   public String[] templates = new String[] {
         "/{prefix}/site/{siteId}/user/{userId}", 
         "/{prefix}?siteId={siteId}&userId={userId}",
         "/{prefix}/{id}/{thing}/go", 
         "other/stuff?prefix={prefix}&id={id}",
         "/{prefix}/xml/{id}",
         "/{prefix}/{id}.xml"
   };

   public TemplateMap[] defineURLMappings() {
      return new TemplateMap[] {
            new TemplateMap(templates[0], templates[1]),
            new TemplateMap(templates[2], templates[3]),
            new TemplateMap(templates[4], templates[5])
      };
   }

   @EntityURLRedirect("/{prefix}/going/nowhere")
   public String returningRedirector(String incomingURL, Map<String, String> values) {
      return "";
   }

   @EntityURLRedirect("/{prefix}/keep/moving")
   public String neverRedirector(String incomingURL, Map<String, String> values) {
      return null;
   }

}
