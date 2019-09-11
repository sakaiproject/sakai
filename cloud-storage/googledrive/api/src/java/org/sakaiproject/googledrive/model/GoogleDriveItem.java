package org.sakaiproject.googledrive.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class GoogleDriveItem {

    private String googleDriveItemId;
    private String name;    
    private Long size;
    private String downloadUrl;
    private String viewUrl;
    private String mimeType;
    private String parentId;
    private String icon;
    private String thumbnail;

    private boolean folder = false;
    public boolean isFolder() {
        return folder;
    }
    private boolean children = false;
    public boolean hasChildren() {
        return isFolder() && children;
    }

    private int depth = 0;
    private boolean expanded = false;

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof GoogleDriveItem){
            GoogleDriveItem gdi = (GoogleDriveItem) obj;
            return this.googleDriveItemId.equals(gdi.getGoogleDriveItemId());
        }
        return retVal;
    }
}
