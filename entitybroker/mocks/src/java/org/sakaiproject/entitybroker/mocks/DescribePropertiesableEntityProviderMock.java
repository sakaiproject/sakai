/**
 * $Id$
 * $URL$
 * DescribePropertiesableEntityProviderMock.java - entity-broker - Jul 19, 2008 9:40:43 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link DescribePropertiesable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations also<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 3 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DescribePropertiesableEntityProviderMock extends CRUDableEntityProviderMock implements
      DescribePropertiesable {

   public DescribePropertiesableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable#getBaseName()
    */
   public String getBaseName() {
      return "org/sakaiproject/entitybroker/mocks/testPrefixCustom";
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable#getResourceClassLoader()
    */
   public ClassLoader getResourceClassLoader() {
      return DescribePropertiesableEntityProviderMock.class.getClassLoader();
   }

}
