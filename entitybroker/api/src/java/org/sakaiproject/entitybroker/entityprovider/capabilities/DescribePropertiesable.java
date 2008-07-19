/**
 * $Id$
 * $URL$
 * DescribePropFileable.java - entity-broker - Jul 18, 2008 5:35:19 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
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
    * @return the baseName of the properties file/classes in the {@link #getResourceClassLoader()}
    */
   public String getBaseName();

   /**
    * Defines the classloader which will be used to load the properties bundle file/classes which
    * contain the entity descriptions
    * @return the classloader which can find the files using {@link #getBaseName()}
    */
   public ClassLoader getResourceClassLoader();

}
