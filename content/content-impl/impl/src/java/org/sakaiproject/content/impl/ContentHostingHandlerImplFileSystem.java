package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides a READ ONLY view of a filesystem through virtual content hosting.
 * 
 * @author johnf
 */
public class ContentHostingHandlerImplFileSystem implements ContentHostingHandler
{
	public final static String XML_NODE_NAME = "mountpoint";

	public final static String XML_ATTRIBUTE_NAME = "path";

	public final static boolean SHOW_FULL_PATHS = false; /* list full paths in the file list view */

	private ContentHostingHandlerResolver contentHostingHandlerResolver = null;

	public void cancel(ContentCollectionEdit edit)
	{
	} /* READ-ONLY VIEW */

	public void cancel(ContentResourceEdit edit)
	{
	} /* READ-ONLY VIEW */

	public void commit(ContentCollectionEdit edit)
	{
	} /* READ-ONLY VIEW */

	public void commit(ContentResourceEdit edit)
	{
	} /* READ-ONLY VIEW */

	public void commitDeleted(ContentResourceEdit edit, String uuid)
	{
	} /* READ-ONLY VIEW */

	public List getCollections(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionFileSystem))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionFileSystem ccfs = (ContentCollectionFileSystem) cc;
		List l = ccfs.getMembers();
		java.util.ArrayList collections = new java.util.ArrayList(l.size());
		for (java.util.Iterator i = l.listIterator(); i.hasNext();)
		{
			String id = (String) i.next();
			ContentEntityFileSystem cefs = resolveToFileOrDirectory(ccfs.realParent, ccfs.basePath, id
					.substring(ccfs.realParent.getId().length() + 1), this);
			if (cefs instanceof ContentCollectionFileSystem) collections.add(cefs.wrap());
		}
		return collections;
	}

	public ContentCollectionEdit getContentCollectionEdit(String id)
	{ /* READ-ONLY VIEW */
		System.out.println("getContentCollectionEdit");
		return null;
	}

	public ContentResourceEdit getContentResourceEdit(String id)
	{ /* READ-ONLY VIEW */
		System.out.println("getContentResourceEdit");
		return null;
	}

	public List getFlatResources(ContentEntity ce)
	{
		System.out.println("getFlatResources");
		return null;
	}

	public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException
	{
		if (!(resource instanceof ContentResourceFileSystem)) return null;
		ContentResourceFileSystem crfs = (ContentResourceFileSystem) resource;
		return crfs.getContent();
	}

	public List getResources(ContentCollection collection)
	{
		ContentEntity cc = collection.getVirtualContentEntity();
		if (!(cc instanceof ContentCollectionFileSystem))
		{
			return null; // this is not the correct handler for this resource -- serious problems!
		}
		ContentCollectionFileSystem ccfs = (ContentCollectionFileSystem) cc;
		List l = ccfs.getMemberResources();
		return l;
	}
	protected ContentEntityFileSystem resolveToFileOrDirectory(ContentEntity realParent, String basePath, String relativePath,
			ContentHostingHandler chh)
	{
		// return a file (resource) or a directory (collection) as appropriate
		while (relativePath.length() > 0 && relativePath.charAt(0) == '/')
			relativePath = relativePath.substring(1);
		while (relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) == '/')
			relativePath = relativePath.substring(0, relativePath.length() - 1);
		relativePath = "/" + relativePath;
		if (basePath.charAt(basePath.length() - 1) == '/') basePath = basePath.substring(0, basePath.length() - 1);
		String newpath = basePath + relativePath;
		java.io.File f = new java.io.File(newpath);
		if (f.isDirectory())
		{
			ContentEntityFileSystem cefs = new ContentCollectionFileSystem(realParent, basePath, relativePath, chh);
			cefs.wrap();
			return cefs;
		}
		else
		{
			ContentEntityFileSystem cefs = new ContentResourceFileSystem(realParent, basePath, relativePath, chh);
			cefs.wrap();
			return cefs;
		}
	}


	public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId)
	{
		// Algorithm: get the mount point from the XML file represented by 'edit'
		// construct a new ContentEntityFileSystem and return it
		try
		{
			byte[] xml = ((ContentResource) edit).getContent();
			if (xml == null) return null;
			javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			if (db == null) return null;
			org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream(xml));
			if (d == null) return null;
			org.w3c.dom.Node node_mountpoint = null;
			org.w3c.dom.NodeList nl = d.getChildNodes();
			for (int j = 0; j < nl.getLength(); ++j)
				if (nl.item(j).getNodeName() != null && nl.item(j).getNodeName().equals(XML_NODE_NAME))
				{
					node_mountpoint = nl.item(j);
					break;
				}
			if (node_mountpoint == null) return null;
			org.w3c.dom.Node node_basepath = node_mountpoint.getAttributes().getNamedItem(XML_ATTRIBUTE_NAME);
			if (node_basepath == null) return null;
			final String basepath = node_basepath.getNodeValue();
			if (basepath == null || basepath.equals("")) return null; // invalid mountpoint specification

			String relativePath = finalId.substring(edit.getId().length());
			ContentEntityFileSystem cefs = resolveToFileOrDirectory(edit, basepath, relativePath, this);
			Edit ce = cefs.wrap();
			if (ce == null) return null; // happens when the requested URL requires a log on but the user is not logged on
			return (ContentEntity) ce;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
	{ /* READ-ONLY VIEW */
		return null;
	}

	public void removeCollection(ContentCollectionEdit edit)
	{
	} /* READ-ONLY VIEW */

	public void removeResource(ContentResourceEdit edit)
	{
	} /* READ-ONLY VIEW */

	public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
	{
		ContentEntity ce = resource.getVirtualContentEntity();
		if (!(ce instanceof ContentResourceFileSystem)) return null;
		ContentResourceFileSystem crfs = (ContentResourceFileSystem) ce;
		return crfs.streamContent();
	}

	/**
	 * An abstract superclass representing virtual content entities corresponding to filesystem entities.
	 * 
	 * @author johnf (johnf@caret.cam.ac.uk)
	 */
	public abstract class ContentEntityFileSystem implements ContentEntity
	{
		/**
		 * Representation of the file/directory represented by this entity. This is intended to be used as a virtual content entity.
		 */
		protected java.io.File file;

		/**
		 * A stored copy of the string filename that was used to construct this object initially. This name might be out-of-date if the resource has been renamed since creation.
		 */
		protected String relativePath;

		protected String basePath;

		protected ContentEntity realParent;

		/**
		 * Object reference to the content hosting handler which looks after this virtual content resource.
		 */
		protected ContentHostingHandler chh;

		/**
		 * Wrapped version of itself (a base content edit)
		 */
		protected Edit wrappedMe;

		abstract protected Edit wrap();

		abstract protected void setVirtualProperties();

		/**
		 * ID of the parent collection object
		 */
		protected String parent;

		protected String parentRelativePath;

		/**
		 * Constructs a new instance
		 * 
		 * @param filename -
		 *        filename or URI of the file or directory to represent
		 */
		public ContentEntityFileSystem(ContentEntity realParent, String basePath, String relativePath, ContentHostingHandler chh)
		{
			this.realParent = realParent;
			this.basePath = basePath;
			this.relativePath = relativePath;
			this.chh = chh;
			this.file = new java.io.File(basePath + relativePath);

			int lastSlash = relativePath.lastIndexOf('/');
			if (lastSlash < 1)
			{
				/*
				 * PROBLEM: getContainingCollection must return a Collection but what do we want to return when you recurse out of the top of the virtual object tree? We can't return the realParent since that is not a Collection. One choice is to make the
				 * root of the virtual tree a parent of itself, and that is what we do. Other than changing the return type of getContainingCollection there is no nice solution to this problem.
				 */
				this.parentRelativePath = "/"; // root cyclically parents itself :-S
				parent = realParent.getId();
			}
			else
			{
				this.parentRelativePath = relativePath.substring(0, lastSlash);
				parent = realParent.getId() + parentRelativePath;
			}
		}


		/**
		 * @return enclosing collection
		 */
		public ContentCollection getContainingCollection()
		{
			return (ContentCollection) resolveToFileOrDirectory(realParent, basePath, parentRelativePath, chh);
		}

		/**
		 * @return Object reference to the content hosting handler which looks after this virtual content resource.
		 */
		public ContentHostingHandler getContentHandler()
		{
			return chh;
		}

		public void setContentHandler(ContentHostingHandler chh)
		{
			this.chh = chh;
		} // re-parent a virtual entity?! you probably don't want to call this!

		public ContentEntity getVirtualContentEntity()
		{
			return this;
		} // method is used by BaseResourceEdit, not really useful here

		public void setVirtualContentEntity(ContentEntity ce)
		{
		} // method is used by BaseResourceEdit, not really useful here

		/**
		 * Returns true unless the represented file is a directory. Note that this returns true if the file/directory does not exist or no-longer exists. The response is not cached and will reflect the current state of the filesystem represented at all
		 * times.
		 * 
		 * @return true if the path/URI is a file, false if it exists but is a non-file object, true if inaccessible/no-longer exists.
		 */
		abstract public boolean isResource();

		/**
		 * Returns true if the represented path is a directory. The response is not cached and will reflect the current state of the filesystem represented at all times.
		 * 
		 * @return true if path/URI is a directory, false otherwise.
		 */
		abstract public boolean isCollection();

		public ContentEntity getMember(String nextId)
		{
			String newpath = nextId.substring(realParent.getId().length()); // cut real parent's ID off the start of the string
			return resolveToFileOrDirectory(realParent, basePath, newpath, chh);
		}

		/* Junk required by GroupAwareEntity superinterface */
		public Collection getGroups()
		{
			return realParent.getGroups();
		}

		public Collection getGroupObjects()
		{
			return realParent.getGroupObjects();
		}

		public AccessMode getAccess()
		{
			return realParent.getAccess();
		}

		public Collection getInheritedGroups()
		{
			return realParent.getInheritedGroups();
		}

		public Collection getInheritedGroupObjects()
		{
			return realParent.getInheritedGroupObjects();
		}

		public AccessMode getInheritedAccess()
		{
			return realParent.getInheritedAccess();
		}

		public Time getReleaseDate()
		{
			return realParent.getReleaseDate();
		}

		public Time getRetractDate()
		{
			return realParent.getRetractDate();
		}

		public boolean isHidden()
		{
			return realParent.isHidden();
		}

		public boolean isAvailable()
		{
			return realParent.isAvailable();
		}

		/* Junk required by Entity superinterface */
		private String join(String base, String extension)
		{ // joins two strings with precisely one / between them
			while (base.length() > 0 && base.charAt(base.length() - 1) == '/')
				base = base.substring(0, base.length() - 1);
			while (extension.length() > 0 && extension.charAt(0) == '/')
				extension = extension.substring(1);
			return base + "/" + extension;
		}

		public String getUrl()
		{
			return join(realParent.getUrl(), relativePath);
		}

		public String getReference()
		{
			return join(realParent.getReference(), relativePath);
		} // wild guess

		public String getUrl(String rootProperty)
		{
			return join(realParent.getUrl(rootProperty), relativePath);
		}

		public String getReference(String rootProperty)
		{
			return join(realParent.getReference(rootProperty), relativePath);
		} // wild guess

		public String getId()
		{
			return join(realParent.getId(), relativePath);
		}

		public ResourceProperties getProperties()
		{
			return realParent.getProperties();
		}

		public Element toXml(Document doc, Stack stack)
		{
			return realParent.toXml(doc, stack);
		}
	}

	/**
	 * A concrete class representing virtual content entities corresponding to filesystem files.
	 * 
	 * @author johnf (johnf@caret.cam.ac.uk)
	 */
	public class ContentResourceFileSystem extends ContentEntityFileSystem implements ContentResource
	{
		public ContentResourceFileSystem(ContentEntity realParent, String basePath, String relativePath, ContentHostingHandler chh)
		{
			super(realParent, basePath, relativePath, chh);
		}

		protected Edit wrap()
		{
			if (wrappedMe == null) wrappedMe = contentHostingHandlerResolver.newResourceEdit(getId());
			if (wrappedMe != null)
			{
				// link it back to this CHH
				((ContentEntity) wrappedMe).setContentHandler(chh);
				((ContentEntity) wrappedMe).setVirtualContentEntity(this);
				setVirtualProperties();
			}
			return wrappedMe;
		}

		protected void setVirtualProperties()
		{
			// set the properties required for a sensible display in the resources list view
			String tmp;
			if (this.relativePath.equals("/"))
				tmp = this.basePath;
			else
			{
				if (SHOW_FULL_PATHS)
					tmp = this.relativePath;
				else
					tmp = this.relativePath.substring(this.relativePath.lastIndexOf("/", this.relativePath.length() - 2))
							.substring(1);
			}
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, tmp);
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not supported on all filesystems
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE,
					new java.util.Date(this.file.lastModified()).toString());
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, this.basePath + this.relativePath);
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION, this.relativePath);

			// resource-only properties
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CONTENT_LENGTH, "" + this.getContentLengthLong());
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isResource()
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isCollection()
		{
			return false;
		}

		/**
		 * Returns the length of the file represented by the path or 0 if the file is not found or is inaccessible. Note that the maximum filelength is 2^31-1 bytes (the size of a java int) because the ContentResource::getContentLength API requires that
		 * the return type be an integer, not a long. See also getContentLengthLong().
		 * 
		 * @return length of the file, subject to the file not exceeding 2^31-1 bytes; -1 if the file does not exist (or is now a directory).
		 */
		public int getContentLength()
		{
			if (file.exists() && file.canRead()) return (int) file.length();
			return -1;
		}

		/**
		 * Returns the length of the file represented by the path or 0 if the file is not found or is inaccessible. See also getContentLength().
		 * 
		 * @return length of the file
		 */
		public long getContentLengthLong()
		{
			if (file.exists() && file.canRead()) return (int) file.length();
			return -1;
		}

		/**
		 * Returns the mimetype of the file represented
		 * 
		 * @return MIME type of the file represented
		 */
		public String getContentType()
		{
			return "text/plain"; // TODO
		}

		/**
		 * Retrieves and returns the contents of the file
		 * 
		 * @return An array containing the data of the file or null if the file is not found or is not accessible
		 */
		public byte[] getContent() throws ServerOverloadException
		{
			try
			{
				java.io.InputStream fis = streamContent();
				if (fis == null) return null;
				byte[] b = new byte[fis.available()];
				fis.read(b);
				fis.close();
				fis = null; // try to close the stream to conserve resources
				return b;
			}
			catch (Exception e)
			{
				return null;
			}
		}

		/**
		 * Returns an Input Stream from which the contents of the file can be streamed, or null if the file does not exist or is inaccessible.
		 */
		public InputStream streamContent() throws ServerOverloadException
		{
			if (!file.exists() || !file.canRead() || !file.isFile()) return null;
			try
			{
				return new java.io.FileInputStream(file);
			}
			catch (Exception e)
			{
			}
			return null;
		}

		public String getResourceType()
		{
			return getContentType();
		}
	}

	/**
	 * A concrete class representing virtual content entities corresponding to filesystem directories.
	 * 
	 * @author johnf (johnf@caret.cam.ac.uk)
	 */
	public class ContentCollectionFileSystem extends ContentEntityFileSystem implements ContentCollection
	{
		public ContentCollectionFileSystem(ContentEntity realParent, String basePath, String relativePath, ContentHostingHandler chh)
		{
			super(realParent, basePath,
					(relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) != '/') ? relativePath + "/"
							: relativePath, chh);
		}

		protected Edit wrap()
		{
			if (wrappedMe == null) wrappedMe = contentHostingHandlerResolver.newCollectionEdit(getId());
			if (wrappedMe != null)
			{
				// link it back to this CHH
				((ContentEntity) wrappedMe).setContentHandler(chh);
				((ContentEntity) wrappedMe).setVirtualContentEntity(this);
				setVirtualProperties();
			}
			return wrappedMe;
		}

		protected void setVirtualProperties()
		{
			// set the properties required for a sensible display in the resources list view
			String tmp;
			if (this.relativePath.equals("/"))
				tmp = this.basePath;
			else
			{
				if (SHOW_FULL_PATHS)
					tmp = this.relativePath;
				else
				{
					tmp = this.relativePath.substring(this.relativePath.lastIndexOf("/", this.relativePath.length() - 2))
							.substring(1);
					tmp = tmp.substring(0, tmp.length() - 1); // remove trailing "/" as that causes 2 /'s to appear in the breadcrumbs trail

				}
			}
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DISPLAY_NAME, tmp);
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_CREATOR, ""); // not supported on all filesystems
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_MODIFIED_DATE,
					new java.util.Date(this.file.lastModified()).toString());
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, this.basePath + this.relativePath);
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_DESCRIPTION, this.relativePath);

			// collection-only properties
			wrappedMe.getProperties().addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.TRUE.toString());
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isResource()
		{
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isCollection()
		{
			return true;
		}

		/**
		 * Returns a list of String Ids of the members of this collection (i.e. files in this directory).
		 * 
		 * @return a list of the String Ids of the members of this collection (i.e. files in this directory). Returns empty list if this is not a directory, no-longer exists, or is inaccessible.
		 */
		public List getMembers()
		{
			try
			{
				String[] contents = file.list();
				java.util.ArrayList mems = new java.util.ArrayList(contents.length);

				String newpath = getId(); // was realParent.getId() + relativePath;
				if (newpath.charAt(newpath.length() - 1) != '/') newpath = newpath + "/";
				for (int x = 0; x < contents.length; ++x)
					if (new java.io.File(newpath + contents[x]).isDirectory())
						mems.add(newpath + contents[x] + "/");
					else
						mems.add(newpath + contents[x]);
				return mems;
			}
			catch (Exception e)
			{
				
			}
			return new java.util.ArrayList();
		}

		/**
		 * Access a List of the collections' internal members as full ContentResource or ContentCollection objects.
		 * 
		 * @return a List of the full objects of the members of the collection.
		 */
		public List getMemberResources()
		{
			List l = getMembers();
			java.util.ArrayList resources = new java.util.ArrayList(l.size());
			for (java.util.Iterator i = l.iterator(); i.hasNext();)
			{
				ContentEntity ce = getMember((String) (i.next()));
				if (ce instanceof ContentResource) resources.add(((ContentResourceFileSystem) ce).wrap());
			}
			return resources;
		}

		/**
		 * Access the size of all the resource body bytes within this collection in Kbytes.
		 * 
		 * @return The size of all the resource body bytes within this collection in Kbytes.
		 */
		public long getBodySizeK()
		{
			long totalsize = 0L;
			List x = getMemberResources();
			for (java.util.Iterator i = x.iterator(); i.hasNext();)
			{
				Object o = i.next();
				if ((o != null) && (o instanceof ContentResource))
				{
					ContentResource crfs = (ContentResource) o;
					if (crfs instanceof ContentResourceFileSystem)
						totalsize += ((ContentResourceFileSystem) crfs).getContentLengthLong();
					else
						totalsize += crfs.getContentLength();
				}
			}
			return totalsize / 1024L;
		}

		public int getMemberCount()
		{
			return file.list().length;
		}

		public String getResourceType()
		{
			return null;
		}
	}

	/**
	 * @return the contentHandlerResover
	 */
	public ContentHostingHandlerResolver getContentHostingHandlerResolver()
	{
		return contentHostingHandlerResolver;
	}

	/**
	 * @param contentHandlerResover the contentHandlerResover to set
	 */
	public void setContentHostingHandlerResolver(ContentHostingHandlerResolver contentHostingHandlerResolver)
	{
		this.contentHostingHandlerResolver = contentHostingHandlerResolver;
	}

	public int getMemberCount(ContentEntity edit)
	{
		if (edit instanceof ContentCollectionFileSystem)
			return ((ContentCollectionFileSystem) edit).getMemberCount();
		if (edit.getVirtualContentEntity() instanceof ContentCollectionFileSystem)
			return ((ContentCollectionFileSystem) (edit.getVirtualContentEntity())).getMemberCount();
		return 0;
	}

}
