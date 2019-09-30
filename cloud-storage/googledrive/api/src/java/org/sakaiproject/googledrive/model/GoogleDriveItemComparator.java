package org.sakaiproject.googledrive.model;

import java.util.Comparator;

public class GoogleDriveItemComparator implements Comparator<GoogleDriveItem> {
    @Override
    public int compare(GoogleDriveItem o1, GoogleDriveItem o2) {
        String o1Name = o1.getGoogleDriveItemId();
        String o2Name = o2.getGoogleDriveItemId();
        if(!o1.isFolder()){
            o1Name = o1.getParentId() + o1Name;
        }
        if(!o2.isFolder()){
            o2Name = o2.getParentId() + o2Name;
        }
        return o1Name.compareTo(o2Name);
    }
}
