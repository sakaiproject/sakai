/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2007 The Regents of the University of California
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.component.common.edu.person;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.edu.person.PhotoService;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;


/**
 * By default, roster photos come from the Profile service.
 */
public class SakaiPersonPhotoService implements PhotoService {
	private static final Log log = LogFactory.getLog(SakaiPersonPhotoService.class);
	
	private SakaiPersonManager sakaiPersonManager;

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.roster.PhotoService#getPhotoAsByteArray(java.lang.String)
	 */
	public byte[] getPhotoAsByteArray(String userId) {
		return null;
	}

	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager) {
		this.sakaiPersonManager = sakaiPersonManager;
	}

	public void savePhoto(byte[] data, String userId) {
		// TODO Auto-generated method stub
		
	}

	public boolean overRidesDefault() {
		// TODO Auto-generated method stub
		return false;
	}

}
