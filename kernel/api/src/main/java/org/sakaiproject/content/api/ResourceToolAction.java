/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;


/**
 * ResourceToolAction defines the way in which actions are described in a resource-type registration. 
 * Each action should have its own ResourceToolAction defined in the registration.  
 * If the action requires user interaction by a helper, the resource-type registration should include
 * an action definition that implements the InteractionAction interface.  
 * If an action requires action by another webapp but does not involve user interaction (i.e. it does
 * not delegate the user interaction to a helper), the resource-type registration should include an
 * action definition that implements the ServiceLevelAction interface.
 * Most actions for new resources types are likely to be similar to familiar actions on resources (e.g. 
 * "create", "revise", "delete", etc) and permissions for these actions are handled by the Content Hosting
 * Service.  If an action requires custom permissions, the definition of that action implements the
 * CustomToolAction interface to provide a way for the Resources tool to determine whether to show 
 * the action as an option in a particular context to a particular user.
 * A ResourceToolAction deinition should implement at least one of those subinterfaces, and it may
 * implement all three.  If a ResourceToolAction implements both InteractionAction and ServiceLevelAction,
 * the activity of the helper defined by InteractionAction will occur before the service-level activity 
 * defined by ServiceLevelAction.
 * 
 * @see org.sakaiproject.content.api.ResourceType
 */
public interface ResourceToolAction 
{
	/**
	 * ActionType defines identifiers for types of actions related to resources.
	 */
	public enum ActionType
	{
		/**
		 * Create upload -- Handled by Resources tool.  Can create multiple uploads at once.  
		 * 		Metadata and content supplied in same form.  Requires content.new permission 
		 * 		in parent folder.
		 */
		NEW_UPLOAD,
		
		/**
		 * Create folder -- Handled by Resources tool.  Can create multiple folders at once.  
		 * 		No content; requires metadata only.  Requires content.new permission in parent 
		 * 		folder.
		 */
		NEW_FOLDER,
		
		/**
		 * Create URLs -- Handled by Resources tool.  Can create multiple URLs at once.  
		 * 		No content; requires metadata only.  Requires content.new permission in parent 
		 * 		folder.
		 */
		NEW_URLS,
		
		/**
		 * Create other -- Handled by helper and Resources tool.  Can create one item at a time.  
		 * 		Content (and possibly some properties) handled by helper. Metadata supplied in 
		 * 		form that appears after helper finishes.  Requires content.new permission in 
		 * 		parent folder.
		 */
		CREATE,
		
		/**
		 * Create other -- Handled entirely by helper.  Can create one or more items.  
		 * 		Content and basic properties handled by helper. Resources metadata form 
		 * 		is bypassed appears after helper finishes.  Requires content.new permission in 
		 * 		parent folder.
		 */
		CREATE_BY_HELPER,
		
		/**
		 * Delete -- Handled by Resources tool.  Requires content.delete permission
		 */
		DELETE,
		
		/**
		 * Revise content -- Handled by helper.  Requires content.revise.any permission (or 
		 * 		content.revise.own if user is creator).
		 */
		REVISE_CONTENT,
		
		/**
		 * Replace content -- Handled by Resources tool.  Requires content.revise.any permission 
		 * 		(or content.revise.own if user is creator).
		 */
		REPLACE_CONTENT,
		
		/**
		 * Restore the content of the folder.    
		 */
		RESTORE,
		
		/**
		 * Revise metadata -- Handled by Resources tool.  Requires content.revise.any permission 
		 * 		(or content.revise.own if user is creator).
		 */
		REVISE_METADATA,
		
		/**
		 * Copy -- Handled by Resources tool.  Requires content.read permission for item being 
		 * 		copied and content.new permission in folder to which it's copied.
		 */
		COPY,
		
		/**
		 * Move -- Handled by Resources tool.  Requires content.delete (or content.update ?) permission for 
		 * 		item being moved and content.new permission in folder to which it's moved.
		 */
		MOVE,
		
		/**
		 * Duplicate -- Handled by Resources tool.  Requires content.read permission for item being 
		 * 		duplicated and content.new permission in parent folder.
		 */
		DUPLICATE,
		
		/**
		 * View content -- Handled by AccessServlet (via Resources tool) or helper.  Requires 
		 * 		content.read permission.
		 */
		VIEW_CONTENT,
		
		/**
		 * View metadata -- Handled by Resources tool.  Requires content.read permission.
		 */
		VIEW_METADATA,
		
		/**
		 * Paste Moved Items -- Handled by Resources tool.  Requires content.new permission.  The action 
		 * 		is only available after a MOVE action and before some other action that
		 * 		cancels the PASTE_MOVED.
		 */
		PASTE_MOVED,
		
