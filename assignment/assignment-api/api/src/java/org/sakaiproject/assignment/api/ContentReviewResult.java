package org.sakaiproject.assignment.api;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.model.ContentReviewItem;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Introduced for SAK-26322 
 * A ContentReviewResult represents the results of a content review item
 */
public class ContentReviewResult
{
	/**
	 * The ContentResource which was reviewed
	 */
	private ContentResource resource;

	/**
	 * The score from the content review service
	 */
	private int reviewScore;
	
	/**
	 * The URL to the content review report
	 */
	private String reviewReport;

	/**
	 * The status of the review
	 */
	private String reviewStatus;

	/**
	 * The URL of the content review icon associated with this item
	 */
	private String reviewIconURL;

	/**
	 * An error string, if any, return from the review service
	 */
	private String reviewError;
	
	private ContentReviewItem contentReviewItem;


	/**
	 * Getter for the ContentResource which was reviewed
	 */
	public ContentResource getContentResource()
	{
		return resource;
	}

	/**
	 * Setter for the ContentResource which was reviewed
	 */
	public void setContentResource(ContentResource resource)
	{
		this.resource = resource;
	}

	/**
	 * Determines if this ContentReview result is from a student's inline text
	 */
	public boolean isInline()
	{
		ResourceProperties props = resource.getProperties();
		return "true".equals(props.getProperty(AssignmentSubmission.PROP_INLINE_SUBMISSION));
	}

	/**
	 * Getter for score from the content review service
	 */
	public int getReviewScore()
	{
		return reviewScore;
	}

	/**
	 * Setter for score form the content review service
	 */
	public void setReviewScore(int reviewScore)
	{
		this.reviewScore = reviewScore;
	}

	/**
	 * Getter for URL to the content review report
	 */
	public String getReviewReport()
	{
		return reviewReport;
	}

	/**
	 * Setter for URL to the content review report
	 */
	public void setReviewReport(String reviewReport)
	{
		this.reviewReport = reviewReport;
	}

	/**
	 * Getter for the status of the review
	 */
	public String getReviewStatus()
	{
		return reviewStatus;
	}

	/**
	 * Setter for the status of the review
	 */
	public void setReviewStatus(String reviewStatus)
	{
		this.reviewStatus = reviewStatus;
	}

	/**
	 * Getter for the URL of the content review icon associated with this item
	 */
	public String getReviewIconURL()
	{
		return reviewIconURL;
	}

	/**
	 * Setter for the URL of the content review icon associated with this item
	 */
	public void setReviewIconURL(String reviewIconURL)
	{
		this.reviewIconURL = reviewIconURL;
	}

	/**
	 * Getter for the error string, if any, returned from the review service
	 */
	public String getReviewError()
	{
		return reviewError;
	}

	/**
	 * Setter for the error string, if any, returned from the review service
	 */
	public void setReviewError(String reviewError)
	{
		this.reviewError = reviewError;
	}

	public ContentReviewItem getContentReviewItem() {
		return contentReviewItem;
	}

	public void setContentReviewItem(ContentReviewItem contentReviewItem) {
		this.contentReviewItem = contentReviewItem;
	}
}
