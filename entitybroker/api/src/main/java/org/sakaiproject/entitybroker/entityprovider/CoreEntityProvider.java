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

package org.sakaiproject.entitybroker.entityprovider;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;

/**
 * This is the base unit for working with Sakai entities, by implementing this interface and
 * creating a spring bean you will tie your entities into Sakai, there are many other interfaces
 * which you can implement to extend the interaction of your entities with Sakai in this package<br/>
 * You (the implementor) will want to create one implementation of this interface for each type of
 * entity you want to link to Sakai to track events, provide URL access, etc.<br/>
 * <br/>
 * Usage:<br/>
 * 1) Implement this interface<br/> 
 * 2) Implement any additional capabilities interfaces (optional)<br/>
 * 3) Create a spring bean definition in the Sakai application context (components.xml)<br/> 
 * 4) Implement {@link AutoRegisterEntityProvider} or register this implementation some other way<br/>
 * <br/> 
 * Recommended best practices: (example: Thing entity)<br/> 
 * 1) Create an interface called
 * ThingEntityProvider which extends {@link EntityProvider} in api logic (add an entity package for
 * it), (e.g. org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider.java) <br/> 
 * 2) Add a public static string which contains the entity prefix (called ENTITY_PREFIX), (e.g. public final
 * static String ENTITY_PREFIX = "eval-evaluation";) <br/> 
 * 3) Implement your ThingEntityProvider in
 * impl logic as ThingEntityProviderImpl (add an entity package for it), (e.g.
 * org.sakaiproject.evaluation.logic.impl.entity.EvaluationEntityProviderImpl.java) <br/> 
 * 4) Implement {@link CoreEntityProvider} in ThingEntityProviderImpl <br/> 
 * 5) Implement {@link AutoRegisterEntityProvider} in ThingEntityProviderImpl <br/> 
 * 6) Add a spring bean
 * definition in the Sakai application context (components.xml), use the api name as the id<br/>
 * Example: <xmp> <bean id="org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider"
 * class="org.sakaiproject.evaluation.logic.impl.entity.EvaluationEntityProviderImpl"> </bean>
 * </xmp> <br/> 
 * 7) Add the needed maven dependendencies to api/logic and impl/logic project.xml
 * files<br/> 
 * Exmaple: <xmp> 
 * <dependency> 
 *    <groupId>sakaiproject</groupId>
 *    <artifactId>sakai-entitybroker-api</artifactId> 
 *    <version>${sakai.version}</version>
 * </dependency> 
 * </xmp> <br/> 
 * That should do it. You should now be able to use the
 * {@link EntityBroker} to access information about your entities and register events for your
 * entities (among other things).
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface CoreEntityProvider extends EntityProvider {

   /**
    * Check if a specific entity managed by this provider exists.<br/>
    * This is primarily used to validate references before making other calls or operating on them.<br/>
    * <b>WARNING:</b> This will be called many times and AT LEAST right before calls are made to
    * any methods or capabilities related to specific entities, please make sure this is
    * very efficient. If you are concerned about efficiency, it is ok for this method to always
    * return true but you will no longer be able to be sure that calls through to your capability
    * implementations are always valid.
    * 
    * @param id a locally unique id for an entity managed by this provider<br/>
    * <b>NOTE:</b> this will be an empty string if this is an entity space (singleton entity) without an id available
    * @return true if an entity with given local id exists, false otherwise
    */
   public boolean entityExists(String id);

}
