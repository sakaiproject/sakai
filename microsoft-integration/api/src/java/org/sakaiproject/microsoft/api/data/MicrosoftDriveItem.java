package org.sakaiproject.microsoft.api.data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftDriveItem implements Comparable<MicrosoftDriveItem>{
	private static final String DEFAULT_CREATED_BY = "SharePoint App";
	public static enum TYPE { 
		FOLDER(""),
		DOC_WORD(".docx"),
		DOC_PPT(".pptx"),
		DOC_EXCEL(".xlsx");
		
		@Getter
		private String ext;
		
		private TYPE(String ext) {
			this.ext = ext;
		}
	}
	
	private static final String ROOT_PATH = "^.*?/root:";

	private String id;
	private String name;
	private String url;
	private String driveId;
	
	private ZonedDateTime createdAt;
	private ZonedDateTime modifiedAt;
	private String modifiedBy;

	private MicrosoftDriveItem parent;
	
	private String path;
	private int depth;
	
	//FILE
	private Long size;
	private String mimeType;
	private String downloadURL;
	private String thumbnail;
	private String linkURL;

	//FOLDER
	@Builder.Default
	private boolean folder = false;
	@Builder.Default
	public Integer childCount = 0;
	@Builder.Default
	private boolean expanded = false;
	
	//SHARED
	@Builder.Default
	private boolean shared = false;
	
	//initially null/empty. Will be filled later when exploring the tree
	@Builder.Default
	private List<MicrosoftDriveItem> children = null;
	
	//custom setter, we will set the parent (this) to each children
	public void setChildren(List<MicrosoftDriveItem> children) {
		this.children = children;
		if(children != null) {
			children.stream().forEach(i -> i.setParent(this));
			childCount = children.size();
		}
	}
	
	public void addChild(MicrosoftDriveItem child) {
		if(child != null) {
			if(children == null) {
				children = new ArrayList<MicrosoftDriveItem>();
			}
			children.add(child);
			child.setParent(this);
			childCount = children.size();
		}
	}
	
	public void removeChild(MicrosoftDriveItem child) {
		if(children != null) {
			children.remove(child);
			childCount = children.size();
		}
	}
	
	//custom getter for modifiedBy. DriveItems created with Microsoft Graph API (application permissions) are allways authored as "SharePoint App"
	//in these cases, we will show it empty
	public String getModifiedBy() {
		if(modifiedBy != null && modifiedBy.equalsIgnoreCase(DEFAULT_CREATED_BY)) {
			return "";
		}
		return modifiedBy;
	}
	
	//when the MicrosoftDriveItem is created, we have the number of children but not the children themselves
	public boolean hasChildren() {
		return folder && childCount > 0;
	}
	
	@Override
	public int compareTo(MicrosoftDriveItem o) {
		if(this.isFolder() == o.isFolder()) {
			return this.getName().compareToIgnoreCase(o.getName());
		}
		if(this.isFolder()) {
			return -1;
		}
		return 1;
	}
	
	//custom builder
	public static class MicrosoftDriveItemBuilder{
		public MicrosoftDriveItemBuilder path(String path) {
			if(StringUtils.isNotBlank(path)) {
				String aux = path.replaceFirst(ROOT_PATH, "");
				this.path = aux;
				this.depth = aux.split("/").length - 1;
			} else {
				this.path = "";
				this.depth = 0;
			}
			
			return this;
		}
	}
}
