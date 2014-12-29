package org.sakaiproject.component.common.edu.person;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.edu.person.BasePhotoService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class FileSystemPhotoService extends BasePhotoService {
	private static final Log LOG = LogFactory.getLog(FileSystemPhotoService.class);
	
	private String photoRepositoryPath = null;
	
	/**
	 * Setters
	 *
	 */
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService scs) {
		serverConfigurationService = scs;
	}
	
	private UserDirectoryService userDirectoryService;
	/**
	 * @param userDirectoryService
	 *        The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setUserDirectoryService(userDirectoryService " + userDirectoryService + ")");
		}

		this.userDirectoryService = userDirectoryService;
	}
	
	
	
	public void init() {
		photoRepositoryPath = serverConfigurationService.getString("profile.photoRepositoryPath", null);
	}
	
	public byte[] getPhotoAsByteArray(String userId) {
		// TODO Auto-generated method stub
		LOG.debug("getPhotoAsByteArray(" + userId +") repo path" + this.photoRepositoryPath );
		return this.getInstitutionalPhotoFromDiskRespository(userId);
	}

	

	public void savePhoto(byte[] data, String userId) {
		this.savePhotoToDiskRepository(data, userId);
		
	}
	
	private byte[] getInstitutionalPhotoFromDiskRespository(String uid) {
		
		LOG.debug("fetching photo's from: " + photoRepositoryPath);
			if(photoRepositoryPath != null) {
				
				FileInputStream fileInput = null;
				
				try {
				
					String eid = userDirectoryService.getUserEid(uid);
					
					String photoPath = photoRepositoryPath+"/"+eid+".jpg";
					
					LOG.debug("Get photo from disk: "+photoPath);
				
					File file = new File(photoPath);
				
					byte[] bytes = new byte[(int)file.length()];
				
		            // Open an input stream
		            fileInput = new FileInputStream (file);
					
		            // Read in the bytes
		            int offset = 0;
		            int numRead = 0;
		            while (offset < bytes.length
		                   && (numRead=fileInput.read(bytes, offset, bytes.length-offset)) >= 0) {
		                offset += numRead;
		            }
		        
		            // Ensure all the bytes have been read in
		            if (offset < bytes.length) {
		                throw new IOException("Could not completely read file :"+file.getName());
		            }
		        
		           return bytes;
		
				} catch (FileNotFoundException e) {
					// file not found, this user does not have a photo ID on file
					LOG.debug("FileNotFoundException: "+e);
				} catch (IOException e) {
					LOG.error("IOException: "+e);
				} catch (UserNotDefinedException e) {
					LOG.debug("UserNotDefinedException: "+e);
				} finally {
					// Close the input stream 
			        try {
			        	if(fileInput != null) fileInput.close();
					} catch (IOException e) {
						LOG.error("Exception in finally block: "+e);
					}
				}
			}
			return null;
	}
	
	private void savePhotoToDiskRepository(byte[] photo, String uid) {
		if (photoRepositoryPath != null ) {
			if (photo == null || photo.length == 0)
				return;
			
			FileOutputStream fileOutput = null;
			try {
				String eid = userDirectoryService.getUserEid(uid);
				String photoPath = photoRepositoryPath+"/"+eid+".jpg";
				fileOutput = new FileOutputStream(photoPath);
				fileOutput.write(photo);
			}
			catch (UserNotDefinedException e) {
				LOG.debug("UserNotDefinedException: "+e);
			} catch (FileNotFoundException e) {
				// file not found, this user does not have a photo ID on file
				LOG.debug("FileNotFoundException: "+e);
			} catch (IOException e) {
				LOG.error("IOException: "+e);
			} finally {
				// Close the input stream 
		        try {
		        	if(fileOutput != null) fileOutput.close();
				} catch (IOException e) {
					LOG.error("Exception in finally block: "+e);
				}
			}
			
			
		}
	}
	
}



