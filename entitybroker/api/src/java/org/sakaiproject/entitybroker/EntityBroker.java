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

package org.sakaiproject.entitybroker;

import java.util.Set;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.PropertiesProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TagProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.TagSearchProvider;

/**
 * This service interface defines the capabilities of the entity broker system<br/> 
 * It allows Sakai system methods, developers, etc. to access Sakai entity information (new and old)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EntityBroker extends PropertiesProvider, TagProvider, TagSearchProvider {

   /**
    * Check if an entity exists by the globally unique reference string, (the global reference
    * string will consist of the entity prefix and any local ID). If no {@link EntityProvider} for
    * the reference is found which implements {@link CoreEntityProvider}, this method will return
    * <code>true</code>
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments (normally the id at least)
    * @return true if the entity exists, false otherwise
    */
   public boolean entityExists(String reference);

   /**
    * Retrieve a complete set of all currently registered {@link EntityProvider} prefixes
    * 
    * @return all currently registered entity prefixes
    */
   public Set<String> getRegisteredPrefixes();

   /**
    * Get the full absolute URL to the entity defined by this entity reference, this will fail-safe
    * to a direct URL to an entity space URL if that is all that is available
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @return a full URL string (e.g. http://server/direct/prefix/id)
    */
   public String getEntityURL(String reference);

   /**
    * Get the full absolute URL to the entity view defined by these params, this will fail-safe
    * to a direct URL to an entity space URL if that is all that is available,
    * this will use the default entity URL template associated with the viewKey and include
    * an optional extension if specified (these will be inferred if they are missing)
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optionally the local id
    * @param viewKey the specific view type to get the URL for,
    * use the VIEW_* constants from {@link EntityView} (e.g. {@link EntityView#VIEW_LIST}),
    * can be null to determine the key automatically
    * @param extension the optional extension to add to the end 
    * which defines the expected data which is returned,
    * use constants in {@link Outputable} (e.g. {@link Outputable#XML}),
    * can be null to use no extension,  default is assumed to be html if none is set
    * @return the full URL string to a specific entity or space,
    * (e.g. http://server/direct/prefix/id)
    */
   public String getEntityURL(String reference, String viewKey, String extension);

   /**
    * Fire an event to Sakai with the specified name, targetted at the supplied reference, which
    * should be a reference to an existing entity managed by this broker<br/>
    * <b>NOTE:</b> This will allow events to be fired for references without a broker or invalid references
    * 
    * @param eventName a string which represents the name of the event (e.g. announcement.create),
    * cannot be null or empty
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments,
    * cannot be null or empty
    */
   public void fireEvent(String eventName, String reference);

   /**
    * Parses an entity reference into a concrete object, of type {@link EntityReference}, or some
    * class derived from it, for example {@link IdEntityReference} or some other class of object
    * which is returned from {@link ParseSpecParseable}.
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @return an entity reference object which will contain the entity prefix and any optional
    *         segments, or <code>null</code> if the reference was not recognized as a valid entity
    *         handled by the broker (will be an {@link IdEntityReference} if there is an id set)
    */
   public EntityReference parseReference(String reference);

   /**
    * Fetches a concrete object representing this entity reference; either one from the
    * {@link Resolvable} capability if implemented by the responsible {@link EntityProvider}, or
    * else from the underlying Sakai entity system.
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @return an object which represents the entity or null if none can be found
    */
   public Object fetchEntity(String reference);

}
