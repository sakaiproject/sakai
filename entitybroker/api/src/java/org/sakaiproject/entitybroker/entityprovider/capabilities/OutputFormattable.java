/**
 * $Id$
 * $URL$
 * HTMLdefineable.java - entity-broker - Apr 6, 2008 7:44:11 PM - azeckoski
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

import java.io.OutputStream;
import java.util.List;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Allows this entity to define the output data format for a reference
 * or a list of entity objects depending on the format requested,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputFormattable extends Outputable {

   /**
    * Formats the entity or collection included or referred to by this entity ref object
    * into output according to the format string provided,
    * Should take into account the reference when determining what the entities are
    * and how to encode them
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * for output, (example: {@link #XML})
    * @param entities (optional) a list of entities to create formatted output for,
    * if this is null then the entities should be retrieved based on the reference,
    * if this contains only a single item AND the ref refers to a single entity
    * then the entity should be extracted from the list and encoded without the indication
    * that it is a collection, for all other cases the encoding should include an indication that
    * this is a list of entities
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    */
   public void formatOutput(EntityReference ref, String format, List<?> entities, OutputStream output);

}
