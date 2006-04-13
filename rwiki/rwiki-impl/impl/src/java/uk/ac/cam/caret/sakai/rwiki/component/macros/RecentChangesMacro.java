/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;
import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper;

/**
 * FIXME needs localisation
 * 
 * @author andrew
 */
// FIXME: Component
public class RecentChangesMacro extends BaseMacro
{

	private static String[] paramDescription = { "1: Optional, If format is yyyy-MM-dd Changes since date. If format is 30d number of days, If format is 12h, number of hours, defaults to last 30 days, " };

	private static String description = "Expands to a list of recently changed pages";

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return "recent-changes";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		DateFormat dateFormat = DateFormat.getDateTimeInstance();

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

			String dateAsString = params.get("date", 0);
			if (dateAsString != null)
			{
				if (dateAsString.trim().endsWith("h"))
				{
					int nHours = Integer.parseInt(dateAsString.trim()
							.substring(0, dateAsString.trim().length() - 1));
					cal = new GregorianCalendar();
					cal.add(GregorianCalendar.HOUR, -nHours);
					since = cal.getTime();
				}
				else if (dateAsString.trim().endsWith("d"))
				{
					int nDays = Integer.parseInt(dateAsString.trim().substring(
							0, dateAsString.trim().length() - 1));
					cal = new GregorianCalendar();
					cal.add(GregorianCalendar.DATE, -nDays);
					since = cal.getTime();
				}
				else
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

					try
					{
						since = format.parse(dateAsString);
					}
					catch (ParseException e)
					{
						writer
								.write("<span class=\"error\"> Cannot parse: "
										+ dateAsString
										+ " must be of the format: yyyy-MM-dd or 30d or 12h Will assume past 30 days </span>");
					}
				}
			}
		}
		writer.write("<span class=\"error\"> Changes since "
				+ dateFormat.format(since) + "  </span>");

		try
		{

			List wikiObjects = objectService.findChangedSince(since, realm);

			writer.write("<div class=\"list\">");

			Iterator iterator = wikiObjects.iterator();
			while (iterator.hasNext())
			{
				RWikiObject object = (RWikiObject) iterator.next();
				if (objectService.checkRead(object))
				{
					// SAK-2671 We should localize against the renderspace not
					// the object's realm!
					writer.write("\n* ["
							+ NameHelper.localizeName(object.getName(),
									localRenderSpace) + "]");

					writer.write(" was last modified "
							+ dateFormat.format(object.getVersion()));
					writer.write(" by "
							+ UserDisplayHelper.formatDisplayName(object
									.getUser()));
				}

			}

			// SAK-2696
			writer.write("\n</div>");
		}
		catch (PermissionException e)
		{
			writer.write("You do not have permission to search.");
			writer.write(e.toString());
			e.printStackTrace(new PrintWriter(writer));

		}

		return;
	}

}

/*******************************************************************************
 * $Header$
 ******************************************************************************/

