/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.service;

import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.edu.person.PhotoService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class FileSystemPhotoService implements PhotoService {

	private static final Log log = LogFactory.getLog(FileSystemPhotoService.class);

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

	public byte[] getPhotoAsByteArray(String userId) {
		String basepath = serverConfigurationService.getString("photo.directory", "/photos");
		String filename = basepath + "/" + userId + ".jpg";

		try {
			User user = userDirectoryService.getUser(userId);
			userId = user.getEid();
		
			String firstLetter = userId.substring(0,1);
            String secondLetter = userId.substring(1,2);
			filename = basepath + "/" + firstLetter + "/" + secondLetter + "/" + userId + ".jpg";
			
		}
		catch (UserNotDefinedException ee) {
			log.error("Could not find user: " + userId);
		}

		File file = new File(filename);

		try {
			return getBytesFromFile(file);
		}
		catch (IOException e) {
			log.error("Could not find file: " + filename);
			return null;
		}
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	public boolean overRidesDefault() {
		return false;
	}

	/**
	 * UNIMPLEMENTED
	 */
	public void savePhoto(byte[] arg0, String arg1) {
	}
}

