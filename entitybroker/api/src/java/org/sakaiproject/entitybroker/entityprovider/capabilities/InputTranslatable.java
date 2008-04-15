/**
 * $Id$
 * $URL$
 * InputTranslatable.java - entity-broker - Apr 12, 2008 2:03:28 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.InputStream;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Allows this entity to define the way to translate data for a reference
 * into an entity object depending on the format requested,
 * if you just want to use the internal methods to handle formatting the input
 * into an entity then simply use {@link Inputable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface InputTranslatable extends Inputable {

   /**
    * Translates the input data stream in the supplied format into an entity object for this reference
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * of the input, (example: {@link #XML})
    * @param input an stream which contains the data to make up this entity,
    * you may assume this is UTF-8 encoded if you don't know anything else about it
    * @return an entity object of the type used for these entities
    */
   public Object translateFormattedData(EntityReference ref, String format, InputStream input);

}
