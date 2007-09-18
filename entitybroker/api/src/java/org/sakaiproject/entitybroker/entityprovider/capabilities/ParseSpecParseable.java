/**
 * ParseSpecParseable.java - created by antranig on 01 June 2007
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * THIS IS NOT HOOKED UP TO ANYTHING YET, DO NOT BOTHER IMPLEMENTING THIS -AZ
 * 
 * Allows an entity provider to define the parsing specification for its own reference string <br/>
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ParseSpecParseable extends ReferenceParseable {

   public String getParseSpec();

}
