package org.sakaiproject.scorm.dao.api;

import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ContentPackageManifestDao {

	public ContentPackageManifest find(String courseId);
	
}
