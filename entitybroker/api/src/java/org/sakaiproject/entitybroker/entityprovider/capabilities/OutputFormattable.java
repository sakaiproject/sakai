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

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Allows this entity to define the output data that is returned for a reference
 * depending on the format requested,
 * if you just want to use the internal methods to handle formatting the output
 * then simply use {@link Outputable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputFormattable extends Outputable {

   /**
    * Formats the entity or collection referred to by this entity ref object
    * into output according to the format string provided
    * 
    * @param ref the parsed reference object which uniquely represents this entity
    * @param format a string constant indicating the extension format (from {@link Formats}) 
    * for output, (example: {@link #XML})
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    */
   public void formatOutput(EntityReference ref, String format, OutputStream output);
   
}
