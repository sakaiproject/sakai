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

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;


/**
 * Class that renders cell of columns aligned to the left or to the right.
 * 
 * @author Matej Knopp
 */
final class SideColumnsView extends AbstractColumnsView
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 */
	public SideColumnsView(final String id, final TreeNode node)
	{
		super(id, node);
		setRenderBodyOnly(true);
	}

	/**
	 * Adds a column to be rendered.
	 * 
	 * @param column
	 *            The column to add
	 * @param component
	 *            The component
	 * @param renderable
	 *            The renderer
	 */
	@Override
	public void addColumn(final IColumn column, final Component component,
		final IRenderable renderable)
	{
		// if the column is aligned to the left, just append it.
		// Otherwise we prepend it, because we want columns aligned to right
		// to be rendered reverse order (because they will have set
		// float:right
		// in css, so they will be displayed in reverse order too).
		Position position = (column.getLocation().getAlignment() == ColumnLocation.Alignment.LEFT)
			? Position.APPEND : Position.PREPEND;
		super.addColumn(column, component, renderable, position);
	}

	@Override
	protected void onRender()
	{
		Response response = RequestCycle.get().getResponse();

		// whether there was no left column rendered yet
		boolean firstLeft = true;

		for (int i = 0; i < columns.size(); ++i)
		{
			IColumn column = columns.get(i);
			Component component = components.get(i);
			IRenderable renderable = renderables.get(i);

			// write wrapping markup
			response.write("<span class=\"b_\" style=\"" + renderColumnStyle(column) + "\">");
			if ((column.getLocation().getAlignment() == ColumnLocation.Alignment.LEFT) && (firstLeft == true))
			{
				// for the first left column we have different style class
				// (without the left border)
				response.write("<span class=\"d_\">");
				firstLeft = false;
			}
			else
			{
				response.write("<span class=\"c_\">");
			}

			if (component != null)
			{
				component.render();
			}
			else if (renderable != null)
			{
				renderable.render(node, response);
			}
			else
			{
				throw new IllegalStateException(
					"Either renderable or cell component must be created for this node");
			}

			response.write("</span></span>\n");
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

	/**
	 * Renders the float css atribute of the given column.
	 * 
	 * @param column
	 *            The
	 * @return The column as a string
	 */
	private String renderColumnFloat(final IColumn column)
	{
		ColumnLocation location = column.getLocation();
		if (location.getAlignment() == ColumnLocation.Alignment.LEFT)
		{
			return "left";
		}
		else if (location.getAlignment() == ColumnLocation.Alignment.RIGHT)
		{
			return "right";
		}
		else
		{
			throw new IllegalStateException("Wrong column allignment.");
		}
	}

	/**
	 * Renders content of the style attribute for the given column.
	 * 
	 * @param column
	 *            The column to render the style attribute from
	 * @return The style as a string
	 */
	private String renderColumnStyle(final IColumn column)
	{
		return "width:" + renderColumnWidth(column) + ";float:" + renderColumnFloat(column);
	}

	/**
	 * Renders width of given column as string.
	 * 
	 * @param column
	 *            The column to render as a string
	 * @return The column as a string
	 */
	private String renderColumnWidth(final IColumn column)
	{
		ColumnLocation location = column.getLocation();
		return "" + location.getSize() + renderUnit(location.getUnit());
	}

	/**
	 * Renders given unit as string.
	 * 
	 * @param unit
	 *            The unit to render to a string
	 * @return The unit as a string
	 */
	private String renderUnit(final ColumnLocation.Unit unit)
	{
		if (unit == ColumnLocation.Unit.EM)
		{
			return "em";
		}
		else if (unit == ColumnLocation.Unit.PX)
		{
			return "px";
		}
		else if (unit == ColumnLocation.Unit.PERCENT)
		{
			return "%";
		}
		else
		{
			throw new IllegalStateException("Wrong column unit for column aligned left or right.");
		}
	}

}
