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

import java.io.OutputStream;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Indicates an entity provider has the capability of exporting entity data which is related to
 * other entities, note that the decision about which data to export is left up to the implementor
 * based on the reference supplied <br/> This is one of the capability extensions for the
 * {@link EntityProvider} interface<br/>
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface Exportable extends EntityProvider {

   /**
    * Request an export stream of data from an entity provider for all data related to a specific
    * entity (this will probably not be an entity in this provider)
    * 
    * @param reference
    *           a globally unique reference to an entity, this is the entity that the exported data
    *           should be associated with (e.g. a reference to a site object or user)
    * @param data
    *           a stream to put the export data into which will be saved by the archiver/exporter
    * @return a string representing the encoding used and possibly other info like a version, this
    *         allows the export to provide tips to the import when data is streamed back in, if
    *         there is no data to export then a null will be returned
    */
   public String exportData(String reference, OutputStream data);

}
