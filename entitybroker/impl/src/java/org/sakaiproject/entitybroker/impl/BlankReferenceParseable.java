/*
 * Created on 4 Oct 2007
 */
package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;

/** A dummy implementation of ReferenceParseable, to be used as a marker 
 * within maps for fast lookup.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 */

public class BlankReferenceParseable implements ReferenceParseable {

  public EntityReference getParsedExemplar() {
    return null;
  }

  public String getEntityPrefix() {
    return null;
  }

}
