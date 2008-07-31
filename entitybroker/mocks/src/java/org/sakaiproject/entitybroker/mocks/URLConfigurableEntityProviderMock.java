/**
 * $Id$
 * $URL$
 * RESTfulEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.mocks;

import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.URLConfigurable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link URLConfigurable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations as well as allowing for internal data output processing<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 2 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class URLConfigurableEntityProviderMock extends RESTfulEntityProviderMock implements CoreEntityProvider, RESTful, 
      URLConfigurable {

   public URLConfigurableEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   @EntityURLRedirect("/{prefix}/{id}/{thing}/go")
   public String outsideRedirector(String incomingURL, Map<String, String> values) {
      return "http://caret.cam.ac.uk/?prefix=" + values.get(EntityView.PREFIX) + "&thing=" + values.get("thing");
   }

   @EntityURLRedirect("/{prefix}/xml/{id}")
   public String xmlRedirector(String incomingURL, Map<String, String> values) {
      return values.get(EntityView.PREFIX) + 
         EntityView.SEPARATOR + values.get(EntityView.ID) + EntityView.PERIOD + Formats.XML;
   }

   @EntityURLRedirect("/{prefix}/going/nowhere")
   public String returningRedirector(String incomingURL, Map<String, String> values) {
      return "";
   }

   @EntityURLRedirect("/{prefix}/keep/moving")
   public String neverRedirector(String incomingURL, Map<String, String> values) {
      return null;
   }

}
