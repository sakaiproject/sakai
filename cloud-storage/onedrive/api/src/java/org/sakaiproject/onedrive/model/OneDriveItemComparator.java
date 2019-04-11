package org.sakaiproject.onedrive.model;

import java.util.Comparator;

public class OneDriveItemComparator implements Comparator<OneDriveItem> {
    @Override
    public int compare(OneDriveItem o1, OneDriveItem o2) {
    	String o1Name = o1.getOneDriveItemId();
		String o2Name = o2.getOneDriveItemId();
		if(!o1.isFolder()){
			o1Name = o1.getParent().getParentId() + o1Name;
		}
		if(!o2.isFolder()){
			o2Name = o2.getParent().getParentId() + o2Name;
		}
        return o1Name.compareTo(o2Name);
    }
}
