package org.sakaiproject.microsoft.api.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MicrosoftDriveItem implements Comparable<MicrosoftDriveItem>{
	
	private static final String ROOT_PATH = "/drive/root:";
	private static final String DRIVE_ROOT_PATH = "/drives/[^/]+/root:";

	private String id;
	private String name;
	private String url;
	private String driveId;

	private int depth;
	
	//FILE
	private Long size;
	private String mimeType;
	private String downloadURL;

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
		public MicrosoftDriveItemBuilder processPath(String path) {
			if(StringUtils.isNotBlank(path)) {
				String aux = path.replaceFirst(ROOT_PATH, "");
				aux = aux.replaceFirst(DRIVE_ROOT_PATH, "");
				this.depth = aux.split("/").length - 1;
			} else {
				this.depth = 0;
			}
			return this;
		}
	}
}
