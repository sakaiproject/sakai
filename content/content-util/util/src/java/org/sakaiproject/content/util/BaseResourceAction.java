package org.sakaiproject.content.util;

import org.sakaiproject.content.api.ResourceToolAction;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 26, 2007
 * Time: 10:38:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseResourceAction implements ResourceToolAction {
   protected String id;
   protected ActionType actionType;
   protected String typeId;


   public BaseResourceAction(String id, ActionType actionType, String typeId) {
      this.id = id;
      this.actionType = actionType;
      this.typeId = typeId;
   }

   /**
    * Access the id of this action (which must be unique within this type and must be limited to alphnumeric characters).
    *
    * @return
    */
   public String getId() {
      return id;
   }

   /**
    * Access the id of the ResourceType this action relates to.
    *
    * @return
    */
   public String getTypeId() {
      return typeId;
   }

   /**
    * Access the enumerated constant for this action.
    *
    * @return
    */
   public ActionType getActionType() {
      return actionType;
   }

   /**
    * @return
    */
   public String getLabel() {
      // return null to let the resources tool handle the label for standard actions
      return null;
   }
}
