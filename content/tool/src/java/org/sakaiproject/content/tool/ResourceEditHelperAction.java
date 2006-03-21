/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.service.framework.session.SessionState;
import org.sakaiproject.service.legacy.filepicker.ResourceEditingHelper;

public class ResourceEditHelperAction extends FilePickerAction {

   protected String initHelperAction(VelocityPortlet portlet, Context context, RunData rundata,
                                     SessionState sstate, ToolSession toolSession) {
      Object createType = toolSession.getAttribute(ResourceEditingHelper.CREATE_TYPE);
      sstate.setAttribute(ResourcesAction.STATE_ATTACH_LINKS, Boolean.TRUE.toString());
      sstate.setAttribute(ResourcesAction.STATE_MODE, ResourcesAction.MODE_HELPER);
      if (toolSession.getAttribute(ResourceEditingHelper.ATTACHMENT_ID) != null) {
         sstate.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_EDIT_ITEM);
         sstate.setAttribute(ResourcesAction.STATE_EDIT_ID,
               toolSession.getAttribute(ResourceEditingHelper.ATTACHMENT_ID));
         sstate.setAttribute(ResourcesAction.STATE_STRUCTOBJ_TYPE_READONLY, Boolean.TRUE.toString());
         if (ResourceEditingHelper.CREATE_TYPE_FORM.equals(createType)) {
            sstate.setAttribute(ResourcesAction.STATE_CREATE_TYPE, ResourcesAction.TYPE_FORM);
         }
      }
      else {
         // must be create
         sstate.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_NEW_ITEM);

         if (ResourceEditingHelper.CREATE_TYPE_FORM.equals(createType)) {
            sstate.setAttribute(ResourcesAction.STATE_CREATE_TYPE,
                  ResourcesAction.TYPE_FORM);
            Object createSubType = toolSession.getAttribute(ResourceEditingHelper.CREATE_SUB_TYPE);
            if (createSubType != null) {
               sstate.setAttribute(ResourcesAction.STATE_STRUCTOBJ_TYPE,
                     createSubType);
               sstate.setAttribute(ResourcesAction.STATE_STRUCTOBJ_TYPE_READONLY, Boolean.TRUE.toString());
            }
         }

         sstate.setAttribute(ResourcesAction.STATE_CREATE_NUMBER, new Integer(1));
         sstate.setAttribute(ResourcesAction.STATE_CREATE_COLLECTION_ID,
               toolSession.getAttribute(ResourceEditingHelper.CREATE_PARENT));
      }

      initMessage(toolSession, sstate);
      return ResourcesAction.MODE_HELPER;
   }

   protected void cleanup(SessionState sstate) {
      super.cleanup(sstate);
      sstate.removeAttribute(ResourcesAction.STATE_ATTACH_ITEM_ID);
      sstate.removeAttribute(ResourcesAction.STATE_STRUCTOBJ_TYPE_READONLY);
      sstate.removeAttribute(ResourcesAction.STATE_CREATE_TYPE);
      sstate.removeAttribute(ResourcesAction.STATE_STRUCTOBJ_TYPE);
      sstate.removeAttribute(ResourcesAction.STATE_CREATE_NUMBER);
      sstate.removeAttribute(ResourcesAction.STATE_CREATE_COLLECTION_ID);
      sstate.removeAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
      sstate.removeAttribute(ResourcesAction.STATE_SHOW_OTHER_SITES);
      sstate.removeAttribute(ResourcesAction.STATE_EDIT_ID);
      sstate.removeAttribute(ResourcesAction.STATE_EDIT_COLLECTION_ID);
      sstate.removeAttribute(ResourcesAction.STATE_ATTACH_LINKS);
      sstate.removeAttribute(ResourcesAction.STATE_ATTACH_ITEM_ID);
      //sstate.removeAttribute(ResourcesAction.STATE_STRUCTOBJ_HOMES);
      //sstate.removeAttribute(ResourcesAction.STATE_EDIT_ITEM);
      //sstate.removeAttribute(ResourcesAction.STATE_SHOW_FORM_ITEMS);
      //sstate.removeAttribute(ResourcesAction.STATE_STRUCT_OBJ_SCHEMA);
      //sstate.removeAttribute(ResourcesAction.STATE_STRUCTOBJ_ROOTNAME);
      //sstate.removeAttribute(ResourcesAction.STATE_EDIT_ALERTS);
   }
}
