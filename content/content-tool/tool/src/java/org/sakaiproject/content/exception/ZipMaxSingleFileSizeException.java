package org.sakaiproject.content.exception;

import org.sakaiproject.exception.SakaiException;
import java.util.Set;

/**
 * <p>
 * ZipMaxSingleFileSizeException is thrown whenever an individual file size exceeds the maximum permitted in default.sakai.properties.
 * </p>
 */
public class ZipMaxSingleFileSizeException extends SakaiException {
	private Set<String> resourceIds;
	/**
	 * Construct.
	 *
	 * @param resourceIds
	 *        The resource id.
	 */
	public ZipMaxSingleFileSizeException(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}
	/**
	 * Access the resource id.
	 *
	 * @return The resource id.
	 */
	public Set<String> getResourceIds()
	{
		return resourceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage()
	{
		return "resource=" + resourceIds.toString();
	}
}