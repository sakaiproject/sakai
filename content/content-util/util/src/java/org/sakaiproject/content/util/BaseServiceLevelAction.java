package org.sakaiproject.content.util;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.content.api.ServiceLevelAction;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 26, 2007
 * Time: 10:17:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseServiceLevelAction extends BaseResourceAction implements ServiceLevelAction {

   private boolean multipleItemAction;

   public BaseServiceLevelAction(String id, ActionType actionType, String typeId, boolean multipleItemAction) {
      super(id, actionType, typeId);
      this.multipleItemAction = multipleItemAction;
   }

   /**
    * This method effects this action on the entity specified.
    *
    * @param reference
    */
   public void invokeAction(Reference reference) {
   }

   public boolean isMultipleItemAction() {
      return multipleItemAction;
   }

   /**
    * This method is invoked before the Resources tool does its part of the action
    * in case the registrant needs to participate in the action at that point.
    *
    * @param reference A reference to the entity with respect to which the action is taken
    */
   public void initializeAction(Reference reference) {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   /**
    * This method is invoked after the Resources tool does its part of the action
    * in case the registrant needs to participate in the action at that point.  Will
    * not be invoked after cancelAction(Reference reference) is invoked.
    *
    * @param entity A reference to the entity  with respect to which the action is taken
    */
   public void finalizeAction(Reference reference) {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   /**
    * This method is invoked if the Resources tool cancels the action after invoking
    * the initializeAction(Reference reference) method in case the registrant needs to
    * clean up a canceled action at that point. Will not be invoked after
    * finalizeAction(Reference reference) is invoked.
    *
    * @param entity A reference to the entity  with respect to which the action is taken
    */
   public void cancelAction(Reference reference) {
      //To change body of implemented methods use File | Settings | File Templates.
   }

}
