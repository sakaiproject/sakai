package org.sakaiproject.scorm.service.api;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.adl.validator.IValidatorOutcome;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;

public interface ScormContentService {
		
	public ContentResource addManifest(ContentPackageManifest manifest, String id);
	
	public List getContentPackages();
	
	public ContentPackageManifest getManifest(String contentPackageId);
	
	public InputStream getManifestAsStream(String contentPackageId);
	
	public IValidatorOutcome validateContentPackage(File contentPackage, boolean onlyValidateManifest);
	
	public void uploadZipArchive(File zipArchive) throws IdUnusedException, IdUniquenessException, IdLengthException, IdInvalidException, OverQuotaException, PermissionException, ServerOverloadException;
	
	public String identifyZipArchive();
	
	//public void uploadZipEntry(File zipEntry, String path);
}
