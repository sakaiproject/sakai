package org.sakaiproject.content.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.sakaiproject.entity.api.Reference;

/**
 * 
 * 
 *
 */
public interface ResourceToolActionPipe 
{
	/**
	 * Used by helper to access the initialization-id for this action, if an initialization-id
	 * was returned when ResourcesAction invoked the InteractionAction.initializeAction() method. 
	 * The return value of this method will be null if an initialization-id was not supplied by
	 * that method.
	 * @return
	 */
	public String getInitializationId();
	
	/**
	 * Used by ResourcesAction to provide a value for the initialization-id.  Should be initialized
	 * the value returned by the InteractionAction.initializeAction() method.
	 * @param 
	 */
	public void setInitializationId(String id);
	
	/**
	 * Used by helper to access current value of resource's "content". Return will be 
	 * empty string to indicate no content is defined for resource or null to indicate 
	 * that content should be accessed as an OutputStream because of size or type of content. 
	 * @return
	 */
	public String getContent();
	
	/**
	 * Used by helper to access a Reference object that fully identifies an existing ContentResource
	 * involved in this action.  If the action is "create", the reference identifies a collection
	 * within which a new resource is being created.  Otherwise the reference identifies a resource
	 * of the type involved in this action. 
	 * @return
	 */
	public Reference getContentEntityReference();
	
	/**
	 * Used by helper to access current value of resource's "content" in cases where size 
	 * or type of content requires stream access. Return may be null to indicate 
	 * that content should be accessed as a String. 
	 * @return
	 */
	public OutputStream getContentStream();
	
	/**
	 * Used by helper to access current mimetype of resource. Null value or empy string 
	 * indicates that mimetype is irrelevant or unavailable.
	 * @return
	 */
	public String getContentType();
	
	/**
	 * Used by helper to access current value of any resource property 
	 * that has been specified by helper in InteractionAction.
	 * @return
	 */
	public String getPropertyValue(String name);
	
	/**
	 * Used by ResourceAction to access helper's revised value of resource's "content".
	 * @return
	 */
	public String getRevisedContent();
	
	/**
	 * Used by ResourceAction to access helper's revised value of resource's "content".
	 * @return
	 */
	public InputStream getRevisedContentStream();
	
	/**
	 * Used by ResourceAction to access helper's revised value for mimetype of resource.
	 * @return
	 */
	public String getRevisedContentType();
	
	/**
	 * Used by ResourceAction to access helper's revisions to values of resource properties.
	 * @return
	 */
	public Map getRevisedResourceProperties();

	/**
	 * Used by ResourcesAction to provide helper with current value of resource's "content".
	 * @return
	 */
	public void setContent(String content);
	
	/**
	 * Used by ResourcesAction to provide a Reference that fully identifies an existing ContentResource
	 * involved in this action.  If the action is "create", the reference identifies a collection
	 * within which a new resource is being created.  Otherwise the reference identifies a resource
	 * of the type involved in this action. 
	 * @param reference
	 */
	public void setContentEntityReference(Reference reference);
	
	/**
	 * Used by ResourcesAction to provide helper with alternative access to current value of resource's "content".
	 * @param content
	 */
	public void setContentStream(OutputStream ostream);
	
	/**
	 * Used by ResourcesAction to provide helper with mimetype of resource.
	 * @param ostream
	 */
	public void setContentType(String type);
	
	/**
	 * Used by ResourcesAction to provide helper with current value of any requested resource property.
	 * @param name
	 * @param value
	 */
	public void setResourceProperty(String name, String value);
	
	/**
	 * Used by helper to provide ResourcesAction with revised value of resource's "content".
	 * @param content
	 */
	public void setRevisedContent(String content);
	
	/**
	 * Used by helper to provide ResourcesAction with revised value of resource's "content".
	 * @param istream
	 */
	public void setRevisedContentStream(InputStream istream);
	
	/**
	 * Used by helper to provide ResourcesAction with revised mimetype of resource. 
	 * Mimetype will not be referenced if param is null.
	 * @param type
	 */
	public void setRevisedContentType(String type);
	
	/**
	 * Used by helper to provide ResourcesAction with revised value for a resource property.
	 * Any property other than "live" properties can be set with this method. 
	 * If action definition names the property and it is not set by this method, any 
	 * current value will be removed.  
	 * @param name
	 * @param value
	 */
	public void setRevisedResourceProperty(String name, String value);
	
}