		/**
		 * Paste Copied Items -- Handled by Resources tool.  Requires content.new permission.  The action 
		 * 		is only available after a COPY action and before some other action that
		 * 		cancels the PASTE_COPIED.
		 */
		PASTE_COPIED,
		
		/**
		 * Defines a custom sort order for the contents of a folder -- Handled by Resources tool.  Requires 
		 * 		content.revise permission for the folder.    
		 */
		REVISE_ORDER,
		
		/**
		 * Revise folder permissions -- Handled by Resources tool.  Requires site.upd permission.  
		 */
		REVISE_PERMISSIONS,
		
		/**
		 * Expand a folder to show its members -- Handled by Resources tool.  Available to anybody with 
		 * content.read permission for the contents of the folder. 
		 */
		EXPAND_FOLDER,
		
		/**
		 * Collapse a folder to hide its members -- Handled by Resources tool.  No permission checks. 
		 */
		COLLAPSE_FOLDER,

		/**
		 * Custom action -- Handled by helper.  May be interactive or service-level.  Custom actions
		 * 		must implement the CustomToolAction interface to provide Resources tool with a way to 
		 * 		determine permissions, as well as either InteractionAction or ServiceLevelAction.
		 */
		CUSTOM_TOOL_ACTION,
 		
		/**
		 * Compress a selected folder to a zip archive with the same name.
		 */
		COMPRESS_ZIP_FOLDER,
				
		/**
		 * Expands a zip file into serveral folders and archives 
		 */
		EXPAND_ZIP_ARCHIVE,

		/**
		 * Create a page in the site linking to the content.
		 */
		MAKE_SITE_PAGE,
		
		/**
		 * Print a file
		 */
		PRINT_FILE
	}
	
	public static final String CREATE = "create";
	public static final String DELETE = "delete";
	public static final String COPY = "copy";
	public static final String REVISE_CONTENT = "revise";
	public static final String REPLACE_CONTENT = "replace";
	public static final String REVISE_METADATA = "properties";
	public static final String ACCESS_CONTENT = "access";
	public static final String ACCESS_PROPERTIES = "info";
	public static final String DUPLICATE = "duplicate";
	public static final String MOVE = "move";
	public static final String PASTE_MOVED = "paste_moved";
	public static final String PASTE_COPIED = "paste_copied";
	public static final String PERMISSIONS = "revise_permissions";
	public static final String REORDER = "revise_order";
	public static final String EXPAND = "expand";
	public static final String COLLAPSE = "collapse";
	public static final String RESTORE = "restore";
	public static final String COMPRESS_ZIP_FOLDER = "compress_zip_folder";
	public static final String EXPAND_ZIP_ARCHIVE = "expand_zip_archive";
	public static final String MAKE_SITE_PAGE = "make_site_page";
	public static final String SHOW = "show";
	public static final String HIDE = "hide";
	public static final String COPY_OTHER = "copy-other";
	public static final String PRINT_FILE = "print file";
	public static final String ACTION_DELIMITER = ":";
	public static final String ZIPDOWNLOAD = "zipDownload";
		
	/**
	 * Prefix for all keys to Tool Session attributes used in messaging between ResourcesAction and
	 * helpers related to registered resource tool actions. Removing all attributes whose keys begin
	 * with this prefix cleans up the tool session after a helper completes its work. 
	 * ResourcesAction will cleanup tool session.
	 */
	public static final String PREFIX = "resourceToolAction.";
	
	public static final String ACTION_PIPE = PREFIX + "action_pipe";
	
	public static final String STARTED = PREFIX + "started";
	
	public static final String STATE_MODE = PREFIX + "state_mode";
	
	public static final String DONE = PREFIX + "done";

	/**
	 * Access the id of this action (which must be unique within this type and must be limited to alphnumeric characters).
	 * @return
	 */
	public String getId();
	
	/**
	 * Access the id of the ResourceType this action relates to.
	 * @return
	 */
	public String getTypeId();
	
	/**
	 * Access the enumerated constant for this action.
	 * @return
	 */
	public ActionType getActionType();
	
	/**
	 * Access a very short localized string that will be used as a label for this action in the user interface.  
	 * If string is longer than about 20 characters, it may be truncated. Shorter strings (less than 10 characters)
	 * are preferred.  If this method returns null, the Resources tool will assign a label based on the ActionType. 
	 * @return
	 */
	public String getLabel();
	
	/**
	 * Should the resources tool make this action available to the current user with respect to the specified entity?
	 * @param entity
	 * @return
	 */
	public boolean available(ContentEntity entity);
	
}
