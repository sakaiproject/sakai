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

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link ActionsExecutable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations as well as allowing for internal data output processing<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 2 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ActionsEntityProviderMock extends CRUDableEntityProviderMock implements CoreEntityProvider, ActionsExecutable, 
   Describeable, RESTful {

   public ActionsEntityProviderMock(String prefix, String[] ids) {
      super(prefix, ids);
   }

   // this one will be picked up by name (CustomAction suffix)
   public Object doubleCustomAction(EntityView view) {
      MyEntity me = (MyEntity) getEntity(view.getEntityReference());
      MyEntity togo = me.copy();
      togo.setNumber( togo.getNumber() * 2 );
      return new ActionReturn(new EntityData(view.getEntityReference().toString(), togo.getStuff(), togo), null);
   }

   @EntityCustomAction(viewKey=EntityView.VIEW_NEW)
   public void clear() {
      myEntities.clear();
   }

   @EntityCustomAction(action="xxx",viewKey=EntityView.VIEW_EDIT)
   public void xxxAction(EntityReference ref) {
      MyEntity me = (MyEntity) getEntity(ref);
      me.extra = "xxx";
      me.setStuff("xxx");
      myEntities.put(me.getId(), me);
   }

   public String[] getHandledOutputFormats() {
      return new String[] {Formats.JSON, Formats.XML};
   }

   public String[] getHandledInputFormats() {
      return new String[] {Formats.HTML, Formats.JSON, Formats.XML};
   }

}
