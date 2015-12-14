package org.sakaiproject.gradebookng.business.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for storing the categorized order of an assignment.
 * 
 * A list of these is persisted as XML into the site tool properties and used for sorting
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class AssignmentOrder {

	@Getter
	@Setter
	@XmlElement
	private Long assignmentId;
	
	@Getter
	@Setter
	@XmlElement
	private int order;

  @Getter
  @Setter
  @XmlElement
  private String category;

  @SuppressWarnings("unused")
	private AssignmentOrder() {
		//JAXB constructor
	}
	
	public AssignmentOrder(long assignmentId, String category, int order) {
		this.assignmentId = new Long(assignmentId);
    this.category = category;
		this.order = order;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	
}
