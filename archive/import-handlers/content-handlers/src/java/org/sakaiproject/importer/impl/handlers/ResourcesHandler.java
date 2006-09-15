package org.sakaiproject.importer.impl.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.sakaiproject.importer.impl.importables.TextDocument;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationService;

import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class ResourcesHandler implements HandlesImportable {
	private static final String COPYRIGHT = "(c) 2006";
	
	private Log m_log = LogFactory.getLog(org.sakaiproject.importer.impl.handlers.ResourcesHandler.class);

	public boolean canHandleType(String typeName) {
		return (("sakai-file-resource".equals(typeName) || ("sakai-folder".equals(typeName)) || ("sakai-text-document".equals(typeName)) || ("sakai-html-document".equals(typeName)) || ("sakai-web-link".equals(typeName))));
	}

	public void handle(Importable thing, String siteId) {
		if(canHandleType(thing.getTypeName())){
			String currentUser = SessionManager.getCurrentSessionUserId();
			SessionManager.getCurrentSession().setUserId("admin");
			String id = null;
			String contentType = null;
			byte[] contents = null;
			int notifyOption = NotificationService.NOTI_NONE;
			String title = null;
			String description = null;
			Map resourceProps = new HashMap();
			if ("sakai-file-resource".equals(thing.getTypeName())) {
				//title = ((FileResource)thing).getTitle();
				description = ((FileResource)thing).getDescription();
				String fileName = ((FileResource)thing).getFileName();
				id = "/group/" + siteId + "/" + ((FileResource)thing).getDestinationResourcePath();
				contentType = new MimetypesFileTypeMap().getContentType(fileName);
				contents = ((FileResource)thing).getFileBytes();
//				if((title == null) || (title.equals(""))) {
//					title = fileName;
//				}
				title = fileName;
				resourceProps.put(ResourceProperties.PROP_DISPLAY_NAME, title);
				resourceProps.put(ResourceProperties.PROP_DESCRIPTION, description);
				if(m_log.isDebugEnabled()) {
					m_log.debug("import ResourcesHandler about to add file entitled '" + title + "'");
				}
				addContentResource(id, contentType, contents, resourceProps, notifyOption);
			} else if ("sakai-web-link".equals(thing.getTypeName())) {
				title = ((WebLink)thing).getTitle();
				description = ((WebLink)thing).getDescription();
				id = "/group/" + siteId + "/"+ thing.getContextPath();
				contentType = ResourceProperties.TYPE_URL;
				String absoluteUrl = "";
				if (((WebLink)thing).isAbsolute()) {
					absoluteUrl = ((WebLink)thing).getUrl();
				} else {
					absoluteUrl = ServerConfigurationService.getServerUrl() + "/access/content/group/" + siteId + "/" + ((WebLink)thing).getUrl();
				}
				contents = absoluteUrl.getBytes();
				if((title == null) || (title.equals(""))) {
					title = ((WebLink)thing).getUrl();
				}
				resourceProps.put(ResourceProperties.PROP_DISPLAY_NAME, title);
				resourceProps.put(ResourceProperties.PROP_DESCRIPTION, description);
				if(m_log.isDebugEnabled()){ 
					m_log.debug("import ResourcesHandler about to add web link entitled '" + title + "'");
				}
				addContentResource(id, contentType, contents, resourceProps, notifyOption);
			} else if ("sakai-html-document".equals(thing.getTypeName())) {
				title = ((HtmlDocument)thing).getTitle();
				contents = ((HtmlDocument)thing).getContent().getBytes();
				id = "/group/" + siteId + "/"+ thing.getContextPath();
				contentType = "text/html";
				resourceProps.put(ResourceProperties.PROP_DISPLAY_NAME, title);
				if(m_log.isDebugEnabled()){ 
					m_log.debug("import ResourcesHandler about to add html document entitled '" + title + "'");
				}
				addContentResource(id, contentType, contents, resourceProps, notifyOption);
			} else if ("sakai-text-document".equals(thing.getTypeName())) {
				title = ((TextDocument)thing).getTitle();
				contents = ((TextDocument)thing).getContent().getBytes();
				id = "/group/" + siteId + "/"+ thing.getContextPath();
				contentType = "text/plain";
				resourceProps.put(ResourceProperties.PROP_DISPLAY_NAME, title);
				if(m_log.isDebugEnabled()){ 
					m_log.debug("import ResourcesHandler about to add text document entitled '" + title + "'");
				}
				addContentResource(id, contentType, contents, resourceProps, notifyOption);
				} // else if ("sakai-folder".equals(thing.getTypeName())) {
//				title = ((Folder)thing).getTitle();
//				description = ((Folder)thing).getDescription();
//				resourceProps.put(ResourceProperties.PROP_DISPLAY_NAME, title);
//	  			resourceProps.put(ResourceProperties.PROP_DESCRIPTION, description);
//	  			resourceProps.put(ResourceProperties.PROP_COPYRIGHT, COPYRIGHT);
//	  			/*
//	  			 * Added title to the end of the path. Otherwise, we're setting the props on the 
//	  			 * containing folder rather than the folder itself.
//	  			 */
//	  			String path = "/group/" + siteId + "/" + ((Folder)thing).getPath()+ title;
//	  			addContentCollection(path,resourceProps);
//	  			
//			}
			SessionManager.getCurrentSession().setUserId(currentUser);
		}

	}
	protected void addContentResource(String id, String contentType, byte[] contents, Map properties, int notifyOption) {
		try {
			ResourcePropertiesEdit resourceProps = ContentHostingService.newResourceProperties();
			Set keys = properties.keySet();
			for (Iterator i = keys.iterator();i.hasNext();) {
				String key = (String)i.next();
				String value = (String)properties.get(key);
				resourceProps.addProperty(key, value);
			}
//			String enclosingDirectory = id.substring(0, id.lastIndexOf('/', id.length() - 2) + 1);
//			if(!existsDirectory(enclosingDirectory)) {
//				Map props = new HashMap();
//				props.put(ResourceProperties.PROP_DISPLAY_NAME, enclosingDirectory.substring(enclosingDirectory.lastIndexOf('/') + 1, enclosingDirectory.length()));
//				addContentCollection(enclosingDirectory, props);
//			}
			ContentHostingService.addResource(id, contentType, contents, resourceProps, notifyOption);
		} catch (PermissionException e) {
			m_log.error("ResourcesHandler.addContentResource: " + e.toString());
		} catch (IdUsedException e) {
//			TODO Auto-generated catch block
            e.printStackTrace();
		} catch (IdInvalidException e) {
//			TODO Auto-generated catch block
            e.printStackTrace();
		} catch (InconsistentException e) {
//			TODO Auto-generated catch block
            e.printStackTrace();
		} catch (OverQuotaException e) {
//			TODO Auto-generated catch block
            e.printStackTrace();
		} catch (ServerOverloadException e) {
//			TODO Auto-generated catch block
            e.printStackTrace();
		}
		
	}
	
	protected boolean existsDirectory(String path) {
		try {
			ContentHostingService.getCollection(path);
		} catch (IdUnusedException e) {
			return false;
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			m_log.error("ResourcesHandler.existsDirectory: " + e.toString());
		}
		return true;
	}

	protected void addContentCollection(String path, Map properties) {
//			ResourcePropertiesEdit resourceProps = ContentHostingService.newResourceProperties();
//			Set keys = properties.keySet();
//			for (Iterator i = keys.iterator();i.hasNext();) {
//				String key = (String)i.next();
//				String value = (String)properties.get(key);
//				resourceProps.addProperty(key, value);
//			}
			try {
//				String enclosingDirectory = path.substring(0, path.lastIndexOf('/', path.length() - 2) + 1);
//				if(!existsDirectory(enclosingDirectory)) {
//					ContentCollectionEdit coll = ContentHostingService.addCollection(enclosingDirectory);
//					ContentHostingService.commitCollection(coll);
//				}
				ContentCollectionEdit coll = ContentHostingService.addCollection(path);
				ContentHostingService.commitCollection(coll);
			} catch (IdUsedException e) {
				// if this thing already exists (which it probably does), 
				// we'll do an update on the properties rather than creating the folder
	            try {
	                ContentHostingService.addProperty
	                    (path, ResourceProperties.PROP_DISPLAY_NAME, (String)properties.get(ResourceProperties.PROP_DISPLAY_NAME));
	                ContentHostingService.addProperty
                    (path, ResourceProperties.PROP_COPYRIGHT, (String)properties.get(ResourceProperties.PROP_COPYRIGHT));
                ContentHostingService.addProperty
	                (path, ResourceProperties.PROP_DESCRIPTION, (String)properties.get(ResourceProperties.PROP_DESCRIPTION));
	            } catch (PermissionException e1) {
	            	m_log.error("ResourcesHandler.addContentCollection: " + e.toString());
	            } catch (IdUnusedException e1) {
	                // TODO Auto-generated catch block
	                e1.printStackTrace();
	            } catch (TypeException e1) {
	                // TODO Auto-generated catch block
	                e1.printStackTrace();
	            } catch (InUseException e1) {
	                // TODO Auto-generated catch block
	                e1.printStackTrace();
	            } catch (ServerOverloadException e1) {
	                // TODO Auto-generated catch block
	                e1.printStackTrace();
	            }
			} catch (IdInvalidException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InconsistentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
