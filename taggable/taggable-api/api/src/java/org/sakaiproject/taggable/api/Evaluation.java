package org.sakaiproject.taggable.api;

import java.util.Date;

/**
 * Interface for Evaluation objects.  This is currently for the Assignment2/OSP 
 * integration but can be expanded to anything that wants to use it.
 * It's really just a wrapper class for the thing that is really the evaluation
 * @author chrismaurer
 *
 */
public interface Evaluation {
	
	/**
	 * Get the siteId
	 * @return
	 */
	public String getSiteId();
	
	/**
	 * Set the siteId
	 * @param siteId
	 */
	public void setSiteId(String siteId);
	
	/**
	 * Get the site title
	 * @return
	 */
	public String getSiteTitle();
	
	/**
	 * Set the site title
	 * @param siteTitle
	 */
	public void setSiteTitle(String siteTitle);
	
	/**
	 * Get the last modification date
	 * @return
	 */
	public Date getLastModDate();
	
	/**
	 * Set the last modification date
	 * @param lastModDate
	 */
	public void setLastModDate(Date lastModDate);
	
	/**
	 * Get the id of the user that created the evaluation
	 * @return
	 */
	public String getCreatedById();
	
	/**
	 * Set the id of the user that created the evaluation
	 * @param createdById
	 */
	public void setCreatedById(String createdById);
	
	/**
	 * Get the (display) name of the user that created the evaluation
	 * @return
	 */
	public String getCreatedByName();
	
	/**
	 * Set the (display) name of the user that created the evaluation
	 * @param createdByName
	 */
	public void setCreatedByName(String createdByName);

	/**
	 * Get the title of the evaluation
	 * @return
	 */
	public String getEvalItemTitle();
	
	/**
	 * Set the title for the evaluation
	 * @param evalItemTitle
	 */
	public void setEvalItemTitle(String evalItemTitle);

	/**
	 * Get the url that will be used to render the evaluation
	 * @return
	 */
	public String getEvalItemURL();
	
	/**
	 * Set the url that will be used to render the evaluation
	 * @param evalItemURL
	 */
	public void setEvalItemURL(String evalItemURL);
	
	/**
	 * Get the url that will be used when editing the evaluation
	 * @return
	 */
	public String getEditActionURL();
	
	/**
	 * Get the url that will be used when removing the evaluation
	 * @return
	 */
	public String getRemoveActionURL();

	/**
	 * Determine if the current user is allowed to view the evaluation
	 * @return
	 */
	public boolean isCanViewEvaluation();
	
	/**
	 * Setter 
	 * @param canViewEvaluation
	 */
	public void setCanViewEvaluation(boolean canViewEvaluation);

	/**
	 * Determine if the current user is allowed to modify the evaluation
	 * @return
	 */
	public boolean isCanModifyEvaluation();

	/**
	 * Setter
	 * @param canModifyEvaluation
	 */
	public void setCanModifyEvaluation(boolean canModifyEvaluation);

	/**
	 * Determine if the current user is allowed to remove the evaluation
	 * @return
	 */
	public boolean isCanRemoveEvaluation();

	/**
	 * Setter
	 * @param canRemoveEvaluation
	 */
	public void setCanRemoveEvaluation(boolean canRemoveEvaluation);

}
