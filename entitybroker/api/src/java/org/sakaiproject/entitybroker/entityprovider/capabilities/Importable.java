/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 **/

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.InputStream;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates an entity provider has the capability of importing entity data that was previously
 * exported via the {@link Exportable} capability which will be related to other entities, note that
 * the way to associate the data is left up to the implementor based on the reference supplied <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Importable extends Exportable {

   /**
    * Request that an import stream be turned into real data related to the entities in this
    * provider and associated with a specific entity (this will probably not be an entity in this
    * provider)
    * 
    * @param reference
    *           a globally unique reference to an entity, this is the entity that the imported data
    *           should be associated with (e.g. a reference to a site object or user)
    * @param data
    *           a stream of data from the archiver/importer, this should match a previous export
    *           stream exactly
    * @param encodingKey
    *           a string representing the encoding used and possibly other info like a version, this
    *           should be the string sent with the export
    * @return true if any data was imported, false if none was imported or an error occurred
    */
   public boolean importData(String reference, InputStream data, String encodingKey);

}
