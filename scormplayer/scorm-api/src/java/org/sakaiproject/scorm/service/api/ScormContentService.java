package org.sakaiproject.scorm.service.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.adl.validator.IValidatorOutcome;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ScormContentService {
		
	public ContentResource addManifest(ContentPackageManifest manifest, String id);
	
	public List getContentPackages();
	
	public ContentPackageManifest getManifest(String contentPackageId);
	
	public InputStream getManifestAsStream(String contentPackageId);
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean doValidateSchema);
	
	
}
