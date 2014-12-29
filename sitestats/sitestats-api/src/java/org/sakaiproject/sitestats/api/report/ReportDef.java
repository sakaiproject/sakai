/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api.report;

import java.io.Serializable;
import java.util.Date;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class ReportDef implements Serializable {
	private static final long	serialVersionUID	= 1L;
	private long				id;
	private String				siteId;
	private String				title;
	private String				description;
	private boolean				hidden				= false;
	private ReportParams		reportParams;
	private String				reportDefinitionXml;
	private Date				createdOn;
	private String				createdBy;
	private Date				modifiedOn;
	private String				modifiedBy;

	/** Get the bd row id. */
	public long getId() {
		return id;
	}

	/** Set the bd row id. */
	public void setId(long id) {
		this.id = id;
	}
	
	/** Get the context (site id) in which this report definition will be presented. If null, will be presented in all sites. */
	public String getSiteId() {
		return this.siteId;
	}

	/** Set the context (site id) in which this report definition will be presented. If null, will be presented in all sites. */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	/** Get the report title. */
	public String getTitle() {
		return title;
	}
	
	/** Set the report title. */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/** Check if report title is localized (when title is a bundle key like "${somekey}"). */
	public boolean isTitleLocalized() {
		return isStringLocalized(title);
	}
	
	/** Get bundle key for localized report title (when title is a bundle key like "${somekey}"). */
	public String getTitleBundleKey() {
		if(isStringLocalized(title)) {
			return title.substring(2, title.length()-1);
		}else{
			return title;
		}
	}
	
	/** Get the report description. */
	public String getDescription() {
		return description;
	}
	
	/** Set the report description. */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/** Check if report description is localized (when description is a bundle key like "${somekey}"). */
	public boolean isDescriptionLocalized() {
		return isStringLocalized(description);
	}
	
	/** Get bundle key for localized report description (when description is a bundle key like "${somekey}"). */
	public String getDescriptionBundleKey() {
		if(isStringLocalized(description)) {
			return description.substring(2, description.length()-1);
		}else{
			return description;
		}
	}
	
	/** Check if this report is hidden. */
	public boolean isHidden() {
		return hidden;
	}
	
	/** Hide/unhide this report. */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/** Get the report parameters. */
	public ReportParams getReportParams() {
		if(reportParams == null) {
			reportParams = new ReportParams(siteId);
		}
		return reportParams;
	}

	/** Set the report parameters. */
	public void setReportParams(ReportParams reportParams) {
		this.reportParams = reportParams;
	}

	/** Get the report definition as XML string. */
	public String getReportDefinitionXml() {
		return reportDefinitionXml;
	}

	/** Set the report definition as XML string. */
	public void setReportDefinitionXml(String prefs) {
		this.reportDefinitionXml = prefs;
	}
	
	/** Get report creation date. */
	public Date getCreatedOn() {
		return createdOn;
	}
	
	/** Set report creation date. */
	public void setCreatedOn(Date date) {
		this.createdOn = date;
	}
	
	/** Get report creation author. */
	public String getCreatedBy() {
		return createdBy;
	}
	
	/** Set report creation author. */
	public void setCreatedBy(String author) {
		this.createdBy = author;
	}

	/** Get report modification date. */
	public Date getModifiedOn() {
		return modifiedOn;
	}
	
	/** Set report modification date. */
	public void setModifiedOn(Date date) {
		this.modifiedOn = date;
	}
	
	/** Get report modification author. */
	public String getModifiedBy() {
		return modifiedBy;
	}
	
	/** Set report modification author. */
	public void setModifiedBy(String author) {
		this.modifiedBy = author;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof ReportDef)) return false;
		ReportDef other = (ReportDef) o;
		return id == other.getId()
				&& siteId.equals(other.getSiteId())
				&& title.equals(other.getTitle())
				&& description.equals(other.getDescription())
				&& hidden == other.isHidden()
				&& reportParams.equals(other.getReportParams())
				&& reportDefinitionXml.equals(other.getReportDefinitionXml())
				&& createdBy.equals(other.getCreatedBy())
				&& createdOn.equals(other.getCreatedOn())
				&& modifiedBy.equals(other.getModifiedBy())
				&& modifiedOn.equals(other.getModifiedOn());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		String hashStr = this.getClass().getName() + ":"
				+ this.getId()
				+ this.getSiteId().hashCode()
				+ this.getTitle().hashCode()
				+ this.getDescription().hashCode()
				+ Boolean.toString(this.isHidden()).hashCode()
				+ this.getReportParams().hashCode()
				+ this.getReportDefinitionXml().hashCode()
				+ this.getCreatedBy().hashCode()
				+ this.getCreatedOn().hashCode()
				+ this.getModifiedBy().hashCode()
				+ this.getModifiedOn().hashCode();
		return hashStr.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return toString(true);
	}
	
	public String toString(boolean includeXml) {
		String context = siteId != null ? "Listed for siteId="+siteId : "Predefined report";
		String str = context
			+ " : Title=" + title
			+ " : Description=" + description
			+ " : Parameters=" + reportParams;
		if(includeXml) {
			str += " : ParametersXml=" + reportDefinitionXml;
		}
		return str;
	}
		
	private boolean isStringLocalized(String string) {
		if(string != null && string.startsWith("${") && string.endsWith("}")) {
			return true;
		}else{
			return false;
		}
	}
}
