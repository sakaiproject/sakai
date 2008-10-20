package org.sakaiproject.scorm.dao.api;

import java.io.Serializable;

import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ContentPackageManifestDao {

	public ContentPackageManifest load(Serializable id);
	
	public Serializable save(ContentPackageManifest manifest);
	
}
