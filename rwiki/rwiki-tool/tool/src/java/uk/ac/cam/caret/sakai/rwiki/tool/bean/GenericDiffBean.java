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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.jrcs.diff.RevisionVisitor;
import org.apache.commons.jrcs.diff.myers.MyersDiff;

import uk.ac.cam.caret.sakai.rwiki.utils.XmlEscaper;

/**
 * Bean Helper class that for creating diffs. Line endings for the left and
 * right content are both normalized to Unix Line endings by the simple regular
 * expression replace "\r\n?" with "\n"
 * 
 * @version $Id: GenericDiffBean.java 7664 2006-04-12 15:27:23Z
 *          ian@caret.cam.ac.uk $
 * @author andrew
 */

public class GenericDiffBean
{

	/**
	 * <code>RevisionVisitor</code> that will generate rows for a diff table.
	 * Notes:
	 * <ul>
	 * <li>Each line in the revision is given a it's own row.</li>
	 * <li>The left and the right columns have the attribute width set at 50%</li>
	 * <li>If the line is unchanged, the left column will have the class
	 * unchangedLeft and the right: the class unchangedRight.</li>
	 * <li>If the line was changed, the left column will have the class:
	 * deletedLeft and the right will have: changedRight.</li>
	 * <li>If the line was deleted, the left column will have the class:
	 * deletedLeft and the right will have: deletedRight. In this case the right
	 * column will contain &amp;#160; a non-breaking space</li>
	 * <li>If the line was added, the left column will have the class:
	 * addedLeft and the right will have: addedRight. In this case the left
	 * column will contain &amp;#160; a non-breaking space</li>
	 * n
	 * </ul>
	 * 
	 * @author andrew
	 */
	public class ColorDiffTableRevisionVisitor implements RevisionVisitor
	{

		private StringBuffer sb = null;

		private int leftLineNum = 0, rightLineNum = 0;

		private boolean needToEnd = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.jrcs.diff.RevisionVisitor#visit(org.apache.commons.jrcs.diff.Revision)
		 */
		public final void visit(final Revision revision)
		{
			sb = new StringBuffer();
			leftLineNum = 0;
			rightLineNum = 0;
			needToEnd = true;
		}

		private void doRow(final int left, final int right,
				final String baseClass)
		{
			while (leftLineNum < left || rightLineNum < right)
			{
				sb.append("<tr><td width=\"50%\" class=\"");
				sb.append(baseClass);
				sb.append("Left\">");
				if (leftLineNum < left)
				{
					sb
							.append(XmlEscaper
									.xmlEscape((String) objectifiedLeft[leftLineNum++]));
				}
				else
				{
					sb.append("&#160;");
				}
				sb.append("</td><td width=\"50%\" class=\"");
				sb.append(baseClass);
				sb.append("Right\">");
				if (rightLineNum < right)
				{
					sb
							.append(XmlEscaper
									.xmlEscape((String) objectifiedRight[rightLineNum++]));
				}
				else
				{
					sb.append("&#160;");
				}
				sb.append("</td></tr>");
			}
		}

		public void visit(final DeleteDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();

			doRow(targetLeft.first(), targetRight.first(), "unchanged");
			doRow(targetLeft.last() + 1, targetRight.last() + 1, "deleted");

		}

		public void visit(final ChangeDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();

			doRow(targetLeft.first(), targetRight.first(), "unchanged");
			doRow(targetLeft.last() + 1, targetRight.last() + 1, "changed");

		}

		public void visit(final AddDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();

			doRow(targetLeft.first(), targetRight.first(), "unchanged");
			doRow(targetLeft.last() + 1, targetRight.last() + 1, "added");
		}

		/**
		 * Called once we have been visited by the revision and it's deltas. The
		 * visitor methods will have been used to generate rows for an XHTML
		 * table representation of the revision deltas.
		 * 
		 * @return string containing XHTML rows, each row should be XML
		 */
		public String getTableRows()
		{
			if (needToEnd)
			{
				doRow(objectifiedLeft.length, objectifiedRight.length,
						"unchanged");
				needToEnd = false;
			}

			return sb.toString();
		}
	}

	/**
	 * <code>RevisionVisitor</code> that will generate a listed diff view
	 * Notes:
	 * <ul>
	 * <li>Each line is given it's own div.</li>
	 * <li>A deleted line has the class deleted.</li>
	 * <li>An added line has the class added.</li>
	 * <li>An unchanged line has the class unchanged.</li>
	 * <li>A changed line is display as an div with class original followed by
	 * a div with class changed</li>
	 * </ul>
	 * 
	 * @author andrew
	 */
	public class ColorDiffRevisionVisitor implements RevisionVisitor
	{
		private StringBuffer sb = null;

		private int leftLineNum = 0, rightLineNum = 0;

		private boolean needToEnd = false;

		public void visit(final Revision revision)
		{
			sb = new StringBuffer();
			leftLineNum = 0;
			rightLineNum = 0;
			needToEnd = true;
		}

		private void appendLine(final Object line, final String cssclass)
		{
			sb.append("<div class=\"");
			sb.append(cssclass);
			sb.append("\">");
			sb.append(XmlEscaper.xmlEscape((String) line));
			sb.append("</div>");
		}

		private void doUnchanged(final int left, final int right)
		{
			if ((left - leftLineNum) != (right - rightLineNum))
			{
				throw new IllegalArgumentException(
						"Left and Right lines out of sync!");
			}
			while (leftLineNum < left)
			{
				appendLine(objectifiedLeft[leftLineNum++], "unchanged");
			}
			rightLineNum = right;
		}

