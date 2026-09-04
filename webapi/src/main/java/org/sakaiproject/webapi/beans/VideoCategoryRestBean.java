package org.sakaiproject.webapi.beans;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.sakaiproject.videotraining.api.model.VideoTrainingCategory;

import lombok.Data;

@Data
public class VideoCategoryRestBean {
	private String id;
	private String name;
	private String parentCategoryId;
	private Instant createdOn;
	private String createdBy;
	private Instant modifiedOn;
	private String modifiedBy;
	private Long videoCount;
	private boolean hasChildren;
	private List<VideoCategoryRestBean> children;

	public VideoCategoryRestBean(VideoTrainingCategory category) {
		this.id = category.getId();
		this.name = category.getName();
		this.parentCategoryId = category.getParentCategoryId();
		this.createdOn = category.getCreatedOn();
		this.createdBy = category.getCreatedBy();
		this.modifiedOn = category.getModifiedOn();
		this.modifiedBy = category.getModifiedBy();
		this.videoCount = category.getVideoCount();
		this.hasChildren = category.isHasChildren();
		this.children = category.getChildren() == null
			? Collections.emptyList()
			: category.getChildren().stream().map(VideoCategoryRestBean::new).collect(Collectors.toList());
	}
}
