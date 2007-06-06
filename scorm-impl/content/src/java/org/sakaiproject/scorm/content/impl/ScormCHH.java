package org.sakaiproject.scorm.content.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ScormCHH extends FastZipCHH {
	
	private static Log log = LogFactory.getLog(ScormCHH.class);

	protected ScormContentService scormContentService() {
		return null;
	}

	protected ContentCollectionEdit makeCollection(String id, String path,
			String name) {
		ContentCollectionEdit collection = super.makeCollection(id, path, name);
		collection.setResourceType(ScormCollectionType.SCORM_CONTENT_TYPE_ID);
		return collection;
	}
	
	/*protected List<ContentEntity> findChildren(ContentEntity ce, int depth) throws ServerOverloadException {
		return extractChildren(ce, depth);
	}*/
		
	protected int countChildren(ContentEntity parent, int depth) {
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String relativePath = getRelativePath(realParent.getId(), parent.getId());
		
		VirtualFileSystem fs = getVirtualFileSystem(realParent);
		
		return fs.getCount(relativePath);
	}
	
	protected List<ContentEntity> extractChildren(ContentEntity parent, int depth) {
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String realParentId = realParent.getId();
		String relativePath = getRelativePath(realParent.getId(), parent.getId());
		
		VirtualFileSystem fs = getVirtualFileSystem(realParent);
		
		List<ContentEntity> list = new LinkedList<ContentEntity>();
		
		List<String> entityNames = fs.getChildren(relativePath);
		
		for (String name : entityNames) {
			if (name.endsWith(Entity.SEPARATOR))
				list.add(makeCollection(realParentId, newId(relativePath, name), name));
			else
				list.add(makeResource(realParentId, newId(relativePath, name), name, null));
		}

		return list;
	}
	
	protected VirtualFileSystem getVirtualFileSystem(ContentEntity parent) {
		ContentResource realParent = (ContentResource) getRealParent(parent.getId());
		String realParentId = realParent.getId();
		
		VirtualFileSystem fs = uncacheVirtualFileSystem(realParentId);
		if (fs == null) {
			fs = buildVirtualFileSystem(realParent);
			if (fs != null)
				cacheVirtualFileSystem(realParentId, fs);
		}
		return fs;
	}
	
	protected VirtualFileSystem uncacheVirtualFileSystem(String key) {
		VirtualFileSystem fs = null;
		try {
			fs = (VirtualFileSystem) ThreadLocalManager.get(VIRTUAL_FS_CACHE_KEY + key);
		} catch(ClassCastException e) {
			log.error("Caught a class cast exception finding virtual file system with key " + key, e);
		}
		
		return fs;
	}
	
	protected void cacheVirtualFileSystem(String key, VirtualFileSystem fs) {
		if (null != fs) {			
			ThreadLocalManager.set(VIRTUAL_FS_CACHE_KEY + key, fs);
		}
	}
	
	protected VirtualFileSystem buildVirtualFileSystem(ContentEntity realParent) {
		String realParentId = realParent.getId();
		
		VirtualFileSystem fs = new VirtualFileSystem(realParentId);
		
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
					System.getProperty(XmlPullParserFactory.PROPERTY_NAME),
					null);

			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			InputStream inStream = getManifestAsStream(realParentId);
			xpp.setInput(inStream, null);

			int eventType = xpp.getEventType();
			String tagName = null;
			String xmlBase = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = xpp.getName();
					if (tagName != null) {
						if (tagName.equals("resource")) {
							xmlBase = getAttributeValue(xpp, "xml", "base");
						} else if (tagName.equals("file")) {
							String href = getAttributeValue(xpp, null, "href");

							StringBuffer pathBuffer = new StringBuffer();
							if (xmlBase != null)
								pathBuffer.append(xmlBase);
							if (href != null) {
								pathBuffer.append(href);
								fs.addPath(pathBuffer.toString());
							}							
						}
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					tagName = xpp.getName();
					if (tagName != null && tagName.equals("resource")) 
						xmlBase = null;
				}
				eventType = xpp.next();
			}
			if (inStream != null)
				inStream.close();
		} catch (Exception xppe) {
			log.error("Caught an exception parsing the xml of the manifest document", xppe);
		}
		
		return fs;
	}
	
	private String getAttributeValue(XmlPullParser xpp, String prefix, String name) {
		int count = xpp.getAttributeCount();
		
		String value = null;
		for (int i = 0; i < count; i++) {
			String attrPrefix = xpp.getAttributePrefix(i);
			String attrName = xpp.getAttributeName(i);
			if (attrName != null && (prefix == null || 
					(attrPrefix != null && attrPrefix.equals(prefix)))) {
				if (attrName.equals(name)) {
					value = URLDecoder.decode(xpp.getAttributeValue(i));
					return value;
				}
			}
		}
		
		return value;
	}
		
	private InputStream getManifestAsStream(String contentPackageId) {
		InputStream stream = null;
		
		String path = new StringBuffer().append(contentPackageId).append("/imsmanifest.xml").toString();
		try {
			ContentResource resource = super.getContentResourceEdit(path);
			
			if (resource != null) {
				byte[] content = resource.getContent();
				
				if (content != null) {
					stream = new ByteArrayInputStream(content);
				} else {
					stream = resource.streamContent();
				}
			}
		} catch (Exception iue) {
			log.error("Caught an exception grabbing the manifest file as a stream", iue);
		}
		
		return stream;
	}
}
