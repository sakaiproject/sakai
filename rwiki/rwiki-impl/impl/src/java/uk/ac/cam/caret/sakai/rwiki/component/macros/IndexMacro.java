/**
 * 
 */
package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;
import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author andrew
 */
public class IndexMacro extends BaseMacro
{

	private static final String description = "";

	private static final String[] paramDescription = {
			"1:space, The space which to index, defaults to the space of the current page.",
			"2:nohead, if nohead, the header line will not be produced" };

	public String getDescription()
	{
		return description;
	}

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.BaseMacro#getName()
	 */
	public String getName()
	{
		return "index";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.BaseMacro#execute(java.io.Writer,
	 *      org.radeox.macro.parameter.MacroParameter)
	 */
	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		SpecializedRenderEngine spRe = (SpecializedRenderEngine) context
				.getRenderEngine();

		PageLinkRenderer plr = spRe.getPageLinkRenderer();

		plr.setCachable(false);

		String space = params.get("space", 0);
		if (space == null || "".equals(space.trim()))
		{
			space = spRe.getSpace();
		}
		boolean nohead = "nohead".equals(params.get("nohead", 1));

		RWikiObjectService objectService = context.getObjectService();

		// This may be very inefficient as we don't necessarily want the objects
		List subpages = objectService.findRWikiSubPages(space
				+ NameHelper.SPACE_SEPARATOR);
		Iterator it = subpages.iterator();

		writer.write("<div class=\"index-list\">\n");
		if (!nohead)
		{
			writer.write("  <p>Index of ");
			writer.write(space);
			writer.write("</p>\n");
		}
		{

			String currentSpace = space;
			char[] currentNameChars = currentSpace.toCharArray();
			int currentSpaceIndex = currentNameChars.length;
			writer.write("  <ul class=\"tree\">\n");
			while (it.hasNext())
			{
				RWikiObject next = (RWikiObject) it.next();
				String nextName = next.getName();
				char[] nextNameChars = nextName.toCharArray();
				int nextSpaceIndex = nextName
						.lastIndexOf(NameHelper.SPACE_SEPARATOR);

				/*
				 * OK next's space is either: i) same space as the currentSpace,
				 * ii) some subspace from the currentSpace, or iii) some other
				 * part of the tree.
				 */

				boolean isNextLonger = nextSpaceIndex > currentSpaceIndex;
				char[] longerSpace = isNextLonger ? nextNameChars
						: currentNameChars;
				int longerSpaceLength = isNextLonger ? nextSpaceIndex
						: currentSpaceIndex;
				int shortSpaceLength = !isNextLonger ? nextSpaceIndex
						: currentSpaceIndex;

				int index = 0;
				// lastSep should always be set to 0 at least (since spaces are
				// normal)
				int lastSep = -1;
				while (index < shortSpaceLength
						&& nextNameChars[index] == currentNameChars[index])
				{
					if (longerSpace[index] == NameHelper.SPACE_SEPARATOR)
					{
						lastSep = index;
					}
					index++;
				}

				if (index == shortSpaceLength && index == longerSpaceLength)
				{
					// spaces are the same! No space change required!
				}
				else if (index == shortSpaceLength
						&& longerSpace[index] == NameHelper.SPACE_SEPARATOR)
				{
					// Shorter space is a prefix of the longer space
					// we have a change up/down one way
					emitSpaceChange(writer, longerSpace, index,
							longerSpaceLength, isNextLonger);
				}
				else
				{
					// We have a change of space which is both up and down
					// Go down from the current space to the lastSep
					emitSpaceChange(writer, currentNameChars, lastSep,
							currentSpaceIndex, false);
					// Then go up to the next space
					emitSpaceChange(writer, nextNameChars, lastSep,
							nextSpaceIndex, true);
				}

				emitListItem(writer, plr, nextNameChars, nextSpaceIndex + 1);

				currentNameChars = nextNameChars;
				currentSpaceIndex = nextSpaceIndex;
			}
			// now check whether we have to go down to the original space
			if (currentSpaceIndex > space.length())
			{
				emitSpaceChange(writer, currentNameChars, space.length(),
						currentSpaceIndex, false);
			}
			writer.write("  </ul>\n");
		}

		writer.write("</div>\n");

	}

	private void emitSpaceChange(Writer writer, char[] chars, int index,
			int end, boolean up) throws IOException
	{
		// indexes are at most length left div 2 (we don't even care
		// about +1 at the end as it can't be!)

		int[] sepIndexes = new int[((end - index) / 2) + 1];
		int spaces = 0;
		sepIndexes[spaces++] = index;

		while (++index < end)
		{
			if (chars[index] == NameHelper.SPACE_SEPARATOR)
			{
				sepIndexes[spaces++] = index;
			}
		}
		sepIndexes[spaces] = end;

		if (up)
		{
			for (int i = 0; i < spaces; i++)
			{
				emitGoUp(writer, chars, sepIndexes[i] + 1, sepIndexes[i + 1]);
			}
		}
		else
		{
			for (int i = spaces; i > 0; i--)
			{
				emitGoDown(writer, chars, sepIndexes[i - 1] + 1, sepIndexes[i]);
			}
		}

	}

	private void emitGoUp(Writer writer, char[] chars, int start, int end)
			throws IOException
	{
		// TODO emit up
		writer.write("<li><a href=\"#\" class=\"subspace-link\">");
		writer.write(chars, start, end - start + 1);
		writer.write("</a>\n<ul>");
	}

	private void emitGoDown(Writer writer, char[] chars, int start, int end)
			throws IOException
	{
		// TODO emit down
		writer.write("</ul>\n</li>");
	}

	private void emitListItem(Writer writer, PageLinkRenderer plr,
			char[] chars, int start) throws IOException
	{

		writer.write("<li>");
		StringBuffer sb = new StringBuffer(chars.length * 3);
		plr.appendLink(sb, new String(chars), new String(chars, start,
				chars.length - start));
		writer.write(sb.toString());
		writer.write("</li>\n");
	}

}
