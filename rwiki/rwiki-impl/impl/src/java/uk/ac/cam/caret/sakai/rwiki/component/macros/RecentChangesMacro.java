/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;
import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
/**
 * 
 * 
 * @author andrew
 */
// FIXME: Component
@Slf4j
public class RecentChangesMacro extends BaseMacro
{

	public String[] getParamDescription()
	{
		return new String[] { Messages.getString("RecentChangesMacro.0") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("RecentChangesMacro.1"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "recent-changes"; //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, new ResourceLoader().getLocale());		
		dateFormat.setTimeZone(TimeService.getLocalTimeZone());
		

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();

		RWikiObjectService objectService = context.getObjectService();

		String realm = context.getRWikiObject().getRealm();

		SpecializedRenderEngine spRe = (SpecializedRenderEngine) context
				.getRenderEngine();

		PageLinkRenderer plr = spRe.getPageLinkRenderer();
		plr.setCachable(false);
		// SAK-2671 We need to know the local render space
		String localRenderSpace = spRe.getSpace();

		GregorianCalendar cal = new GregorianCalendar();
		cal.add(GregorianCalendar.DATE, -30);
		Date since = cal.getTime();

		// check for single url argument (text == url)
		if (params.getLength() == 1)
		{

			String dateAsString = params.get("date", 0); //$NON-NLS-1$
			if (dateAsString != null)
			{
				if (dateAsString.trim().endsWith("h")) //$NON-NLS-1$
				{
					int nHours = Integer.parseInt(dateAsString.trim()
							.substring(0, dateAsString.trim().length() - 1));
					cal = new GregorianCalendar();
					cal.add(GregorianCalendar.HOUR, -nHours);
					since = cal.getTime();
				}
				else if (dateAsString.trim().endsWith("d")) //$NON-NLS-1$
				{
					int nDays = Integer.parseInt(dateAsString.trim().substring(
							0, dateAsString.trim().length() - 1));
					cal = new GregorianCalendar();
					cal.add(GregorianCalendar.DATE, -nDays);
					since = cal.getTime();
				}
				else
				{
					SimpleDateFormat format = new SimpleDateFormat(Messages.getString("RecentChangesMacro.6"), new ResourceLoader().getLocale()); //$NON-NLS-1$
					format.setTimeZone(TimeService.getLocalTimeZone());

					try
					{
						since = format.parse(dateAsString);
					}
					catch (ParseException e)
					{
						writer
								.write(Messages.getString("RecentChangesMacro.7") //$NON-NLS-1$
										+ dateAsString
										+ Messages.getString("RecentChangesMacro.8")); //$NON-NLS-1$
					}
				}
			}
		}
		writer.write(Messages.getString("RecentChangesMacro.9") //$NON-NLS-1$
				+ dateFormat.format(since) + "  </span>"); //$NON-NLS-1$

		try
		{

			List wikiObjects = objectService.findChangedSince(since, realm);

			writer.write("<div class=\"list\">"); //$NON-NLS-1$

			Iterator iterator = wikiObjects.iterator();
			while (iterator.hasNext())
			{
				RWikiObject object = (RWikiObject) iterator.next();
				if (objectService.checkRead(object))
				{
					// SAK-2671 We should localize against the renderspace not
					// the object's realm!
					String linkName = NameHelper.localizeName(object.getName(),
							localRenderSpace); 
					
					StringBuffer buffer = new StringBuffer();
					
					spRe.appendLink(buffer, linkName, linkName, null, true);
					
					writer.write("\n* "); //$NON-NLS-1$
					writer.write(buffer.toString());

					writer.write(Messages.getString("RecentChangesMacro.13") //$NON-NLS-1$
							+ dateFormat.format(object.getVersion()));
					writer.write(Messages.getString("RecentChangesMacro.14") //$NON-NLS-1$
							+ UserDisplayHelper.formatDisplayName(object
									.getUser(), object.getRealm()));
				}

			}

			// SAK-2696
			writer.write("\n</div>"); //$NON-NLS-1$
		}
		catch (PermissionException e)
		{
			writer.write(Messages.getString("RecentChangesMacro.16")); //$NON-NLS-1$
			writer.write(e.toString());
			log.error(e.getMessage(), e);
		}

		return;
	}
	
}

/*******************************************************************************
 * $Header$
 ******************************************************************************/

