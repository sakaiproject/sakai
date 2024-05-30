/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.markup.html.tree.table;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.NullResponse;


/**
 * Class that renders cells of columns aligned in the middle. This class also takes care of counting
 * their widths and of column spans.
 * 
 * @author Matej Knopp
 */
final class MiddleColumnsView extends AbstractColumnsView
{
	private static final long serialVersionUID = 1L;

	private final boolean treeHasLeftColumn;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 * @param treeHasLeftColumn
	 *            Whether there is a column aligned to left in the tree table
	 */
	public MiddleColumnsView(final String id, final TreeNode node, final boolean treeHasLeftColumn)
	{
		super(id, node);
		this.treeHasLeftColumn = treeHasLeftColumn;
	}

	/**
	 * Computes the percentagle widths of columns. If a column spans over other columns, the widths
	 * of those columns will be zero.
	 * 
	 * @return widths of columns
	 */
	protected double[] computeColumnWidths()
	{
		// initialize the columns array
		double result[] = new double[columns.size()];
		Arrays.fill(result, 0d);

		// the sum of weights of all columns
		double sum = 0d;
		double whole = 99.8d;

		// go over all columns, check their alignment and count sum of their
		// weights
		for (IColumn column : columns)
		{
			// check if the unit is right
			if (column.getLocation().getUnit() != ColumnLocation.Unit.PROPORTIONAL)
			{
				throw new IllegalStateException("Middle columns must have PROPORTIONAL unit set.");
			}
			sum += column.getLocation().getSize();
		}


		int index = 0; // index of currently processed column

		int spanColumn = 0; // index of column that is spanning over currently
		// processed column (if any)
		int spanLeft = 0; // over how many columns does the spanning column
		// span

		for (IColumn column : columns)
		{
			int ix = index; // to which column should we append the size
			if (spanLeft > 0) // is there a column spanning over current
			// column?
			{
				ix = spanColumn; // the size should be appended to the
				// spanning
				// column
				--spanLeft;
			}
			// add the percentage size to the column
			result[ix] += Math.round((column.getLocation().getSize()) / sum * whole);

			// wants this column to span and no other column is spanning over
			// this column?
			if ((spanLeft == 0) && (column.getSpan(node) > 1))
			{
				int maxSpan = columns.size() - columns.indexOf(column); // how
				// many
				// columns
				// left
				int span = column.getSpan(node) - 1; // how much columns want
				// the column to span
				// over
				spanColumn = index; // index of column that is spanning
				spanLeft = span < maxSpan ? span : maxSpan; // set the number of
				// columns spanned
				// over
			}
			++index;
		}

		// count the sum
		double together = 0d;

		for (double value : result)
		{
			together += value;
		}

		// is it bigger than 99.8? that can cause layout problems in IE
		if (together > 99.8d)
		{
			// this can happen - rounding error. just decrease the last one
			for (int i = result.length - 1; i >= 0; --i)
			{
				if (result[i] != 0d)
				{
					result[i] -= together - 99.8d;
					break;
				}
			}

		}

		return result;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#onRender()
	 */
	@Override
	protected void onRender()
	{
		Response response = RequestCycle.get().getResponse();
		double widths[] = computeColumnWidths();

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(0);
		nf.setMaximumIntegerDigits(3);

		for (int i = 0; i < columns.size(); ++i)
		{
			Component component = components.get(i);
			IRenderable renderable = renderables.get(i);
			IColumn column = columns.get(i);

			// write the wrapping column markup
			response.write("<span class=\"b_\" style=\"width:" + nf.format(widths[i]) + "%\">");

			// determine whether we should render the left border
			if (!treeHasLeftColumn && (i == 0))
			{
				response.write("<span class=\"d_\">");
			}
			else
			{
				response.write("<span class=\"c_\">");
			}

			if (component != null) // is there a component for current column?
			{
				// render the component
				component.render();
			}
			else if (renderable != null) // no component - try to render
			// renderable
			{
				renderable.render(node, response);
			}
			else
			{
				// no renderable or component. fail
				throw new IllegalStateException(
					"Either renderable or cell component must be created for this noode");
			}

			// end of wrapping markup
			response.write("</span></span>\n");

			// does this component span over other columns
			int span = column.getSpan(node);
			if (span > 1)
			{
				// iterate through the columns and if any of them has a
				// component,
				// render the component to null response (otherwise the
				// component will
				// complain that it hasn't been rendered
				for (int j = 1; (j < span) && (i < components.size()); ++j)
				{
					++i;
					if (components.get(i) != null)
					{
						Response old = RequestCycle.get().setResponse(NullResponse.getInstance());
						(components.get(i)).render();
						RequestCycle.get().setResponse(old);
					}
				}
			}
		}
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getMarkup(org.apache.wicket.Component)
	 */
	@Override
	public IMarkupFragment getMarkup(final Component child)
	{
		// each direct child gets the markup of this repeater
		return getMarkup();
	}
}
