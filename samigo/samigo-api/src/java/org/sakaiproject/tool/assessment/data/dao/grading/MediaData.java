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
		return media;
	}

	public void setMedia(byte[] media) {
		this.media = media;
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
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result
				+ ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + (durationIsOver ? 1231 : 1237);
		result = prime * result
				+ ((fileSize == null) ? 0 : fileSize.hashCode());
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + (isHtmlInline ? 1231 : 1237);
		result = prime * result + (isLink ? 1231 : 1237);
		result = prime * result
				+ ((itemGradingData == null) ? 0 : itemGradingData.hashCode());
		result = prime * result
				+ ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
		result = prime
				* result
				+ ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + Arrays.hashCode(media);
		result = prime * result + ((mediaId == null) ? 0 : mediaId.hashCode());
		result = prime * result
				+ ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((timeAllowed == null) ? 0 : timeAllowed.hashCode());
		return result;
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
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (durationIsOver != other.durationIsOver)
			return false;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (isHtmlInline != other.isHtmlInline)
			return false;
		if (isLink != other.isLink)
			return false;
		if (itemGradingData == null) {
			if (other.itemGradingData != null)
				return false;
		} else if (!itemGradingData.equals(other.itemGradingData))
			return false;
		if (lastModifiedBy == null) {
			if (other.lastModifiedBy != null)
				return false;
		} else if (!lastModifiedBy.equals(other.lastModifiedBy))
			return false;
		if (lastModifiedDate == null) {
			if (other.lastModifiedDate != null)
				return false;
		} else if (!lastModifiedDate.equals(other.lastModifiedDate))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (!Arrays.equals(media, other.media))
			return false;
		if (mediaId == null) {
			if (other.mediaId != null)
				return false;
		} else if (!mediaId.equals(other.mediaId))
			return false;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (timeAllowed == null) {
			if (other.timeAllowed != null)
				return false;
		} else if (!timeAllowed.equals(other.timeAllowed))
			return false;
		return true;
	}
}
