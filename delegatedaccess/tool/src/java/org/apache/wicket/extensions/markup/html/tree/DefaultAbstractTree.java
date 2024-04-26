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
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.io.IClusterable;

/**
 * Tree class that contains convenient functions related to presentation of the tree, which includes
 * junction link, tree item selection link, spacers (with lines) and default tree item and folder
 * icons.
 * <p>
 * The class itself adds no component to tree items. If you use this class directly, you have to
 * implement populateTreeItem() on your own. If you want to use an existing (complete) tree class,
 * use {@link Tree}
 * <p>
 * This class allows you to choose between 3 types of links.
 * {@link DefaultAbstractTree#setLinkType(LinkType)}
 * 
 * @author Matej Knopp
 */
@Deprecated
public abstract class DefaultAbstractTree extends AbstractTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Helper class for calling an action from a link.
	 * 
	 * @author Matej Knopp
	 */
	protected interface ILinkCallback extends IAjaxLink, IClusterable
	{
	}

	/**
	 * Reference to the css file.
	 */
	private static final ResourceReference CSS = new CssResourceReference(
		DefaultAbstractTree.class, "res/tree.css");

	/** Reference to the icon of closed tree folder */
	private static final ResourceReference FOLDER_CLOSED = new PackageResourceReference(
		DefaultAbstractTree.class, "res/folder-closed.gif");

	/** Reference to the icon of open tree folder */
	private static final ResourceReference FOLDER_OPEN = new PackageResourceReference(
		DefaultAbstractTree.class, "res/folder-open.gif");

	/** Reference to the icon of tree item (not a folder) */
	private static final ResourceReference ITEM = new PackageResourceReference(
		DefaultAbstractTree.class, "res/item.gif");

	/** The link type, default is {@link LinkType#AJAX ajax}. */
	private LinkType linkType = LinkType.AJAX;

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 */
	public DefaultAbstractTree(final String id)
	{
		super(id);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public DefaultAbstractTree(final String id, final IModel<? extends TreeModel> model)
	{
		super(id, model);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public DefaultAbstractTree(final String id, final TreeModel model)
	{
		super(id, new WicketTreeModel());
		setModelObject(model);
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
	 * Sets the type of links on tree items. After the link type is changed, the whole tree is
	 * rebuild and re-rendered.
	 * 
	 * @param linkType
	 *            type of links
	 */
	public void setLinkType(final LinkType linkType)
	{
		if (this.linkType != linkType)
		{
			this.linkType = linkType;
			invalidateAll();
		}
	}

	/**
	 * Returns the resource reference of default stylesheet.
	 * 
	 * @return The package resource reference
	 */
	protected ResourceReference getCSS()
	{
		return CSS;
	}

	/**
	 * Returns the resource reference of default closed tree folder.
	 * 
	 * @return The package resource reference
	 */
	protected ResourceReference getFolderClosed()
	{
		return FOLDER_CLOSED;
	}

	/**
	 * Returns the resource reference of default open tree folder.
	 * 
	 * @return The package resource reference
	 */
	protected ResourceReference getFolderOpen()
	{
		return FOLDER_OPEN;
	}

	/**
	 * Returns the resource reference of default tree item (not folder).
	 * 
	 * @return The package resource reference
	 */
	protected ResourceReference getItem()
	{
		return ITEM;
	}

	/**
	 * Returns the resource reference for icon of specified tree node.
	 * 
	 * @param node
	 *            The node
	 * @return The package resource reference
	 */
	protected ResourceReference getNodeIcon(final TreeNode node)
	{
		if (node.isLeaf() == true)
		{
			return getItem();
		}
		else
		{
			if (isNodeExpanded(node))
			{
				return getFolderOpen();
			}
			else
			{
				return getFolderClosed();
			}
		}
	}

	/**
	 * Creates the indentation element. This element should be placed as first element in the tree
	 * item markup to ensure proper indentation of the tree item. This implementation also takes
	 * care of lines that connect nodes.
	 * 
	 * @param parent
	 *            The component parent
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node for which to create the indentation element
	 * @param level
	 *            The current level
	 * @return The indentation component
	 */
	protected Component newIndentation(final MarkupContainer parent, final String id,
		final TreeNode node, final int level)
	{
		WebMarkupContainer result = new WebMarkupContainer(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
			{
				Response response = RequestCycle.get().getResponse();
				TreeNode parent = node.getParent();

				CharSequence urls[] = new CharSequence[level];
				for (int i = 0; i < level; ++i)
				{
					if (isNodeLast(parent))
					{
						urls[i] = "indent-blank";
					}
					else
					{
						urls[i] = "indent-line";
					}

					parent = parent.getParent();
				}

				for (int i = level - 1; i >= 0; --i)
				{
					response.write("<span class=\"" + urls[i] + "\"></span>");
				}
			}
		};
		result.setRenderBodyOnly(true);
		return result;
	}

	/**
	 * Creates an image placed on junction link. This image actually consists of two spans with
	 * different css classes. These classes are specified according to the stylesheet to make the
	 * junction image look well together with lines connecting nodes.
	 * 
	 * @param parent
	 *            The component parent
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 * @return The component that represents a junction
	 */
	protected MarkupContainer newJunctionImage(final MarkupContainer parent, final String id,
		final TreeNode node)
	{
		return (MarkupContainer)new WebMarkupContainer(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);

				final String cssClassInner;
				if (node.isLeaf() == false)
				{
					cssClassInner = isNodeExpanded(node) ? "minus" : "plus";
				}
				else
				{
					cssClassInner = "corner";
				}

				final String cssClassOuter = isNodeLast(node) ? "junction-last" : "junction";

				Response response = RequestCycle.get().getResponse();
				response.write("<span class=\"" + cssClassOuter + "\"><span class=\"" +
					cssClassInner + "\"></span></span>");
			}
		}.setRenderBodyOnly(true);
	}

	/**
	 * Creates the junction link for given node. Also (optionally) creates the junction image. If
	 * the node is a leaf (it has no children), the created junction link is non-functional.
	 * 
	 * @param parent
	 *            parent component of the link
	 * 
	 * @param id
	 *            wicket:id of the component
	 * 
	 * @param imageId
	 *            wicket:id of the image. this can be null, in that case image is not created. image
	 *            is supposed to be placed on the link (link is parent of image)
	 * 
	 * @param node
	 *            tree node for which the link should be created.
	 * @return The link component
	 */
	protected Component newJunctionLink(final MarkupContainer parent, final String id,
		final String imageId, final TreeNode node)
	{
		final MarkupContainer junctionLink;

		if (node.isLeaf() == false)
		{
			junctionLink = newLink(parent, id, new ILinkCallback()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(final AjaxRequestTarget target)
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
		}
		else
		{
			junctionLink = new WebMarkupContainer(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * {@inheritDoc}
				 */
				@Override
				protected void onComponentTag(final ComponentTag tag)
				{
					super.onComponentTag(tag);
					tag.put("onclick", "return false");
				}
			};
		}

		if (imageId != null)
		{
			junctionLink.add(newJunctionImage(junctionLink, imageId, node));
		}

		return junctionLink;
	}

	/**
	 * Creates a link of type specified by current linkType. When the links is clicked it calls the
	 * specified callback.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param callback
	 *            The link call back
	 * @return The link component
	 */
	protected MarkupContainer newLink(final MarkupContainer parent, final String id,
		final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link<Void>(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * {@inheritDoc}
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
				 * {@inheritDoc}
				 */
				@Override
				public void onClick(final AjaxRequestTarget target)
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
				 * {@inheritDoc}
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
	 * Creates the icon for current node. By default uses image reference specified by
	 * {@link DefaultAbstractTree#getNodeIcon(TreeNode)}.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param node
	 *            The tree node
	 * @return The web component that represents the icon of the current node
	 */
	protected Component newNodeIcon(final MarkupContainer parent, final String id,
		final TreeNode node)
	{
		return new WebMarkupContainer(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				IRequestHandler handler = new ResourceReferenceRequestHandler(getNodeIcon(node));
				tag.put("style", "background-image: url('" + RequestCycle.get().urlFor(handler) +
					"')");
			}
		};
	}

	/**
	 * Creates a link that can be used to select / deselect the specified node.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @param node
	 *            The parent node
	 * @return The component that represents the link
	 */
	protected MarkupContainer newNodeLink(final MarkupContainer parent, final String id,
		final TreeNode node)
	{
		return newLink(parent, id, new ILinkCallback()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				getTreeState().selectNode(node, !getTreeState().isNodeSelected(node));
				onNodeLinkClicked(target, node);

				if (target != null)
				{
					updateTree(target);
				}
			}
		});
	}

	/**
	 * Callback function called after user clicked on an junction link. The node has already been
	 * expanded/collapsed (depending on previous status).
	 * 
	 * @param target
	 *            Request target - may be null on non-ajax call
	 * @param node
	 *            Node for which this callback is relevant
	 */
	protected void onJunctionLinkClicked(final AjaxRequestTarget target, final TreeNode node)
	{
	}

	/**
	 * This callback method is called after user has selected / deselected the given node.
	 * 
	 * @param target
	 *            Request target - may be null on non-ajax call
	 * @param node
	 *            Node for which this this callback is fired.
	 */
	protected void onNodeLinkClicked(final AjaxRequestTarget target, final TreeNode node)
	{
	}

	/**
	 * Returns whether the provided node is last child of it's parent.
	 * 
	 * @param node
	 *            The node
	 * @return whether the provided node is the last child
	 */
	private boolean isNodeLast(final TreeNode node)
	{
		TreeNode parent = node.getParent();
		if (parent == null)
		{
			return true;
		}
		else
		{
			return parent.getChildAt(parent.getChildCount() - 1).equals(node);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		super.renderHead(response);
		ResourceReference css = getCSS();
		if (css != null)
		{
			response.render(CssHeaderItem.forReference(css));
		}
	}
}
