package org.sakaiproject.contentreview.model;

import java.util.Date;

/**
 * This defines locks for various evaluation resources (primary this is used for locking the data preloads)
 * to allow for cluster operations
 */
public class ContentReviewLock implements java.io.Serializable {

	private Long id;

	private Date lastModified;

	/**
	 * The name of the lock
	 */
	private String name;

	/**
	 * The holder (owner) of this lock
	 */
	private String holder;

	// Constructors

	/** default constructor */
	public ContentReviewLock() {
	}

	/** full constructor */
	public ContentReviewLock(String name, String holder) {
		this.lastModified = new Date();
		this.name = name;
		this.holder = holder;
	}

	// Property accessors
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
   
   public String getHolder() {
      return holder;
   }
   
   public void setHolder(String holder) {
      this.holder = holder;
   }


}
