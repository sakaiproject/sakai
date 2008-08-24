/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.impl.jcr.SakaiConstants;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.jcr.api.DAVConstants;
import org.sakaiproject.jcr.api.JCRConstants;
import org.sakaiproject.jcr.api.JCRService;

/**
 * <p>
 * BaseDbSingleStorage is a class that stores Resources (of some type) in a
 * database, <br />
 * provides locked access, and generally implements a services "storage" class.
 * The <br />
 * service's storage class can extend this to provide covers to turn Resource
 * and <br />
 * Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions:
 * <br /> - just the Resource Id field is enough to distinguish one Resource
 * from another <br /> - a resource's reference is based on no more than the
 * resource id <br /> - a resource's id cannot change.
 * </p>
 * <p>
 * In order to handle Unicode characters properly, the SQL statements executed
 * by this class <br />
 * should not embed Unicode characters into the SQL statement text; rather,
 * Unicode values <br />
 * should be inserted as fields in a PreparedStatement. Databases handle Unicode
 * better in fields.
 * </p>
 */
public class BaseJCRStorage
{

	private static final String COUNT_COLLECTION_MEMBERS = "count-members";

	private static final String COUNT_COLLECTION_COLLECTIONS = "count-collections";

	private static final String COUNT_COLLECTION_RESOURCES = "count-resources";

	private static final String GET_MEMBER_RESOURCES = "get-member-resources";

	private static final String GET_MEMBER_COLLECTIONS = "get-member-collections";

	private static final Log log = LogFactory.getLog(BaseJCRStorage.class);

