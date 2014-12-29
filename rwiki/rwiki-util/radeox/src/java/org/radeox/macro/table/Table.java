/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.macro.table;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.radeox.macro.Repository;

/**
 * A Table implementation primarly for the table macro
 * 
 * @author stephan
 * @version $Id$
 */

public class Table
{
	// current number of rows
	private int indexRow = 0;

	// current number of cols
	private int indexCol = 0;

	private List rows;

	private List currentRow;

	private String currentCell;

	private List functionOccurences;

	private Repository functions;

	private HashMap tableAttributes = new HashMap();

	public Table()
	{
		rows = new ArrayList(10);
		currentRow = new ArrayList(10);
		currentCell = "";
		functions = FunctionRepository.getInstance();
		tableAttributes.put("border", "0");
		tableAttributes.put("cellspacing", "0");
		tableAttributes.put("cellpadding", "0");
		tableAttributes.put("class", "listHier lines");

	}
	
	public void setAttribute(String attributeName, String attributeValue) {
		//FIXME do some checks here
		tableAttributes.put(attributeName, attributeValue);
	}

	private void addFunction(Function function)
	{
		functions.put(function.getName().toLowerCase(), function);
	}

	public Object getXY(int x, int y)
	{
		// perhaps move everything to a twodim array first
		return ((List) rows.get(y)).get(x);
	}

	public void setXY(int x, int y, Object content)
	{
		((List) rows.get(y)).set(x, content);
	}

	/**
	 * Add a cell to the current row of the table
	 * 
	 * @param content
	 *        Content of the cell
	 */
	public void addCell(String content)
	{
		if (!"".equals(currentCell))
		{
			newCell();
		}
		addText(content);
	}

	public void newCell()
	{
		currentCell = currentCell.trim();
		if (currentCell.startsWith("="))
		{
			// Logger.debug("Table.addCell: function found.");
			if (null == functionOccurences)
			{
				functionOccurences = new ArrayList();
			}
			functionOccurences.add(new int[] { indexCol, indexRow });
			// function
		}
		currentRow.add(currentCell);
		indexCol++;

		currentCell = "";
	}

	public void addText(String content)
	{
		currentCell += content;
	}

	/**
	 * Finishes current row and starts a new one
	 */
	public void newRow()
	{
		newCell();
		rows.add(currentRow);
		indexRow++;
		// create new row with number of cells of
		// the last row, this is a good guess
		currentRow = new ArrayList(indexCol);
		indexCol = 0;
	}

	/**
	 * Recalculate all cells. Currently does nothing.
	 */
	public void calc()
	{
		if (null != functionOccurences)
		{
			Iterator iterator = functionOccurences.iterator();
			while (iterator.hasNext())
			{
				int[] position = (int[]) iterator.next();
				String functionString = ((String) getXY(position[0],
						position[1])).trim();
				// better use RegEx
				String name = functionString.substring(1,
						functionString.indexOf("(")).trim().toLowerCase();

				String range = functionString.substring(functionString
						.indexOf("(") + 1, functionString.indexOf(")"));
				int colon = range.indexOf(":");
				String start = range.substring(0, colon).trim();
				String end = range.substring(colon + 1).trim();

				int startX = start.charAt(0) - 'A';
				int startY = Integer.parseInt(start.substring(1)) - 1;
				int endX = end.charAt(0) - 'A';
				int endY = Integer.parseInt(end.substring(1)) - 1;

				// normalize range, start is left top, end is bottom right
				if (startX > endX)
				{
					int tmp = startX;
					startX = endX;
					endX = tmp;
				}

				if (startY > endY)
				{
					int tmp = startY;
					startY = endY;
					endY = tmp;
				}

				// Logger.debug("Calc: " + position[0] + " " + position[1] + " "
				// + function + " " + start + " " + end);
				// Logger.debug("Calc: " + startX+","+startY+" -
				// "+endX+","+endY);

				if (functions.containsKey(name))
				{
					Function function = (Function) functions.get(name);
					function.execute(this, position[0], position[1], startX,
							startY, endX, endY);
				}
			}
		}
		return;

	}

	/**
	 * Serialize table by appending it to a writer. The output format is HTML.
	 * 
	 * @param writer
	 *        Writer to append the table object to
	 * @return writer Writer the table object appended itself to
	 */
	public Writer appendTo(Writer writer) throws IOException
	{
		writer.write("<table");// class=\"wiki-table\" cellpadding=\"0\"
								// cellspacing=\"0\" border=\"0\">");
		Set keySet = tableAttributes.keySet();
		String[] toSort = new String[keySet.size()];
		{
			int i = 0;
			for (Iterator it = keySet.iterator(); it.hasNext(); i++)
			{
				toSort[i] = (String) it.next();
			}
		}
		Arrays.sort(toSort);

		for (int i = 0; i < toSort.length; i++)
		{
			String key = toSort[i];
			writer.write(' ');
			writer.write(key);
			writer.write("=\"");
			writer.write((String) tableAttributes.get(key));
			writer.write("\"");
		}

		writer.write(">");
		List[] outputRows = (List[]) rows.toArray(new List[rows.size()]);
		int rowSize = outputRows.length;
		boolean odd = true;
		for (int i = 0; i < rowSize; i++)
		{
			writer.write("<tr");
			if (i == 0)
			{
				writer.write(">");
			}
			else if (odd)
			{
				writer.write(" class=\"table-odd\">");
				odd = false;
			}
			else
			{
				writer.write(" class=\"table-even\">");
				odd = true;
			}
			String[] outputCols = (String[]) outputRows[i]
					.toArray(new String[outputRows[i].size()]);
			int colSize = outputCols.length;
			for (int j = 0; j < colSize; j++)
			{
				writer.write(i == 0 ? "<th scope=\"col\">" : "<td>");
				if (outputCols[j] == null || outputCols[j].trim().length() == 0)
				{
					writer.write("&#160;");
				}
				else
				{
					writer.write(outputCols[j]);
				}
				writer.write(i == 0 ? "</th>" : "</td>");
			}
			writer.write("</tr>");
		}
		writer.write("</table>");
		return writer;
	}
}