		private void doSomeChange(final int left, final int right,
				final String original, final String change)
		{
			while (leftLineNum < left)
			{
				appendLine(objectifiedLeft[leftLineNum++], original);
			}
			while (rightLineNum < right)
			{
				appendLine(objectifiedRight[rightLineNum++], change);
			}
		}

		public void visit(final DeleteDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();
			doUnchanged(targetLeft.first(), targetRight.first());
			doSomeChange(targetLeft.last() + 1, targetRight.last() + 1,
					"deleted", "bug");
		}

		public void visit(final ChangeDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();
			doUnchanged(targetLeft.first(), targetRight.first());
			doSomeChange(targetLeft.last() + 1, targetRight.last() + 1,
					"original", "changed");
		}

		public void visit(final AddDelta delta)
		{
			Chunk targetLeft = delta.getOriginal();
			Chunk targetRight = delta.getRevised();
			doUnchanged(targetLeft.first(), targetRight.first());
			doSomeChange(targetLeft.last() + 1, targetRight.last() + 1, "bug",
					"added");
		}

		/**
		 * Called once we have been visited by the revision and it's deltas. The
		 * visitor methods will have been used to generate an XHTML
		 * representation of the revision deltas.
		 * 
		 * @return xhtml string
		 */
		public String getDiffString()
		{
			if (needToEnd)
			{
				doUnchanged(objectifiedLeft.length, objectifiedRight.length);
				needToEnd = false;
			}

			return sb.toString();
		}

	}

	/**
	 * The content of the left revision i.e. the old revision
	 */
	private String leftContent;

	/**
	 * The content of the right revision i.e. the new revision
	 */
	private String rightContent;

	/**
	 * A JRCS <code>Revision</code> representing the difference between the
	 * left and right content
	 */
	private Revision difference;

	/**
	 * an Object[] of the right content, each line as a separate object.
	 */
	private Object[] objectifiedRight;

	/**
	 * an Object[] of the left content, each line as a separate object.
	 */
	private Object[] objectifiedLeft;

	/**
	 * Create a <code>GenericDiffBean</code> for the given left and right
	 * content
	 * 
	 * @param leftContent
	 *        the left content i.e. the old content
	 * @param rightContent
	 *        the right content i.e. the new content
	 */
	public GenericDiffBean(String leftContent, String rightContent)
	{
		this.setLeftContent(leftContent);
		this.setRightContent(rightContent);
		this.init();
	}

	public GenericDiffBean()
	{
		// REQUIRED TO BE A BEAN!
	}

	/**
	 * initialise the bean. To be called after the leftContent and rightContent
	 * have been set.
	 */
	public void init()
	{
		MyersDiff diffAlgorithm = new MyersDiff();

		try
		{
			objectifiedLeft = leftContent.split("\n");

			objectifiedRight = rightContent.split("\n");

			difference = diffAlgorithm.diff(objectifiedLeft, objectifiedRight);

		}
		catch (DifferentiationFailedException e)
		{
			// Quite why JRCS has this I don't know!
			throw new RuntimeException(
					"DifferentiationFailedException occured: "
							+ "This should never happen!", e);
		}

	}

	/**
	 * Creates the rows for an XHTML table representing the differences between
	 * the left and right revision contents. This table is created using
	 * <code>ColorDiffTableRevisionVisitor</code>
	 * 
	 * @see ColorDiffTableRevisionVisitor
	 * @return String representing rows of an XHTML table
	 */
	public String getColorDiffTable()
	{
		ColorDiffTableRevisionVisitor rv = new ColorDiffTableRevisionVisitor();
		difference.accept(rv);

		return rv.getTableRows();
	}

	/**
	 * Creates a string containing XHTML representing the differences between
	 * the left and right revision contents. This method uses the
	 * <code>ColorDiffRevisionVisitor</code>
	 * 
	 * @see ColorDiffRevisionVisitor
	 * @return String representation of XHTML div
	 */
	public String getColorDiffString()
	{
		ColorDiffRevisionVisitor rv = new ColorDiffRevisionVisitor();
		difference.accept(rv);
		return rv.getDiffString();
	}

	/**
	 * Gets the difference between the left and right as a Unix Diff
	 * 
	 * @return String representing the unix diff
	 */
	public String getUnixDiffString()
	{
		return difference.toString();
	}

	/**
	 * Get the left content
	 * 
	 * @return the left content (with normalized line endings)
	 */
	public String getLeftContent()
	{
		return leftContent;
	}

	/**
	 * Sets the left content i.e. the old content.
	 * 
	 * @param leftContent
	 *        A non null string representing the left content
	 */
	public void setLeftContent(String leftContent)
	{
		this.leftContent = leftContent.replaceAll("\r\n?", "\n");
	}

	/**
	 * Gets the right content
	 * 
	 * @return the right content (with normalized line endings)
	 */
	public String getRightContent()
	{
		return rightContent;
	}

	/**
	 * Sets the right content i.e. the new content
	 * 
	 * @param rightContent
	 *        A non null string representing the right content
	 */
	public void setRightContent(String rightContent)
	{
		this.rightContent = rightContent.replaceAll("\r\n?", "\n");
	}

	/**
	 * Gets a JRCS <code>Revision</code> representing the difference between
	 * the left and right content
	 * 
	 * @return non-null <code>Revision</code> (if init has been called)
	 */
	public Revision getDifference()
	{
		return difference;
	}

}
