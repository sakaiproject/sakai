/**
 * $Id$
 * $URL$
 * DescribePropFileable.java - entity-broker - Jul 18, 2008 5:35:19 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Properties;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;


/**
 * This allows an entity to define the {@link Properties} files/classes to be used
 * for looking up descriptions of the entity ({@link Describeable})<br/>
 * This is the configuration interface<br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * @see Describeable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface DescribePropertiesable extends Describeable {

   /**
    * Defines the {@link Properties} file/class baseName which is used to find descriptions of an entity
    * as per the keys defined in {@link Describeable}<br/>
    * The baseName will be the file without the .properties or the locale codes<br/>
    * Examples:<br/>
    * <code>myentity</code> - would match: myentity.properties, myentity_en.properties, myentity_en_GB.properties, etc.<br/>
    * <code>mydir/myloc/mything</code> - would match: mydir/myloc/mything.properties, etc.<br/>
    * @see Describeable for details about the keys to place in the properties file
    * @return the baseName of the properties file/classes in the {@link #getResourceClassLoader()}
    */
   public String getBaseName();

   /**
    * Defines the ClassLoader which will be used to load the properties bundle file/classes which
    * contain the entity descriptions
    * @return the ClassLoader which can find the files using {@link #getBaseName()}
    */
   public ClassLoader getResourceClassLoader();

}
