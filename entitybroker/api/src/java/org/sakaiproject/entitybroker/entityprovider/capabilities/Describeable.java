/**
 * $Id$
 * $URL$
 * Describeable.java - entity-broker - Jul 18, 2008 5:15:58 PM - azeckoski
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

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;


/**
 * This entity will describe itself,
 * this description can be accessed for display in interfaces,
 * the description should generally be targeted at developers
 * (in particular, those using REST or web services)<br/>
 * This is the convention interface, it will simply look for the properties file
 * in the classloader which your {@link EntityProvider} is located in,
 * the file must be named <b>&lt;entity-prefix&gt;.properties</b> (e.g. myentity.properties)<br/>
 * The keys inside the file must be as follows:<br/>
 * &lt;entity-prefix&gt; = This is the main description of the entity, appears at the top <br/>
 * &lt;entity-prefix&gt;.view.&lt;viewKey&gt; = This is a description about a particular view for an entity (viewKey from entity view constants {@link EntityView#VIEW_LIST}) <br/>
 * &lt;entity-prefix&gt;.action.&lt;actionKey&gt; = This is a description of a custom action for this entity (see {@link ActionsExecutable} or {@link EntityCustomAction}) <br/>
 * &lt;entity-prefix&gt;.field.&lt;fieldName&gt; = This is a description about a particular entity field for this entity object (see {@link Resolvable} or {@link CollectionResolvable}) <br/>
 * &lt;entity-prefix&gt;.redirect.&lt;redirectTemplate&gt; = This is a description about a particular redirect rule for this entity URL space (see {@link Redirectable} or {@link EntityURLRedirect}) <br/>
 * &lt;entity-prefix&gt;.&lt;capability&gt; = This is a description about a particular capability for this entity <br/>
 * <b>Example:</b><xmp>
      myentity = This is my entity, it is used for <b>examples</b> only
      myentity.view.show = this shows a single instance of my entity
      myentity.action.copy = this makes a copy of an instance of a myentity
      myentity.field.name = this is the name of the entity, it is a user displayable name
      myentity.redirect./{prefix}/xml/{id} = redirects to an xml view of a myentity
      myentity.Inputable = <i>extra</i> notes about the Inputable implementation for myentity
 * </xmp>
 * <br/>
 * If you need to define the location of your properties file then use: {@link DescribePropertiesable}
 * <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface Describeable extends EntityProvider {

   // this space left blank intentionally

}
