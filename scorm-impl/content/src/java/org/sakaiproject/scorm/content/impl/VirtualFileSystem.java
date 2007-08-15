package org.sakaiproject.scorm.content.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;

public class VirtualFileSystem {
	private String rootId;
	private VirtualDirectory rootDirectory;
	
	public VirtualFileSystem(String rootId) {
		this.rootId = rootId;	
		this.rootDirectory = new VirtualDirectory(rootId);
	}
	
	public void addPath(String path) {
		findNode(path, true);		
	}
	
	public List<String> getChildren(String path) {
		List<String> list = new LinkedList<String>();
		VirtualNode node = findNode(path, false);
		if (node == null || !node.isDirectory())
			return list;
		
		VirtualDirectory dir = (VirtualDirectory)node;
		
		return dir.getChildren();
	}
	
	public int getCount(String path) {
		VirtualNode node = findNode(path, false);
		if (node == null || !node.isDirectory())
			return 0;
		
		VirtualDirectory dir = (VirtualDirectory)node;
		return dir.getNumberOfChildren();
	}
	
	private VirtualNode findNode(String path, boolean isWritable) {
		if (path.startsWith(Entity.SEPARATOR) && path.length() > 1)
			path = path.substring(1, path.length());
		
		boolean isNodeDirectory = path.endsWith(Entity.SEPARATOR);
		
		if (path.length() == 0)
			return rootDirectory;
		
		String[] tokens = path.split(Entity.SEPARATOR);
		
		VirtualNode currentNode = null;
		VirtualDirectory currentDirectory = rootDirectory;
		
		for (int i = 0;i<tokens.length;i++) {
			String itemName = tokens[i];			
			boolean isDirectory = i + 1 < tokens.length;
			
			currentNode = currentDirectory.getChild(itemName);
			
			if (currentNode == null) {
				if (isWritable) {
					if (isDirectory || isNodeDirectory)
						currentNode = new VirtualDirectory(itemName);
					else 
						currentNode = new VirtualNode(itemName);
					
					currentDirectory.addChild(itemName, currentNode);
				} else {
					return null;
				}
			}
			
			if (currentNode != null) {
				if (currentNode.isDirectory()) 
					currentDirectory = (VirtualDirectory)currentNode;
			} 
		}
		
		return currentNode;
	}
	
	
	public class VirtualNode {
		private String name;
		
		public VirtualNode(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isDirectory() {
			return false;
		}
	}
	
	public class VirtualDirectory extends VirtualNode {
		private Map<String, VirtualNode> children;
		
		public VirtualDirectory(String name) {
			super(name);
			children = new HashMap<String, VirtualNode>();
		}
		
		public VirtualNode addChild(String name, VirtualNode node) {
			return children.put(name, node);
		}
	
		public VirtualNode getChild(String name) {
			return children.get(name);
		}
		
		public List<String> getChildren() {
			List<String> list = new LinkedList<String>();
			for (VirtualNode node : children.values()) {
				if (node.isDirectory())
					list.add(node.getName() + Entity.SEPARATOR);
				else
					list.add(node.getName());
			}
			
			return list;
		}
		
		public int getNumberOfChildren() {
			return children.size();
		}
		
		public boolean isDirectory() {
			return true;
		}
	}
}
