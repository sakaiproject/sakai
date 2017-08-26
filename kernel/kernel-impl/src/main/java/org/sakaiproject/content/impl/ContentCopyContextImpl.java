package org.sakaiproject.content.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.sakaiproject.content.api.ContentCopyContext;

/**
 * Class to keep track of how the copying of resources is going.
 * The files that need to be looked at and the data for doing the copy.
 * @author buckett
 *
 */
public class ContentCopyContextImpl implements ContentCopyContext{

	// The site ID that the resources are coming from.
	private String oldSiteId;
	// The site ID that the resources are going to.
	private String newSiteId;
	
	// If true then we should look for any related content and copy that as well.
	private boolean walkReferences;
	
	// Resources we still need to look at.
	private Queue<String> resourcesToProcess = new LinkedList<String>();
	// Resources that we've already looked at or in queue, this is to stop us ending up in a loop.
	private Set<String> resourcesSeen = new HashSet<String>();
	
	private Map<String, String> results = new HashMap<String, String>();
	
	public ContentCopyContextImpl(String oldSiteId, String newSiteId, boolean walkReferences) {
		this.oldSiteId = oldSiteId;
		this.newSiteId = newSiteId;
		this.walkReferences = walkReferences;
	}

	public String getOldSiteId() {
		return oldSiteId;
	}

	public String getNewSiteId() {
		return newSiteId;
	}

	/**
	 * Should dependent resources be added to the list of material to copy.
	 * @return <code>true</code> if dependent resources should be copied.
	 */
	public boolean isWalkReferences() {
		return walkReferences;
	}

	/**
	 * When a new resource is found is should be normalised and passed to this method.
	 * @param resourceId The ID to resource that should be copied. This should <b>not</b> be 
	 * a reference to the resource.
	 */
	public void addResource(String resourceId) {
		if (resourcesSeen.add(resourceId)) {
			resourcesToProcess.add(resourceId);
		}
	}
	
	/**
	 * When a new resource to be process is needed this method should be called.
	 * If we aren't walking the references then we never give them back.
	 * @return A resource ID to process/copy.
	 */
	public String popResource() {
		return resourcesToProcess.poll();
	}

	public void logCopy(String source, String destination) {
		results.put(source, destination);	
	}

	public Map<String, String> getCopyResults() {
		return Collections.unmodifiableMap(results);
	}
}
