/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.content.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.sakaiproject.entity.api.Entity;

public class VirtualFileSystem implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String rootId;
	private VirtualDirectory rootDirectory;

	public VirtualFileSystem(String rootId)
	{
		this.rootId = rootId;
		this.rootDirectory = new VirtualDirectory(rootId);
	}

	public void addPath(String path)
	{
		findNode(path, true);
	}

	private VirtualNode findNode(String path, boolean isWritable)
	{
		if (path.startsWith(Entity.SEPARATOR) && path.length() > 1)
		{
			path = path.substring(1, path.length());
		}

		boolean isNodeDirectory = path.endsWith(Entity.SEPARATOR);
		if (path.length() == 0)
		{
			return rootDirectory;
		}

		String[] tokens = path.split(Entity.SEPARATOR);
		VirtualNode currentNode = null;
		VirtualDirectory currentDirectory = rootDirectory;

		for (int i = 0; i < tokens.length; i++)
		{
			String itemName = tokens[i];
			boolean isDirectory = i + 1 < tokens.length;

			currentNode = currentDirectory.getChild(itemName);
			if (currentNode == null)
			{
				if (isWritable)
				{
					if (isDirectory || isNodeDirectory)
					{
						currentNode = new VirtualDirectory(itemName);
					}
					else
					{
						currentNode = new VirtualNode(itemName);
					}

					currentDirectory.addChild(itemName, currentNode);
				}
				else
				{
					return null;
				}
			}

			if (currentNode != null)
			{
				if (currentNode.isDirectory())
				{
					currentDirectory = (VirtualDirectory) currentNode;
				}
			}
		}

		return currentNode;
	}

	public List<String> getChildren(String path)
	{
		List<String> list = new LinkedList<>();
		VirtualNode node = findNode(path, false);
		if (node == null || !node.isDirectory())
		{
			return list;
		}

		VirtualDirectory dir = (VirtualDirectory) node;
		return dir.getChildren();
	}

	public int getCount(String path)
	{
		VirtualNode node = findNode(path, false);
		if (node == null || !node.isDirectory())
		{
			return 0;
		}

		VirtualDirectory dir = (VirtualDirectory) node;
		return dir.getNumberOfChildren();
	}

	public class VirtualDirectory extends VirtualNode
	{
		private static final long serialVersionUID = 1L;

		private Map<String, VirtualNode> children;

		public VirtualDirectory(String name)
		{
			super(name);
			children = new HashMap<>();
		}

		public VirtualNode addChild(String name, VirtualNode node)
		{
			return children.put(name, node);
		}

		public VirtualNode getChild(String name)
		{
			return children.get(name);
		}

		public List<String> getChildren()
		{
			List<String> list = new LinkedList<>();
			for (VirtualNode node : children.values())
			{
				String name = node.isDirectory() ? node.getName() + Entity.SEPARATOR : node.getName();
				list.add(name);
			}

			return list;
		}

		public int getNumberOfChildren()
		{
			return children.size();
		}

		@Override
		public boolean isDirectory()
		{
			return true;
		}
	}

	public static class VirtualNode implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Getter private String name;

		public VirtualNode(String name)
		{
			this.name = name;
		}

		public boolean isDirectory()
		{
			return false;
		}
	}
}
