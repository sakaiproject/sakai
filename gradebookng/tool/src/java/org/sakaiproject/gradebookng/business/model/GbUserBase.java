package org.sakaiproject.gradebookng.business.model;

/**
 *
 * @author bjones86
 */
public interface GbUserBase
{
	public String getDisplayId();
	public String getDisplayName();
	public boolean isValid();
}
