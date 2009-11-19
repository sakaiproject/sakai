package org.sakaiproject.tool.assessment.ui.bean.print;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a> alt^I
 *
 * This class is basically just a conveinceince class for abstracting the creation of
 * PDF's from assessments
 * 
 */
public class PDFItemBean {

	private Long itemId = null; 

	private String content = null;

	private String meta = null;

	/**
	 * gets the item id
	 */
	public Long getItemId() {
		return itemId;
	}

	/**
	 * sets the item id
	 */
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	/**
	 * gets the raw generated html version of a question
	 * @return question html
	 */
	public String getContent() {
		return content;
	}

	/**
	 * sets the ray generated html version of a question
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * gets the Meta data section of an item
	 * @return Meta block string
	 */
	public String getMeta() {
		return meta;
	}

	/**
	 * sets the Meta dat section of an item
	 * @param meta
	 */
	public void setMeta(String meta) {
		this.meta = meta;
	}

}