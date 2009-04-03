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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.archive.tool;

import java.util.Enumeration;
import java.util.Hashtable;

import org.sakaiproject.archive.cover.ArchiveService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
* <p>ArchiveAction is the Sakai archive tool.</p>
*/
public class ArchiveAction
	extends VelocityPortletPaneledAction
{
	private static final String STATE_MODE = "mode";
	private static final String BATCH_MODE = "batch";
	
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("admin");
   
	/**
	* build the context
	*/
    public String buildMainPanelContext(VelocityPortlet portlet, 
			Context context,
			RunData rundata,
			SessionState state)
	{
		String template = null;

		// if not logged in as the super user, we won't do anything
		if (!SecurityService.isSuperUser())
		{
			context.put("tlang",rb);
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		// check mode and dispatch
		String mode = (String) state.getAttribute(STATE_MODE);
		if (mode == null)
		{
			template = buildListPanelContext(portlet, context, rundata, state);
		}
		else if (mode.equals(BATCH_MODE))
		{
			template = buildBatchPanelContext(portlet, context, rundata, state);
		}
		
		return (String)getContext(rundata).get("template") + template;
		
	}	// buildMainPanelContext
    
    /**
	* build the context for non-batch import/export
	*/
	public String buildListPanelContext(VelocityPortlet portlet, 
										Context context,
										RunData rundata,
										SessionState state)
	{
		context.put("tlang",rb);
		
		// build the menu
		Menu bar = new MenuImpl();
		bar.add( new MenuEntry(rb.getString("archive.button.batch"), "doToggle_State") );
		context.put(Menu.CONTEXT_MENU, bar);
		context.put (Menu.CONTEXT_ACTION, "ArchiveAction");
		
		return "";

	}	// buildListPanelContext

	/**
	* build the context for batch import/export
	*/
	public String buildBatchPanelContext(VelocityPortlet portlet, 
										Context context,
										RunData rundata,
										SessionState state)
	{
		context.put("tlang",rb);
		
		//build the menu
		Menu bar = new MenuImpl();
		bar.add( new MenuEntry(rb.getString("archive.button.nonbatch"), "doToggle_State") );
		context.put(Menu.CONTEXT_MENU, bar);
		context.put (Menu.CONTEXT_ACTION, "ArchiveAction");
		
		return "-batch";

	}	// buildListPanelContext
	
	/**
	 * 
	 */
	public void doToggle_State(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		if (state.getAttribute(STATE_MODE) == null)
		{
			state.setAttribute(STATE_MODE, BATCH_MODE);
		}
		else
		{
			state.removeAttribute(STATE_MODE);
		}
		
	}	// doToggle_State
	
	/**
	* doArchive called when "eventSubmit_doArchive" is in the request parameters
	* to run the archive.
	*/
	public void doArchive(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!SecurityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.limited"));
			return;
		}

		String id = data.getParameters().getString("archive-id");
		if ((id != null) && (id.trim().length() > 0))
		{
			String msg = ArchiveService.archive(id.trim());
			addAlert(state, rb.getString("archive.site") + " " + id + " " + rb.getString("archive.complete") + " " + msg);
		}
		else
		{
			addAlert(state, rb.getString("archive.please"));
		}

	}	// doArchive

	/**
	* doImport called when "eventSubmit_doImport" is in the request parameters
	* to run an import.
	*/
	public void doImport(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!SecurityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.import"));
			return;
		}

		String id = data.getParameters().getString("import-id");
		String file = data.getParameters().getString("import-file");
		if (	(id != null) && (id.trim().length() > 0)
			&&	(file != null) && (file.trim().length() > 0))
		{
			String msg = ArchiveService.merge(file.trim(), id.trim(), null);
			addAlert(state, rb.getString("archive.import1")  + " " + file + " " + rb.getString("archive.site") + " "
					+ id + " " + rb.getString("archive.complete") + " " + msg);
		}
		else
		{
			addAlert(state, rb.getString("archive.file"));
		}

	}	// doImport
	
	/**
	* doImport called when "eventSubmit_doImport" is in the request parameters
	* to run an import.
	*/
	public void doBatch_Import(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		Hashtable fTable = new Hashtable();
		
		if (!SecurityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.batch.auth"));
			return;
		}
		
		//String fileName = data.getParameters().getString("import-file");
		FileItem fi = data.getParameters().getFileItem ("importFile");
		if (fi == null)
		{
			addAlert(state, rb.getString("archive.batch.missingname"));
		}
		else
		{
			// get content
			String content = fi.getString();
			
			String[] lines = content.split("\n");
			for(int i=0; i<lines.length; i++)
			{
				String lineContent = (String) lines[i];
				String[] lineContents = lineContent.split("\t");
				if (lineContents.length == 2)
				{
					fTable.put(lineContents[0], lineContents[1]);
				}
				else
				{
					addAlert(state, rb.getString("archive.batch.wrongformat"));
				}
			}
		}
		
		if (!fTable.isEmpty())
		{
			Enumeration importFileName = fTable.keys();
			int count = 1;
			while (importFileName.hasMoreElements())
			{
				String path = StringUtil.trimToNull((String) importFileName.nextElement());
				String siteCreatorName = StringUtil.trimToNull((String) fTable.get(path));
				if (path != null && siteCreatorName != null)
				{
					String nSiteId = IdManager.createUuid();
					
					try
					{
						// merge
						addAlert(state, "\n" + rb.getString("archive.import1") + " " + count + ": " + rb.getString("archive.import2") + " "+ path + " " + rb.getString("archive.site") + " "
								+ nSiteId + " " + rb.getString("archive.importcreatorid") + " " + siteCreatorName + " " + rb.getString("archive.complete") + "\n");
						addAlert(state, ArchiveService.merge(path, nSiteId, siteCreatorName));
						
					}
					catch (Exception ignore)
					{
					}
				}
				
				count++;
			}
		}
	}	// doBatchImport

}	// ArchiveAction



