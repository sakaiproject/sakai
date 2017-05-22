/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.alias.tool;

// imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.courier.api.ObservingCourier;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.ResourceLoader;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * AliasesAction is the Sakai aliases editor.
 * </p>
 */
@Slf4j
public class AliasesAction extends PagedResourceActionII
{
    private static final long serialVersionUID = -5477742481219305334L;

    /**
	 * maximum chars allowed for alias setup 
	 */
	private static final int MAX_ALIAS_ID_LENGTH = 99;
	private static final int MAX_ALIAS_TARGET_LENGTH = 255;
	
	/**
	 * Populate the state object, if needed.
	 */
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("aliases");

	private AliasService aliasService;
	private SecurityService securityService;

	public AliasesAction() {
		aliasService = ComponentManager.get(AliasService.class);
		securityService = ComponentManager.get(SecurityService.class);
	}

	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

	} // initState

	/**
	 * Setup our observer to be watching for change events for our channel.
	 * 
	 * @param peid
	 *        The portlet id.
	 * @deprecated this is unused
	 */
	private void updateObservationOfChannel(SessionState state, String peid)
	{
		// EventObservingCourier observer = (EventObservingCourier) state.getAttribute(STATE_OBSERVER);
		//
		// // the delivery location for this tool
		// String deliveryId = clientWindowId(state, peid);
		// observer.setDeliveryId(deliveryId);

	} // updateObservationOfChannel

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String template = null;

		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		// put $action into context for menus, forms and links
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		
		context.put("max_alias_id_length", MAX_ALIAS_ID_LENGTH);
		
		context.put("max_alias_target_length", MAX_ALIAS_TARGET_LENGTH);

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");
		if (mode == null)
		{
			template = buildListContext(state, context);
		}
		else if (mode.equals("new"))
		{
			template = buildNewContext(state, context);
		}
		else if (mode.equals("edit"))
		{
			template = buildEditContext(state, context);
		}
		else if (mode.equals("confirm"))
		{
			template = buildConfirmRemoveContext(state, context);
		}
		else
		{
			log.warn("mode: {}", mode);
			template = buildListContext(state, context);
		}

		String prefix = (String) getContext(rundata).get("template");
		return prefix + template;

	} // buildNormalContext

	/**
	 * Build the context for the main list mode.
	 */
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context
		context.put("service", aliasService);

		// put all aliases into the context
		context.put("aliases", prepPage(state));

		// build the menu
		Menu bar = new MenuImpl();
		if (aliasService.allowAdd())
		{
			bar.add(new MenuEntry(rb.getString("alias.new"), null, true, MenuItem.CHECKED_NA, "doNew"));
		}

		// add the paging commands
		//addListPagingMenus(bar, state);
		int pageSize = Integer.valueOf(state.getAttribute(STATE_PAGESIZE).toString()).intValue();
		int currentPageNubmer = Integer.valueOf(state.getAttribute(STATE_CURRENT_PAGE).toString()).intValue();
		int startNumber = pageSize * (currentPageNubmer - 1) + 1;
		int endNumber = pageSize * currentPageNubmer;

		int totalNumber = 0;
		try
		{
			totalNumber = Integer.valueOf(state.getAttribute(STATE_NUM_MESSAGES).toString()).intValue();
		}
		catch (java.lang.NullPointerException ignore) {}
		catch (java.lang.NumberFormatException ignore) {}

		if (totalNumber < endNumber) endNumber = totalNumber;
		context.put("totalNumber", Integer.valueOf(totalNumber));
		context.put("numbers", new Integer[]{Integer.valueOf(startNumber), Integer.valueOf(endNumber), Integer.valueOf(totalNumber)});
		pagingInfoToContext(state, context);
		
		// add the search commands
		addSearchMenus(bar, state);

		// add the refresh commands
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		// for page size drop-down list
		ArrayList<Integer[]> list = new ArrayList<Integer[]>();
		String[] sizeArray = {"5", "10", "20", "50", "100", "200"};
		for (Iterator<String> iSize = (new ArrayList<String>(Arrays.asList(sizeArray))).iterator(); iSize.hasNext();)
		{
			list.add(new Integer[]{Integer.valueOf(iSize.next())});
		}
		context.put("sizeList", list);
		
		return "_list";

	} // buildListContext

	/**
	 * Build the context for the new alias mode.
	 */
	private String buildNewContext(SessionState state, Context context)
	{
		return "_edit";

	} // buildNewContext

	/**
	 * Build the context for the new alias mode.
	 */
	private String buildEditContext(SessionState state, Context context)
	{
		// name the html form for alias edit fields
		context.put("form-name", "alias-form");

		// get the alias to edit
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");
		context.put("alias", alias);

		// build the menu
		// we need the form fields for the remove...
		boolean menuPopulated = false;
		Menu bar = new MenuImpl();
		if (aliasService.allowRemoveAlias(alias.getId()))
		{
			bar.add(new MenuEntry(rb.getString("alias.remove"), null, true, MenuItem.CHECKED_NA, "doRemove", "alias-form"));
			menuPopulated = true;
		}

		if (menuPopulated)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		return "_edit";

	} // buildEditContext

	/**
	 * Build the context for the new alias mode.
	 */
	private String buildConfirmRemoveContext(SessionState state, Context context)
	{
		// get the alias to edit
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");
		context.put("alias", alias);

		return "_confirm_remove";

	} // buildConfirmRemoveContext

	/**
	 * doNew called when "eventSubmit_doNew" is in the request parameters to add a new alias
	 */
	public void doNew(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "new");

		// mark the alias as new, so on cancel it can be deleted
		state.setAttribute("new", "true");

		// disable auto-updates while not in list mode
		ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (courier != null) courier.disable();

	} // doNew

	/**
	 * doEdit called when "eventSubmit_doEdit" is in the request parameters to edit a alias
	 */
	public void doEdit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");

		// get the alias
		try
		{
			AliasEdit alias = aliasService.edit(id);
			state.setAttribute("alias", alias);
			state.setAttribute("mode", "edit");

			// disable auto-updates while not in list mode
			ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
			if (courier != null) courier.disable();
		}
		catch (IdUnusedException e)
		{
			log.warn("alias not found: {}", id);

			addAlert(state, rb.getFormattedMessage("alias.notfound", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
			if (courier != null) courier.enable();
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("alias.notpermis", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
			if (courier != null) courier.enable();
		}
		catch (InUseException e)
		{
			addAlert(state, rb.getFormattedMessage("alias.someone", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
			if (courier != null) courier.enable();
		}

	} // doEdit

	/**
	 * doSave called when "eventSubmit_doSave" is in the request parameters to save alias edits
	 */
	public void doSave(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		// read the form - if rejected, leave things as they are
		if (!readAliasForm(data, state)) return;

		// commit the change
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");
		if (alias != null)
		{
			aliasService.commit(alias);
		}

		// cleanup
		state.removeAttribute("alias");
		state.removeAttribute("new");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (courier != null) courier.enable();

	} // doSave

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters to cancel alias edits
	 */
	public void doCancel(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the alias
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");
		if (alias != null)
		{
			// if this was a new, delete the alias
			if ("true".equals(state.getAttribute("new")))
			{
				// remove
				try
				{
					aliasService.remove(alias);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("alias.notpermis1", new Object[]{alias.getId()}));
				}
			}
			else
			{
				aliasService.cancel(alias);
			}
		}

		// cleanup
		state.removeAttribute("alias");
		state.removeAttribute("new");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (courier != null) courier.enable();

	} // doCancel

	/**
	 * doRemove called when "eventSubmit_doRemove" is in the request parameters to confirm removal of the alias
	 */
	public void doRemove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readAliasForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the alias
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the alias
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");

		// remove
		try
		{
			aliasService.remove(alias);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("alias.notpermis1", new Object[]{alias.getId()}));
		}

		// cleanup
		state.removeAttribute("alias");
		state.removeAttribute("new");

		// go to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (courier != null) courier.enable();

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel alias removal
	 */
	public void doCancel_remove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// return to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_remove

	/**
	 * Read the alias form and update the alias in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readAliasForm(RunData data, SessionState state)
	{
		// read the form
		String id = StringUtils.trimToNull(data.getParameters().getString("id"));
		String target = StringUtils.trimToNull(data.getParameters().getString("target"));
		
		// get the alias
		AliasEdit alias = (AliasEdit) state.getAttribute("alias");

		// add if needed
		if (alias == null)
		{
			try
			{
				// add the alias, getting an edit clone to it
				alias = aliasService.add(id);

				// put the alias in the state
				state.setAttribute("alias", alias);
			}
			catch (IdUsedException e)
			{
				addAlert(state, rb.getFormattedMessage("alias.use", new Object[]{id}));
				return false;
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getFormattedMessage("alias.invalid", new Object[]{id}));
				return false;
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("alias.notpermis2", new Object[]{id}));
				return false;
			}
		}

		// update
		if (alias != null)
		{
			alias.setTarget(target);
		}

		return true;

	} // readAliasForm

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return aliasService.searchAliases(search, first, last);
		}

		return aliasService.getAliases(first, last);
	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return aliasService.countSearchAliases(search);
		}

		return aliasService.countAliases();
	}

} // AliasesAction