	private static final String JCR_SAKAI_UUID = null;

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseJCRStorage.class);

	/** The xml tag name for the element holding each actual resource entry. */
	protected String m_resourceEntryTagName = null;

	/** The StorageUser to callback for new Resource and Edit objects. */
	protected LiteStorageUser m_user = null;

	/**
	 * Locks, keyed by reference, holding Connections (or, if locks are done
	 * locally, holding an Edit).
	 */
	protected Hashtable m_locks = null;

	/** If set, we treat reasource ids as case insensitive. */
	protected boolean m_caseInsensitive = false;

	private JCRService jcrService;

	private String nodeType;

	private Map<String, String> queryTerms;

	/**
	 * @param jcrService
	 * @param collectionUser
	 * @param string
	 */
	public BaseJCRStorage(JCRService jcrService, LiteStorageUser storageUser,
			String nodeType)
	{
		this.jcrService = jcrService;
		this.m_user = storageUser;
		this.nodeType = nodeType;

		queryTerms = new HashMap<String, String>();
		queryTerms.put("WHERE:IN_COLLECTION", "{0}/*");
		queryTerms.put("WHERELIKE:IN_COLLECTION", "{0}/*");
		queryTerms.put(COUNT_COLLECTION_MEMBERS, "{0}/*");
		queryTerms.put(COUNT_COLLECTION_COLLECTIONS, "{0}/element(*,nt:folder)");
		queryTerms.put(COUNT_COLLECTION_RESOURCES, "{0}/element(*,nt:file)");
		queryTerms.put(GET_MEMBER_COLLECTIONS, "{0}/element(*,nt:folder)");
		queryTerms.put(GET_MEMBER_RESOURCES, "{0}/element(*,nt:file)");
	}

	/**
	 * Open and be ready to read / write.
	 */
	public void open()
	{
		if (isEmpty())
		{
			Session currentSession = null;
			boolean reset = false;
			try
			{
				log.debug("--------------------------- Start Repository Populate");
				currentSession = jcrService.setSession(null);
				log.debug("Got Current Session as   " + currentSession);

				reset = true;

				Session s = jcrService.login();
				log.debug("Prepopulating Nodes in repo");
				Node n = createNode("/", JCRConstants.NT_FOLDER);
				n.save();
				for (Iterator<String> i = m_user.startupNodes(); i.hasNext();)
				{

					String[] ndef = i.next().split(";");
					log.debug("       Creating " + ndef[0] + " as a " + ndef[1]);
					n = createNode(ndef[0], ndef[1]);
					if (JCRConstants.NT_FOLDER.equals(ndef[1]))
					{
						n.setProperty(SakaiConstants.CHEF_IS_COLLECTION, "true");
					}
					else
					{
						n.setProperty(SakaiConstants.CHEF_IS_COLLECTION, "false");
					}
					n.setProperty(SakaiConstants.SAKAI_CONTENT_PRIORITY, "2");
					n.setProperty(SakaiConstants.CHEF_CREATOR, "admin");
					n.setProperty(SakaiConstants.SAKAI_ACCESS_MODE, "inherited");
					n.setProperty(SakaiConstants.CHEF_MODIFIEDBY, "admin");
					n.setProperty(SakaiConstants.SAKAI_HIDDEN, "false");
					n.setProperty(DAVConstants.DAV_DISPLAYNAME, ndef[2]);
					// n.setProperty(DAVConstants.DAV_GETLASTMODIFIED, davDate);
					// n.setProperty(DAVConstants.DAV_CREATIONDATE, davDate);
					n.save();
				}
				log.debug("Session is " + s);
				// TODO: clean up s.exportDocumentView("/sakai", System.out,
				// true,
				// false);
				s.save();
				s.logout();
				log.debug("Creating Root Node: SUCCESS");

			}
			catch (RepositoryException e)
			{
				log.error("Unable to create root node cause:" + e.getMessage());
				throw new RuntimeException("Unable to create root node cause:"
						+ e.getMessage(), e);
			}
			catch (TypeException e)
			{
				log.error("Unable to create root node cause:" + e.getMessage());
				throw new RuntimeException("Unable to create root node cause:"
						+ e.getMessage(), e);
			}
			finally
			{
				if (reset)
				{
					try
					{
						jcrService.setSession(currentSession);
						currentSession = jcrService.getSession();
						for (Iterator<String> i = m_user.startupNodes(); i.hasNext();)
						{
							String[] ndef = i.next().split(";");
							log.debug("       Checking " + ndef[0] + " as a " + ndef[1]);
							Node n = getNodeById(ndef[0]);
							if (n == null)
							{
								log.fatal("Didnt find " + ndef[0] + " after populate ");
								System.exit(1);
							}
							else
							{
								log.debug("     Got " + n);
							}
						}
						log.debug("Repo Is Empty " + isEmpty());
						log
								.debug("--------------------------- Repository Populate Complete OK");
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}

		}

	}

	/**
	 * Close.
	 */
	public void close()
	{
	}

	/**
	 * Read one Resource from xml
	 * 
	 * @param xml
	 *        An string containing the xml which describes the resource.
	 * @return The Resource object created from the xml.
	 */
	protected Entity readResource(Node n)
	{
		if (n == null)
		{
			return null;
		}

		try
		{

			Entity e = m_user.newResource(n);
			return e;
		}
		catch (Exception e)
		{
			log.warn("readResource(): ", e);
			return null;
		}
	}

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	public boolean checkResource(String id)
	{
		Node n = getNodeById(id);
		if (n != null)
		{
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (nodeType.equals(nt.getName()))
				{
					return true;
				}
			}
			catch (RepositoryException e)
			{
				log.error("Failed ", e);
			}
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	private Node getNodeById(String id)
	{
		try
		{
			Session session = jcrService.getSession();
			Item i = session.getItem(m_user.convertId2Storage(id));

			if (i != null && i.isNode())
			{
				return (Node) i;
			}
			else
			{
				log.debug("Item is not a node " + i);
			}
		}
		catch (PathNotFoundException ex)
		{
		}
		catch (RepositoryException re)
		{
			log.debug("Node Not Found " + id, re);
			log.warn("Node Not Found " + id + " cause:" + re.getMessage());

		}
		return null;
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(String id)
	{

		return readResource(getNodeById(id));

	}

	public boolean isEmpty()
	{
		Node n = getNodeById("/");
		if (n == null)
		{
			return true;
		}
		try
		{
			boolean hasnodes = n.hasNodes();
			log.debug("Root has nodes " + hasnodes);
			return !hasnodes;
		}
		catch (RepositoryException e)
		{
			log.warn("Is Empty failed with " + e.getMessage() + " assuming empty");
			return true;
		}
	}

	public List getAllResources()
	{

		throw new UnsupportedOperationException("Not Available");
	}

	public List getAllResources(int first, int last)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	public int countAllResources()
	{
		throw new UnsupportedOperationException("Not Available");
	}

	public int countSelectedResourcesWhere(String sqlWhere)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Get all Resources where the given field matches the given value.
	 * 
	 * @param field
	 *        The db field name for the selection.
	 * @param value
	 *        The value to select.
	 * @return The list of all Resources that meet the criteria.
	 */
	@SuppressWarnings("unchecked")
	public List getAllResourcesWhere(String field, String value)
	{
		List l = new ArrayList();
		if ("IN_COLLECTION".equals(field))
		{
			Node n = getNodeById(value);
			addMembers(n, l, false);
		}
		else
		{
			log.error("Unknown Type " + field);
		}
		return l;
	}

	public List getAllResourcesWhereLike(String field, String value)
	{
		List l = new ArrayList();
		if ("IN_COLLECTION".equals(field))
		{
			Node n = getNodeById(value);
			addMembers(n, l, true);

		}
		else
		{
			log.error("Unknown Type " + field);
		}
		return l;
	}

	/**
	 * @param value
	 * @param l
	 */
	private void addMembers(Node n, List l, boolean recurse)
	{
		if (n == null)
		{
			return;
		}
		try
		{
			for (NodeIterator ni = n.getNodes(); ni.hasNext();)
			{
				Node nn = ni.nextNode();
				if (nn != null)
				{
					try
					{
						String ntname = nn.getPrimaryNodeType().getName();
						if (nodeType.equals(ntname))
						{
							l.add(readResource(nn));
						}
						else if (recurse && JCRConstants.NT_FOLDER.equals(ntname))
						{
							addMembers(nn, l, recurse);
						}
					}
					catch (RepositoryException re)
					{

					}
				}
			}
		}
		catch (RepositoryException re)
		{

		}
	}

	/**
	 * @param string
	 * @param value
	 * @return
	 */
	private List getNodeList(String key, Object[] value)
	{
		NodeIterator ni = getNodeIterator(key, value);
		if (ni == null)
		{
			return new ArrayList();
		}
		List<Entity> al = new ArrayList<Entity>();
		for (; ni.hasNext();)
		{
			Node n = ni.nextNode();
			al.add(m_user.newResource(n));
		}
		return al;
	}

	private NodeIterator getNodeIterator(String key, Object[] value)
	{
		String queryFormat = queryTerms.get(key);
		if (queryFormat == null)
		{
			throw new UnsupportedOperationException("No List Option for " + key);
		}
		String qs = MessageFormat.format(queryFormat, value);
		try
		{
			Session session = jcrService.getSession();
			Workspace w = session.getWorkspace();
			QueryManager qm = w.getQueryManager();

			Query q = qm.createQuery(qs, Query.XPATH);
			QueryResult qr = q.execute();

			NodeIterator ni = qr.getNodes();
			log.debug(" Executing Query [" + qs + "] gave [" + ni.getSize() + "] results");
			return ni;
		}
		catch (RepositoryException e)
		{
			log.debug("Failed to get List using query [" + qs + "]", e);
			return null;
		}

	}

	/**
	 * Get selected Resources, filtered by a test on the id field
	 * 
	 * @param filter
	 *        A filter to select what gets returned.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResources(final Filter filter)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Get selected Resources, using a supplied where clause
	 * 
	 * @param sqlWhere
	 *        The SQL where clause.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResourcesWhere(String sqlWhere)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param id
	 *        The id.
	 * @param others
	 *        Other fields for the newResource call
	 * @return The locked Resource object with this id, or null if the id is in
	 *         use.
	 */
	public Edit putResource(String id, Object[] others)
	{
		try
		{
			Node n;
			n = createNode(id, this.nodeType);
			if (n == null)
			{
				return null;
			}
			return editResource(id);
		}
		catch (TypeException e)
		{
			log.error("Incorrect Node Type", e);
		}
		return null;
	}

	/**
	 * store the record in content_resource_delete table along with
	 * resource_uuid and date
	 */
	public Edit putDeleteResource(String id, String uuid, String userId, Object[] others)
	{
		return null;
	}

	/** update XML attribute on properties and remove locks */
	public void commitDeleteResource(Edit edit, String uuid)
	{
		log
				.warn("commitDeleteResource is not currently Implemented: No Possible since the deleted UUID cannot be shared ");
	}

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be
	 * gotten.
	 * 
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot
	 *         be locked.
	 */
	public Edit editResource(String id)
	{
		Node n = getNodeById(id);
		if (n == null)
		{
			return null;
		}
		try
		{
			if (n.lock(true, true) == null)
			{
				return null;
			}
		}
		catch (UnsupportedRepositoryOperationException e)
		{
			log.warn("Operation Not Supported ", e);
		}
		catch (RepositoryException e)
		{
			log.warn("Lock Failed ", e);
			return null;
		}
		return m_user.newResourceEdit(n);
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to commit.
	 */
	public void commitResource(Edit edit)
	{
		Node n = getNodeById(edit.getId());
		if (n == null)
		{
			log.error("Cant Commit since node cant be found for id " + edit.getId());
		}
		else
		{
			m_user.commit(edit, n);
			try
			{
				n.save();
				if (n.isLocked())
				{
					n.unlock();
				}
			}
			catch (RepositoryException e)
			{
				log.error("Failed to save resource ", e);
			}
		}
	}

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelResource(Edit edit)
	{
		if (jcrService.hasActiveSession())
		{
			Node n = getNodeById(edit.getId());
			try
			{
				n.refresh(false);
			}
			catch (RepositoryException e)
			{
				log.warn("Failed to cancel Edit ", e);
			}
			try
			{
				if (n.isLocked())
				{
					n.unlock();
				}
			}
			catch (RepositoryException e)
			{
				log.warn("Failed to un Lock ", e);
			}
		}
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Edit edit)
	{
		Node n = getNodeById(edit.getId());
		try
		{
			n.remove();
		}
		catch (RepositoryException e)
		{
			log.debug("Failed to cancel Edit ", e);
		}
		try
		{
			n.unlock();
		}
		catch (RepositoryException e)
		{
			log.debug("Failed to un Lock. on Some JCR Impls this is Ok ", e);
		}
	}

	/**
	 * Create a new node. Nodes are of the form
	 * nt:folder/nt:folder/nt:folder/nt:file nt:folders have properties nt:files
	 * have properties nt:files have a nt:resource subnode
	 * 
	 * @param id
	 * @param collection
	 * @return
	 * @throws TypeException
	 */
	private Node createNode(String id, String type) throws TypeException
	{
		Node node = null;
		try
		{
			String absPath = m_user.convertId2Storage(id);
			Session s = jcrService.getSession();
			Node n = getNodeFromSession(s, absPath);
			// the node might already exist
			if (n != null)
			{
				return n;
			}

			String vpath = getParentPath(absPath);
			while (n == null && !"/".equals(vpath))
			{
				n = getNodeFromSession(s, vpath);
				if (n == null)
				{
					vpath = getParentPath(vpath);
				}
				else
				{
					log.debug("Got Path " + vpath + " as " + n);
				}
			}
			if (n == null)
			{
				n = s.getRootNode();
			}
			log.debug("VPath is " + vpath);
			String relPath = absPath.substring(vpath.length());
			// Node rootNode = s.getRootNode();
			if (relPath.startsWith("/"))
			{
				relPath = relPath.substring(1);
			}

			String[] pathElements = relPath.split("/");
			log.debug("RelPath is " + relPath + " split into " + pathElements.length
					+ " elements ");
			for (String pathel : pathElements)
			{
				log.debug("       Path Element is [" + pathel + "]");
			}

			Node currentNode = n;
			for (int i = 0; i < pathElements.length; i++)
			{
				try
				{
					log.debug("Getting " + pathElements[i] + " under " + currentNode);
					currentNode = currentNode.getNode(pathElements[i]);
					if (!currentNode.isNodeType(JCRConstants.NT_FOLDER)
							&& !currentNode.isNodeType(JCRConstants.NT_BASE))
					{
						throw new TypeException(
								"Cant create collection or a folder inside a node that is not a folder "
										+ currentNode.getPath());
					}

				}
				catch (PathNotFoundException pnfe)
				{
					if ( log.isDebugEnabled() ) 
					    log.debug("Not Found " + pnfe.getMessage() + " ");
					if (i < pathElements.length - 1
							|| JCRConstants.NT_FOLDER.equals(type))
					{
					        if ( log.isDebugEnabled() ) 
						   log.debug("Adding Node " + pathElements[i] + " as " + type
								+ " to " + currentNode.getPath());
						Node newNode = currentNode.addNode(pathElements[i],
								JCRConstants.NT_FOLDER);
						populateFolder(newNode);
						currentNode.save();
						currentNode = newNode;
					        if ( log.isDebugEnabled() ) 
						   log.debug("Adding Node Complete");
					}
					else
					{
					        if ( log.isDebugEnabled() ) 
						    log.debug("Adding Node " + pathElements[i] + " as " + type
								+ " to " + currentNode.getPath());
						Node newNode = currentNode.addNode(pathElements[i],
								JCRConstants.NT_FILE);
						populateFile(newNode);
						currentNode.save();
						currentNode = newNode;
					        if ( log.isDebugEnabled() ) 
						    log.debug("Adding Node Complete");

					}
				}
				if (currentNode.isCheckedOut())
				{
					currentNode.save();
				}
			}
			node = currentNode;
			if (node == null)
			{
				log.error("Failed to create Node " + absPath + " got " + node);
				throw new Error("Failed to create node " + absPath + " got " + node);
			}
			else if (!absPath.equals(node.getPath()))
			{

				log.error("Failed to create Node " + absPath + " got" + node.getPath());
				throw new Error("Failed to create node " + absPath + " got "
						+ node.getPath());
			}

		}
		catch (RepositoryException rex)
		{
			log.warn("Unspecified Repository Failiure ", rex);
			log.error("Unspecified Repository Failiure " + rex.getMessage());
		}
		return node;

	}

	/**
	 * @param absPath
	 * @return
	 */
	private String getParentPath(String absPath)
	{
		int pre = absPath.lastIndexOf("/");
		if (pre > 0)
		{
			String parentPath = absPath.substring(0, pre);
                        if ( log.isDebugEnabled() )  
			   log.debug("Parent path is [" + parentPath + "]");
			return parentPath;
		}
		return "/";
	}

	/**
	 * @param s
	 * @param id
	 * @return
	 * @throws TypeException
	 * @throws RepositoryException
	 * @throws RepositoryException
	 */
	private Node getNodeFromSession(Session s, String id) throws TypeException,
			RepositoryException
	{
		Item i;
		try
		{
			i = s.getItem(id);
		}
		catch (PathNotFoundException e)
		{
			log.debug("getNodeFromSession: Node Does Not Exist :" + id);
			return null;
		}
		Node n = null;
		if (i != null)
		{
			if (i.isNode())
			{
				n = (Node) i;
			}
			else
			{
				throw new TypeException("Path does not point to a node");
			}
		}
		return n;
	}

	private void populateFile(Node node) throws RepositoryException
	{
		// JCR Types
		if (jcrService.needsMixin(node, JCRConstants.MIX_REFERENCEABLE))
		{
			node.addMixin(JCRConstants.MIX_REFERENCEABLE);
		}
		if (jcrService.needsMixin(node, JCRConstants.MIX_LOCKABLE))
		{
			node.addMixin(JCRConstants.MIX_LOCKABLE);
		}
		if (jcrService.needsMixin(node, JCRConstants.MIX_SAKAIPROPERTIES))
		{
			node.addMixin(JCRConstants.MIX_SAKAIPROPERTIES);
		}
		Node resource = node.addNode(JCRConstants.JCR_CONTENT, JCRConstants.NT_RESOURCE);
		resource.setProperty(JCRConstants.JCR_LASTMODIFIED, new GregorianCalendar());
		resource.setProperty(JCRConstants.JCR_MIMETYPE, "application/octet-stream");
		resource.setProperty(JCRConstants.JCR_DATA, "");
		resource.setProperty(JCRConstants.JCR_ENCODING, "UTF-8");

	}

	private void populateFolder(Node node) throws RepositoryException
	{
		// JCR Types
		// TODO: perhpase
		log.debug("Doing populate Folder");
		if (jcrService.needsMixin(node, JCRConstants.MIX_LOCKABLE))
		{
			node.addMixin(JCRConstants.MIX_LOCKABLE);
		}
		if (jcrService.needsMixin(node, JCRConstants.MIX_REFERENCEABLE))
		{
			node.addMixin(JCRConstants.MIX_REFERENCEABLE);
		}
		if (jcrService.needsMixin(node, JCRConstants.MIX_SAKAIPROPERTIES))
		{
			node.addMixin(JCRConstants.MIX_SAKAIPROPERTIES);
		}

		node.setProperty(JCRConstants.JCR_LASTMODIFIED, new GregorianCalendar());

	}

	/**
	 * @param collectionId
	 * @return
	 */
	public Collection<String> getMemberCollectionIds(String collectionId)
	{
		return getMembersOfType(collectionId, JCRConstants.NT_FOLDER);

	}

	/**
	 * @param collectionId
	 * @param nt_folder
	 * @return
	 */
	private List<String> getMembersOfType(String collectionId, String type)
	{
		List<String> l = new ArrayList<String>();
		try
		{
			Node n = getNodeById(collectionId);
			if (n == null)
			{
				return l;
			}
			NodeType nt = n.getPrimaryNodeType();
			if (JCRConstants.NT_FOLDER.equals(nt.getName()))
			{
				NodeIterator ni = n.getNodes();
				for (; ni.hasNext();)
				{
					try
					{
						Node tn = ni.nextNode();
						if (type == null
								|| type.equals(tn.getPrimaryNodeType().getName()))
						{
							l.add(m_user.convertStorage2Id(tn.getPath()));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Cant get Path " + e.getMessage());
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to count collection " + e.getMessage());
		}
		return l;
	}

	/**
	 * @param collectionId
	 * @return
	 */
	public Collection<String> getMemberResourceIds(String collectionId)
	{
		return getMembersOfType(collectionId, JCRConstants.NT_FILE);
	}

	/**
	 * @param collectionId
	 * @return
	 */
	public int getMemberCount(String collectionId)
	{
		try
		{
			Node n = getNodeById(collectionId);
			if (n == null)
			{
				return 0;
			}
			NodeType nt = n.getPrimaryNodeType();
			if (JCRConstants.NT_FOLDER.equals(nt.getName()))
			{
				
				int c = (int) n.getNodes().getSize();
				if ( c == -1 ) {
					c = 0;
					for ( NodeIterator ni = n.getNodes(); ni.hasNext(); ) {
						ni.next();
						c++;
					}
				}
				log.debug(" Collection " + collectionId + " has " + c + " members ");
				return c;
			}
		}
		catch (Exception e)
		{
			log.error("Failed to count collection");
		}
		return 0;
	}

	/**
	 * @param resourceId
	 * @param uuid
	 */
	public void setResourceUuid(String resourceId, String uuid)
	{
		// Node n = getNodeById(resourceId);
		// n.setProperty(JCR_SAKAI_UUID, uuid);
		// n.save();
	}

	/**
	 * @param thisCollection
	 * @param new_folder_id
	 * @return
	 * @throws IdUnusedException
	 *         When the target folder does not exit
	 * @throws TypeException
	 *         When the target is not a folder
	 * @throws IdUsedException
	 *         When a unique target cannot be found
	 * @throws ServerOverloadException
	 *         Failed to move collection due to repository error
	 */
	public String moveCollection(ContentCollectionEdit thisCollection,
			String new_folder_id) throws IdUnusedException, TypeException,
			IdUsedException, ServerOverloadException
	{
		try
		{
			String parentFolderId = isolateContainingId(new_folder_id);
			Node n = getNodeById(parentFolderId);
			if (n == null)
			{
				throw new IdUnusedException(
						"The target parent folder of the move does not exist "
								+ new_folder_id);
			}
			if (!JCRConstants.NT_FOLDER.equals(n.getPrimaryNodeType().getName()))
			{
				throw new TypeException("The target of the move " + new_folder_id
						+ " is not a folder ");
			}

			String newName = isolateName(new_folder_id);

			boolean found = n.hasNode(m_user.convertId2Storage(newName));
			// now see of the folder exists
			for (int i = 0; i < 100 && found; i++)
			{
				found = n.hasNode(m_user.convertId2Storage(newName + "-" + i));
				if (!found)
				{
					newName = newName + "-" + i;
				}
			}
			if (!found)
			{
				throw new IdUsedException("Unable to create a new copy of " + newName
						+ " in " + new_folder_id);
			}
			String newPath = parentFolderId + "/" + newName;

			// move the folder
			if (thisCollection instanceof BaseJCRCollectionEdit)
			{
				BaseJCRCollectionEdit bce = (BaseJCRCollectionEdit) thisCollection;
				n.getSession().move(bce.getNode().getPath(),
						m_user.convertId2Storage(newPath));
			}
			else
			{
				Node newNode = createNode(newPath, JCRConstants.NT_FOLDER);
				m_user.commit(thisCollection, newNode);
			}
			return newPath;
		}
		catch (RepositoryException e)
		{
			log.error("Failed to move collection to " + new_folder_id, e);
			throw new ServerOverloadException(new_folder_id);
		}
	}

	/**
	 * Find the containing collection id of a given resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the containing collection id.
	 */
	protected String isolateContainingId(String id)
	{
		// take up to including the last resource path separator, not counting
		// one at the very end if there
		return id.substring(0, id.lastIndexOf('/', id.length() - 2) + 1);

	} // isolateContainingId

	protected String isolateName(String id)
	{
		if (id == null) return null;
		if (id.length() == 0) return null;

		// take after the last resource path separator, not counting one at the
		// very end if there
		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1,
				(lastIsSeparator ? id.length() - 1 : id.length()));

	} // isolateName

	/**
	 * @param thisResource
	 * @param new_id
	 * @return
	 * @throws IdUnusedException
	 * @throws TypeException
	 * @throws IdUsedException
	 * @throws ServerOverloadException
	 */
	public String moveResource(ContentResourceEdit thisResource, String new_id)
			throws IdUnusedException, TypeException, IdUsedException,
			ServerOverloadException
	{
		try
		{
			String parentFolderId = isolateContainingId(new_id);
			Node n = getNodeById(parentFolderId);
			if (n == null)
			{
				throw new IdUnusedException(
						"The target parent folder of the move does not exist " + new_id);
			}
			if (!JCRConstants.NT_FOLDER.equals(n.getPrimaryNodeType().getName()))
			{
				throw new TypeException("The target of the move " + new_id
						+ " is not a folder ");
			}

			String newName = isolateName(new_id);
			String basename = newName;
			String extension = "";
			int index = newName.lastIndexOf(".");
			if (index >= 0)
			{
				basename = newName.substring(0, index);
				extension = newName.substring(index);
			}

			boolean found = n.hasNode(m_user.convertId2Storage(newName));
			// now see of the folder exists
			for (int i = 0; i < 100 && found; i++)
			{
				found = n.hasNode(m_user.convertId2Storage(basename + "-" + i + "."
						+ extension));
				if (!found)
				{
					newName = newName + "-" + i + "." + extension;
				}
			}
			if (!found)
			{
				throw new IdUsedException("Unable to create a new copy of " + newName
						+ " in " + new_id);
			}
			String newPath = parentFolderId + "/" + newName;

			// move the folder
			if (thisResource instanceof BaseJCRResourceEdit)
			{
				BaseJCRResourceEdit bcr = (BaseJCRResourceEdit) thisResource;
				n.getSession().move(bcr.getNode().getPath(),
						m_user.convertId2Storage(newPath));
			}
			else
			{
				Node newNode = createNode(newPath, JCRConstants.NT_FILE);
				Session s = n.getSession();
				ValueFactory vf = s.getValueFactory();
				InputStream is = thisResource.streamContent();
				Value v = vf.createValue(is);
				newNode.setProperty(JCRConstants.JCR_DATA, v);
				m_user.commit(thisResource, newNode);
			}
			return newPath;
		}
		catch (RepositoryException e)
		{
			log.error("Failed to move respource to " + new_id, e);
			throw new ServerOverloadException(new_id);
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public String getUuid(String id)
	{
		Node n = getNodeById(id);
		if (n == null)
		{
			return null;
		}
		try
		{
			return n.getUUID();
		}
		catch (RepositoryException e)
		{
			log.error(" Cant get UUID on " + id + " returning null cause:"
					+ e.getMessage());
		}
		return null;
	}

}
