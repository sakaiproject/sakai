/**
 * $Id$
 * $URL$
 * AbstractEntityProvider.java - entity-broker - Apr 30, 2008 7:26:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.util;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * WARNING: Requires Spring 1.2.8 or newer libraries in the classpath <br/>
 * Makes it easier to write {@link EntityProvider}s in webapps <br/>
 * A class to extend that gets rid of some of the redundant code that has
 * to be written over and over, causes this provider to be registered when it
 * is created and unregistered when it is destroyed, also includes the
 * {@link DeveloperHelperService} as a protected variable,
 * pairs with the parent bean (org.sakaiproject.entitybroker.entityprovider.AbstractEntityProvider)<br/>
 * Create your spring bean like so (class is your provider, set whatever properties you are using):
 * <xmp><bean parent="org.sakaiproject.entitybroker.entityprovider.AbstractEntityProvider" 
      class="org.sakaiproject.entitybroker.entitywebapp.WebappEntityProvider">
      <property name="dao" ref="MemoryDao" />
   </bean></xmp>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public abstract class AbstractEntityProvider implements EntityProvider, InitializingBean, DisposableBean {

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   protected DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
      this.developerHelperService = developerHelperService;
   }

   // TODO add in annotations?
   public void afterPropertiesSet() throws Exception {
      entityProviderManager.registerEntityProvider(this);
   }

   public void destroy() throws Exception {
      entityProviderManager.unregisterEntityProvider(this);
   }

}
