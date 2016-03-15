/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.data.dao.grading;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * 
 *
 *
 */
public class MediaData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -437737678404198607L;
	private Long mediaId;
	private ItemGradingData itemGradingData;
	private byte[] media;
	private Long fileSize; // in kilobyte
	private String mimeType;
	private String description;
	private String location;
	private String filename;
	private boolean isLink;
	private boolean isHtmlInline;
	private Integer status;
	private String createdBy;
	private Date createdDate;
	private String lastModifiedBy;
	private Date lastModifiedDate;
	private String duration;
	// Transient field to hold the ContentResource for media storage.
	// When AssessmentGradingFacadeQueries.saveMedia is called, the byte array is written
	// to a resource, rather than the blob column.
	private ContentResource contentResource;

	public MediaData() {
	}

	public MediaData(ItemGradingData itemGradingData, byte[] media,
			Long fileSize, String mimeType, String description,
			String location, String filename, boolean isLink,
			boolean isHtmlInline, Integer status, String createdBy,
			Date createdDate, String lastModifiedBy, Date lastModifiedDate,
			String duration) {
		this.itemGradingData = itemGradingData;
		this.media = media;
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.description = description;
		this.location = location;
		this.filename = filename;
		this.isLink = isLink;
		this.isHtmlInline = isHtmlInline;
		this.status = status;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = lastModifiedDate;
		this.duration = duration;
	}

	public MediaData(Long mediaId, ItemGradingData itemGradingData,
			Long fileSize, String mimeType, String description,
			String location, String filename, boolean isLink,
			boolean isHtmlInline, Integer status, String createdBy,
			Date createdDate, String lastModifiedBy, Date lastModifiedDate,
			String duration) {
		this.mediaId = mediaId;
		this.itemGradingData = itemGradingData;
		this.fileSize = fileSize;
		this.mimeType = mimeType;
		this.description = description;
		this.location = location;
		this.filename = filename;
		this.isLink = isLink;
		this.isHtmlInline = isHtmlInline;
		this.status = status;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = lastModifiedDate;
		this.duration = duration;
	}

	public MediaData(byte[] media, String mimeType) {
		setMimeType(mimeType);
		setMedia(media);
		setFileSize(Long.valueOf(media.length));
	}

	public MediaData(Long mediaId, String filename, Long fileSize,
			String duration, Date createdDate) {
		this.mediaId = mediaId;
		this.filename = filename;
		this.fileSize = fileSize;
		this.duration = duration;
		this.createdDate = createdDate;
	}

	public Long getMediaId() {
		return mediaId;
	}

	public void setMediaId(Long mediaId) {
		this.mediaId = mediaId;
	}

	public ItemGradingData getItemGradingData() {
		return itemGradingData;
	}

	public void setItemGradingData(ItemGradingData itemGradingData) {
		this.itemGradingData = itemGradingData;
	}

	public byte[] getMedia() {
		if (media == null && contentResource != null) {
			try {
				return contentResource.getContent();
			} catch (ServerOverloadException e) {
				return null;
			}
		}
		return media;
	}

	public void setMedia(byte[] media) {
		this.media = media;
	}

	public byte[] getDbMedia() {
		return media;
	}

	public void setDbMedia(byte[] media) {
		this.media = media;
	}

	public ContentResource getContentResource() {
		return contentResource;
	}

	public void setContentResource(ContentResource contentResource) {
		this.contentResource = contentResource;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public String getFileSizeKBFormat() {
		String fileSizeKBStr = "";
		if (fileSize != null) {
			double fileSizeKB = fileSize.doubleValue() / 1000;
			DecimalFormat nf = new DecimalFormat();
			nf.setMaximumFractionDigits(2);
			nf.setDecimalSeparatorAlwaysShown(true);
			fileSizeKBStr = nf.format(fileSizeKB);
		}
		return fileSizeKBStr;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String pdescription) {
		description = pdescription;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean getIsLink() {
		return isLink;
	}

	public void setIsLink(boolean isLink) {
		this.isLink = isLink;
	}

	public boolean getIsHtmlInline() {
		return isHtmlInline;
	}

	public void setIsHtmlInline(boolean isHtmlInline) {
		this.isHtmlInline = isHtmlInline;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return this.lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	// convenient method
	private boolean durationIsOver;

	public boolean getDurationIsOver() {
		return durationIsOver;
	}

	public void setDurationIsOver(boolean durationIsOver) {
		this.durationIsOver = durationIsOver;
	}

	private String timeAllowed;

	public String getTimeAllowed() {
		return timeAllowed;
	}

	public void setTimeAllowed(String timeAllowed) {
		this.timeAllowed = timeAllowed;
	}

	@Override
	public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1,31);
        builder.append(createdBy);
        builder.append(createdDate);
        builder.append(description);
        builder.append(duration);
        builder.append(durationIsOver);
        builder.append(fileSize);
        builder.append(filename);
        builder.append(isHtmlInline);
        builder.append(isLink);
        builder.append(itemGradingData);
        builder.append(lastModifiedBy);
        builder.append(lastModifiedDate);
        builder.append(location);
        builder.append(media);
        builder.append(mediaId);
        builder.append(mimeType);
        builder.append(status);
        builder.append(timeAllowed);
        return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaData other = (MediaData) obj;
		EqualsBuilder builder = new EqualsBuilder();
		builder.appendSuper(super.equals(obj));
		builder.append(createdBy, other.createdBy);
		builder.append(createdDate, other.createdDate);
		builder.append(description, other.description);
		builder.append(duration, other.duration);
		builder.append(durationIsOver, other.durationIsOver);
		builder.append(fileSize, other.fileSize);
		builder.append(filename, other.filename);
		builder.append(isHtmlInline, other.isHtmlInline);
		builder.append(isLink, other.isLink);
		builder.append(itemGradingData, other.itemGradingData);
		builder.append(lastModifiedBy, other.lastModifiedBy);
		builder.append(lastModifiedDate, other.lastModifiedDate);
		builder.append(location, other.location);
		builder.append(media, other.media);
		builder.append(mediaId, other.mediaId);
		builder.append(mimeType, other.mimeType);
		builder.append(status, other.status);
		builder.append(timeAllowed, other.timeAllowed);
		return builder.isEquals();
	}
}
