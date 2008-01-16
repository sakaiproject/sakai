/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.ILaunchData;

public interface ContentPackageManifest extends Serializable {
	
	//public String getTitle();

	//public void setTitle(String title);

	//public Document getDocument();

	//public void setDocument(Document manifest);

	public void setLaunchData(List l);

	public List getLaunchData();

	public ILaunchData getLaunchData(String identifier);
	
	/*
	 * CommentsFromLMS appears to be only set via a LMS UI, 
	 * and *not* from the manifest. A null return value from
	 * the getter (and not an empty Map) indicates lack of them.
	 * 
	 */

	public Map getCommentsFromLMS();

	public void setCommentsFromLMS(Map mapOfCommentLists);

	public ISeqActivityTree getActTreePrototype();

	public void setActTreePrototype(ISeqActivityTree actTreePrototype);

	//public String getControlMode();

	//public void setControlMode(String mode);
	
	//public String getResourceId();
	
	//public void setResourceId(String id);
}
