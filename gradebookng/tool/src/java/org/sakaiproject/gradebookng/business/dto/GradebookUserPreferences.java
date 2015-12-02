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
 * Class for managing user settings for the gradebook NG tool. This is per user and per site
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class GradebookUserPreferences {

	@Getter
	@Setter
	@XmlElement
	private String userUuid;
	
	@Getter
	@Setter
	@XmlElement
	private String siteId;
	
	@Getter
	@Setter
	@XmlElement
	public int sortOrder;
	
	
	private GradebookUserPreferences() {
		//JAXB constructor
	}
	
	public GradebookUserPreferences(String userUuid) {
		this.userUuid = userUuid;
	}
	
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	
}
