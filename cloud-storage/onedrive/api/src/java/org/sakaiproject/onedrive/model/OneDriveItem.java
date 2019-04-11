package org.sakaiproject.onedrive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneDriveItem {

    @JsonProperty("id")
    private String oneDriveItemId;

    private String name;
    
    private Integer size;
	
    @JsonProperty(value = "@microsoft.graph.downloadUrl")//this is always public
    //@JsonProperty(value = "webUrl")//this checks against onedrive permissions
    private String downloadUrl;
    
    private OneDriveFolder folder;
    private OneDriveFile file;

    @JsonProperty(value = "parentReference")
    private OneDriveParent parent;

    public boolean isFolder() {
        return folder != null;
    }
    public boolean hasChildren() {
        return isFolder() && folder.childCount != 0;
    }
	
    private int depth = 0;
    private boolean expanded = false;

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof OneDriveItem){
            OneDriveItem ptr = (OneDriveItem) obj;
            return this.oneDriveItemId.equals(ptr.getOneDriveItemId());
        }
        return retVal;
    }
}
