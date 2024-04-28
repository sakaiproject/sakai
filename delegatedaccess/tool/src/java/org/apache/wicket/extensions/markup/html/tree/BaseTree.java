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
package org.apache.wicket.extensions.markup.html.tree;

import java.util.Optional;

import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.string.Strings;

/**
 * An abstract Tree component that should serve as a base for custom Tree Components.
 * 
 * It has one abstract method - {@link #newNodeComponent(String, IModel)} that needs to be
 * overridden.
 * 
 * @author Matej Knopp
 */
@Deprecated
public abstract class BaseTree extends AbstractTree
{
	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public BaseTree(String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public BaseTree(String id, IModel<? extends TreeModel> model)
	{
		super(id, model);
	}

	// default stylesheet resource
	private static final ResourceReference CSS = new CssResourceReference(BaseTree.class,
		"res/base-tree.css");

	/**
	 * Returns the stylesheet reference
	 * 
	 * @return stylesheet reference
	 */
	protected ResourceReference getCSS()
	{
		return CSS;
	}

	private static final long serialVersionUID = 1L;

	private static final String JUNCTION_LINK_ID = "junctionLink";
	private static final String NODE_COMPONENT_ID = "nodeComponent";

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.AbstractTree#populateTreeItem(WebMarkupContainer,
	 *      int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void populateTreeItem(WebMarkupContainer item, int level)
	{
		// add junction link
		Object node = item.getDefaultModelObject();
		Component junctionLink = newJunctionLink(item, JUNCTION_LINK_ID, node);
		junctionLink.add(new JunctionBorder(node, level));
		item.add(junctionLink);

		// add node component
		Component nodeComponent = newNodeComponent(NODE_COMPONENT_ID,
			(IModel<Object>)item.getDefaultModel());
		item.add(nodeComponent);

		// add behavior that conditionally adds the "selected" CSS class name
		item.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentTag(Component component, ComponentTag tag)
			{
				Object node = component.getDefaultModelObject();
				String klass = getItemClass(node);
				if (!Strings.isEmpty(klass))
				{
					CharSequence oldClass = tag.getAttribute("class");
					if (Strings.isEmpty(oldClass))
					{
						tag.put("class", klass);
					}
					else
					{
						tag.put("class", oldClass + " " + klass);
					}
				}
			}
		});
	}

	protected String getItemClass(Object node)
	{
		if (getTreeState().isNodeSelected(node))
		{
			return getSelectedClass();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the class name that will be added to row's CSS class for selected rows
	 * 
	 * @return CSS class name
	 */
	protected String getSelectedClass()
	{
		return "selected";
	}

	/**
	 * Creates a new component for the given TreeNode.
	 * 
	 * @param id
	 *            component ID
	 * @param model
	 *            model that returns the node
	 * @return component for node
	 */
	protected abstract Component newNodeComponent(String id, IModel<Object> model);

	/**
	 * Returns whether the provided node is last child of it's parent.
	 * 
	 * @param node
	 *            The node
	 * @return whether the provided node is the last child
	 */
	private boolean isNodeLast(Object node)
	{
		Object parent = getParentNode(node);
		if (parent == null)
		{
			return true;
		}
		else
		{
			return getChildAt(parent, getChildCount(parent) - 1).equals(node);
		}
	}

	/**
	 * Class that wraps a link (or span) with a junction table cells.
	 * 
	 * @author Matej Knopp
	 */
	private class JunctionBorder extends Behavior
	{
		private static final long serialVersionUID = 1L;

		// TODO this field is not serializable but nested inside an serializable component
		private final Object node;
		private final int level;

		/**
		 * Construct.
		 * 
		 * @param node
		 * @param level
		 */
		public JunctionBorder(Object node, int level)
		{
			this.node = node;
			this.level = level;
		}

		/**
		 * @see Behavior#afterRender(Component)
		 */
		@Override
		public void afterRender(final Component component)
		{
			component.getResponse().write("</td>");
		}

		/**
		 * @see Behavior#beforeRender(Component)
		 */
		@Override
		public void beforeRender(final Component component)
		{
			Response response = component.getResponse();
			Object parent = getParentNode(node);

			CharSequence classes[] = new CharSequence[level];
			for (int i = 0; i < level; ++i)
			{
				if (parent == null || isNodeLast(parent))
				{
					classes[i] = "spacer";
				}
				else
				{
					classes[i] = "line";
				}

				parent = getParentNode(parent);
			}

			for (int i = level - 1; i >= 0; --i)
			{
				response.write("<td class=\"" + classes[i] + "\"><span></span></td>");
			}

			if (isNodeLast(node))
			{
				response.write("<td class=\"half-line\">");
			}
			else
			{
				response.write("<td class=\"line\">");
			}
		}
	}

	/**
	 * Creates the junction link for given node. Also (optionally) creates the junction image. If
	 * the node is a leaf (it has no children), the created junction link is non-functional.
	 * 
	 * @param parent
	 *            parent component of the link
	 * @param id
	 *            wicket:id of the component
	 * @param node
	 *            tree node for which the link should be created.
	 * @return The link component
	 */
	protected Component newJunctionLink(MarkupContainer parent, final String id, final Object node)
	{
		final MarkupContainer junctionLink;

		if (isLeaf(node) == false)
		{
			junctionLink = newLink(id, new ILinkCallback()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					if (isNodeExpanded(node))
					{
						getTreeState().collapseNode(node);
					}
					else
					{
						getTreeState().expandNode(node);
					}
					onJunctionLinkClicked(target, node);

					if (target != null)
					{
						updateTree(target);
					}
				}
			});
			junctionLink.add(new Behavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onComponentTag(Component component, ComponentTag tag)
				{
					if (isNodeExpanded(node))
					{
						tag.put("class", "junction-open");
					}
					else
					{
						tag.put("class", "junction-closed");
					}
				}
			});
		}
		else
		{
			junctionLink = new WebMarkupContainer(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see Component#onComponentTag(ComponentTag)
				 */
				@Override
				protected void onComponentTag(ComponentTag tag)
				{
					super.onComponentTag(tag);
					tag.setName("span");
					tag.put("class", "junction-corner");
				}
			};

		}

		return junctionLink;
	}

	/**
	 * Callback function called after user clicked on an junction link. The node has already been
	 * expanded/collapsed (depending on previous status).
	 * 
	 * @param target
	 *            Request target - may be null on non-ajax call
	 * 
	 * @param node
	 *            Node for which this callback is relevant
	 */
	protected void onJunctionLinkClicked(AjaxRequestTarget target, Object node)
	{
	}

	/**
	 * Helper class for calling an action from a link.
	 * 
	 * @author Matej Knopp
	 */
	public interface ILinkCallback extends IAjaxLink, IClusterable
	{
	}

	/**
	 * Creates a link of type specified by current linkType. When the links is clicked it calls the
	 * specified callback.
	 * 
	 * @param id
	 *            The component id
	 * @param callback
	 *            The link call back. {@code null} is passed for its onClick(AjaxRequestTarget) for
	 *            {@link LinkType#REGULAR} and eventually for {@link LinkType#AJAX_FALLBACK}.
	 * @return The link component
	 */
	public MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link<Void>(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see Link#onClick()
				 */
				@Override
				public void onClick()
				{
					callback.onClick(null);
				}
			};
		}
		else if (getLinkType() == LinkType.AJAX)
		{
			return new AjaxLink<Void>(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see AjaxLink#onClick(AjaxRequestTarget)
				 */
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
		else
		{
			return new AjaxFallbackLink<Void>(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see AjaxFallbackLink#onClick(AjaxRequestTarget)
				 */
				@Override
				public void onClick(Optional<AjaxRequestTarget> target)
				{
					callback.onClick(target.orElse(null));
				}
			};
		}
	}

	/**
	 * Returns the current type of links on tree items.
	 * 
	 * @return The link type
	 */
	public LinkType getLinkType()
	{
		return linkType;
	}

	/**
	 * Sets the type of links on tree items. After the link type is changed, the whole tree must be
	 * rebuilt (call invalidateAll).
	 * 
	 * @param linkType
	 *            type of links
	 */
	public void setLinkType(LinkType linkType)
	{
		if (this.linkType != linkType)
		{
			this.linkType = linkType;
		}
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.AbstractTree#isForceRebuildOnSelectionChange()
	 */
	@Override
	protected boolean isForceRebuildOnSelectionChange()
	{
		return false;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		ResourceReference css = getCSS();
		if (css != null)
		{
			response.render(CssHeaderItem.forReference(css));
		}

	}

	private LinkType linkType = LinkType.AJAX;
}
