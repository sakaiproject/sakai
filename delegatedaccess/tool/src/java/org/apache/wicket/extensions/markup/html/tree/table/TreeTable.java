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

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree;
import org.apache.wicket.extensions.markup.html.tree.WicketTreeModel;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IClusterable;


/**
 * TreeTable is a component that represents a grid with a tree. It's divided into columns. One of
 * the columns has to be column derived from {@link PropertyTreeColumn}.
 *
 * @deprecated Use {@link org.apache.wicket.extensions.markup.html.repeater.tree.TableTree} instead
 * @author Matej Knopp
 */
@Deprecated
public class TreeTable extends DefaultAbstractTree
{
	/**
	 * Callback for rendering tree node text.
	 */
	public static interface IRenderNodeCallback extends IClusterable
	{
		/**
		 * Renders the tree node to text.
		 * 
		 * @param node
		 *            The tree node to render
		 * @return the tree node as text
		 */
		public String renderNode(TreeNode node);
	}

	/**
	 * Represents a content of a cell in TreeColumn (column containing the actual tree).
	 * 
	 * @author Matej Knopp
	 */
	private class TreeFragment extends Fragment
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param id
		 * @param node
		 * @param level
		 * @param renderNodeCallback
		 *            The call back for rendering nodes
		 */
		public TreeFragment(final String id, final TreeNode node, final int level,
			final IRenderNodeCallback renderNodeCallback)
		{
			super(id, "fragment", TreeTable.this);

			add(newIndentation(this, "indent", node, level));

			add(newJunctionLink(this, "link", "image", node));

			MarkupContainer nodeLink = newNodeLink(this, "nodeLink", node);
			add(nodeLink);

			nodeLink.add(newNodeIcon(nodeLink, "icon", node));

			nodeLink.add(new Label("label", new IModel<String>()
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see IModel#getObject()
				 */
				@Override
				public String getObject()
				{
					return renderNodeCallback.renderNode(node);
				}
			}));
		}
	}

	/** Reference to the css file. */
	private static final ResourceReference CSS = new CssResourceReference(
		DefaultAbstractTree.class, "res/tree-table.css");

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a tree cell for given node. This method is supposed to be used by TreeColumns
	 * (columns that draw the actual tree).
	 * 
	 * @param parent
	 *            Parent component
	 * 
	 * @param id
	 *            Component ID
	 * 
	 * @param node
	 *            Tree node for the row
	 * 
	 * @param level
	 *            How deep is the node nested (for convenience)
	 * 
	 * @param callback
	 *            Used to get the display string
	 * 
	 * @param table
	 *            Tree table
	 * 
	 * @return The tree cell
	 */
	public static Component newTreeCell(final MarkupContainer parent, final String id,
		final TreeNode node, final int level, final IRenderNodeCallback callback,
		final TreeTable table)
	{
		return table.newTreePanel(parent, id, node, level, callback);
	}

	// columns of the TreeTable
	private final IColumn columns[];

	/**
	 * Creates the TreeTable for the given array of columns.
	 * 
	 * @param id
	 * @param columns
	 */
	public TreeTable(final String id, final IColumn columns[])
	{
		this(id, (TreeModel)null, columns);
	}

	/**
	 * Creates the TreeTable for the given TreeModel and array of columns.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 * @param columns
	 *            The columns
	 */
	public TreeTable(final String id, final TreeModel model, final IColumn columns[])
	{
		this(id, new WicketTreeModel(model), columns);
	}

	/**
	 * Creates the TreeTable for the given model and array of columns.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 * @param columns
	 *            The columns
	 */
	public TreeTable(final String id, final IModel<? extends TreeModel> model,
		final IColumn columns[])
	{
		super(id, model);

		this.columns = columns;

		// Attach the javascript that resizes the header according to the body
		// This is necessary to support fixed position header. The header does
		// not
		// scroll together with body. The body contains vertical scrollbar. The
		// header width must be same as body content width, so that the columns
		// are properly aligned.
		add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void renderHead(final Component component, final IHeaderResponse response)
			{
				response.render(OnDomReadyHeaderItem.forScript("Wicket.TreeTable.attachUpdate(\"" +
					getMarkupId() + "\")"));
			}
		});
	}

	private boolean hasLeftColumn()
	{
		for (IColumn column : columns)
		{
			if (column.getLocation().getAlignment().equals(Alignment.LEFT))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds the header to the TreeTable.
	 */
	protected void addHeader()
	{
		// create the view for side columns
		SideColumnsView sideColumns = new SideColumnsView("sideHeaderColumns", null);
		add(sideColumns);
		if (columns != null)
		{
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if ((column.getLocation().getAlignment() == Alignment.LEFT) ||
					(column.getLocation().getAlignment() == Alignment.RIGHT))
				{
					TreeTableItem component = new TreeTableItem(i);
					Component cell = column.newHeader(sideColumns, TreeTableItem.ID);
					component.add(cell);
					sideColumns.addColumn(column, component, null);
				}
			}
		}

		// create the view for middle columns
		MiddleColumnsView middleColumns = new MiddleColumnsView("middleHeaderColumns", null,
			hasLeftColumn());
		add(middleColumns);
		if (columns != null)
		{
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.MIDDLE)
				{
					TreeTableItem component = new TreeTableItem(i);
					Component cell = column.newHeader(middleColumns, TreeTableItem.ID);
					component.add(cell);
					middleColumns.addColumn(column, component, null);
				}
			}
		}
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree#getCSS()
	 */
	@Override
	protected ResourceReference getCSS()
	{
		return CSS;
	}

	/**
	 * Creates a new instance of the TreeFragment.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level of the tree row
	 * @param renderNodeCallback
	 *            The node call back
	 * @return The tree panel
	 */
	protected Component newTreePanel(final MarkupContainer parent, final String id,
		final TreeNode node, final int level, final IRenderNodeCallback renderNodeCallback)
	{
		return new TreeFragment(id, node, level, renderNodeCallback);
	}

	/**
	 * @see AbstractTree#onBeforeAttach()
	 */
	@Override
	protected void onBeforeAttach()
	{
		// has the header been added yet?
		if (get("sideHeaderColumns") == null)
		{
			// no. initialize columns first
			if (columns != null)
			{
				for (IColumn column : columns)
				{
					column.setTreeTable(this);
				}
			}

			// add the tree table header
			addHeader();
		}
	}

	/**
	 * Populates one row of the tree.
	 * 
	 * @param item
	 *            the tree node component
	 * @param level
	 *            the current level
	 */
	@Override
	protected void populateTreeItem(final WebMarkupContainer item, final int level)
	{
		final TreeNode node = (TreeNode)item.getDefaultModelObject();

		// add side columns
		SideColumnsView sideColumns = new SideColumnsView("sideBodyColumns", node);
		item.add(sideColumns);
		if (columns != null)
		{
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if ((column.getLocation().getAlignment() == Alignment.LEFT) ||
					(column.getLocation().getAlignment() == Alignment.RIGHT))
				{
					TreeTableItem component;
					// first try to create a renderable
					IRenderable renderable = column.newCell(node, level);

					if (renderable == null)
					{
						// if renderable failed, try to create a regular component.
						component = new TreeTableItem(i);
						Component cell = column.newCell(sideColumns, TreeTableItem.ID, node, level);
						component.add(cell);
					}
					else
					{
						component = null;
					}

					sideColumns.addColumn(column, component, renderable);
				}
			}
		}

		// add middle columns
		MiddleColumnsView middleColumns = new MiddleColumnsView("middleBodyColumns", node,
			hasLeftColumn());
		if (columns != null)
		{
			for (int i = 0; i < columns.length; i++)
			{
				IColumn column = columns[i];
				if (column.getLocation().getAlignment() == Alignment.MIDDLE)
				{
					TreeTableItem component;
					// first try to create a renderable
					IRenderable renderable = column.newCell(node, level);

					if (renderable == null)
					{
						// if renderable failed, try to create a regular component
						component = new TreeTableItem(i);
						Component cell = column.newCell(middleColumns, TreeTableItem.ID, node,
							level);
						component.add(cell);
					}
					else
					{
						component = null;
					}

					middleColumns.addColumn(column, component, renderable);
				}
			}
		}
		item.add(middleColumns);

		// do distinguish between selected and unselected rows we add an
		// behavior that modifies row css class.
		item.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentTag(final Component component, final ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				tag.put("class", "row");
				if (getTreeState().isNodeSelected(node))
				{
					tag.put("class", "row row-selected");
				}
			}
		});
	}
}
