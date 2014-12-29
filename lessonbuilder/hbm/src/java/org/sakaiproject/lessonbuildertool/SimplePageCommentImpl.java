package org.sakaiproject.lessonbuildertool;

import java.util.Date;

public class SimplePageCommentImpl implements SimplePageComment, Comparable {
	
	private long id;
	private long itemId; // ID of comments widget
	private long pageId; // ID of the page the comment is on
	private Date timePosted; // Time the comment was posted
	private String author; // User ID of author
	private String comment; // Text of the comment
	private String UUID;
	private boolean html;
	private Double points;
	
	public SimplePageCommentImpl() { }
	
	public SimplePageCommentImpl(long itemId, long pageId, String author, String comment, String UUID, boolean html) {
		this.itemId = itemId;
		this.pageId = pageId;
		this.timePosted = new Date();
		this.author = author;
		this.comment = comment;
		this.UUID = UUID;
		this.html = html;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setItemId(long id) {
		this.itemId = id;
	}
	
	public long getItemId() {
		return itemId;
	}
	
	public void setPageId(long id) {
		this.pageId = id;
	}
	
	public long getPageId() {
		return pageId;
	}
	
	public void setTimePosted(Date date) {
		timePosted = date;
	}
	
	public Date getTimePosted() {
		return timePosted;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setUUID(String UUID) {
		this.UUID = UUID;
	}
	
	public String getUUID() {
		return UUID;
	}
	
	public void setHtml(boolean html) {
		this.html = html;
	}
	
	public boolean getHtml() {
		return html;
	}
	
	
	public int compareTo(Object o) {
		if(!(o instanceof SimplePageComment)) {
			throw new ClassCastException("Expected SimplePageComment Object");
		}
		
		return timePosted.compareTo(((SimplePageCommentImpl)o).getTimePosted());
	}
	
	public void setPoints(Double points) {
		this.points = points;
	}
	
	public Double getPoints() {
		return points;
	}
}
