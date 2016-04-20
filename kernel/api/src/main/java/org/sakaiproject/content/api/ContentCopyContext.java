package org.sakaiproject.content.api;

import java.util.Map;

/**
 * Class to keep track of how the copying of resources is going.
 * The files that need to be looked at and the data for doing the copy.
 * @author buckett
 */
public interface ContentCopyContext {
	
	public String getOldSiteId();
	
	public String getNewSiteId();
	/**
	 * Should dependent resources be added to the list of material to copy.
	 * @return <code>true</code> if dependent resources should be copied.
	 */
	public boolean isWalkReferences();

	/**
	 * When a new resource is found is should be normalised and passed to this method.
	 * @param resourceId The ID to resource that should be copied. This should <b>not</b> be 
	 * a reference to the resource.
	 */
	public void addResource(String resourceId);
	
	/**
	 * When a new resource to be process is needed this method should be called.
	 * If we aren't walking the references then we never give them back.
	 * @return A resource ID to process/copy.
	 */
	public String popResource();
	
	/**
	 * Log the results of a copy.
	 * @param source The resource that was being copied.
	 * @param destination The destination of the resource.
	 */
	public void logCopy(String source, String destination);
	
	/**
	 * Get all the results of the copy.
	 * @return This is a map of old resource to new resource.
	 */
	public Map<String, String> getCopyResults();
}
