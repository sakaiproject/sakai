/**
 * $Id$
 * $URL$
 * Describeable.java - entity-broker - Jul 18, 2008 5:15:58 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * This entity will describe itself,
 * this description can be accessed for display in interfaces,
 * the description should generally be targeted at developers
 * (in particular, those using REST or web services)<br/>
 * This is the convention interface, it will simply look for the properties file
 * in the classloader which your {@link EntityProvider} is located in,
 * the file must be named <b>&lt;entity-prefix&gt;.properties</b> (e.g. myentity.properties)<br/>
 * The keys inside the file must be as follows:<br/>
 * &lt;entity-prefix&gt; = This is the main description of the entity, appears at the top<br/>
 * &lt;entity-prefix&gt;.&lt;capability&gt; = This is a description about a particular capability for this entity<br/>
 * <b>Example:</b><xmp>
      myentity = This is my entity, it is used for <b>examples</b> only
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
