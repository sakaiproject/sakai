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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.ILaunchData;
import org.adl.validator.contentpackage.LaunchData;

public class ContentPackageManifest implements Serializable {
	
	private static final long serialVersionUID = 1L; 

	private Serializable id;
	private ISeqActivityTree actTreePrototype;
	//private Map comments;
	private List launchDataList;
	private HashMap<String, LaunchData> launchDataMap;
	
	public ContentPackageManifest() {
		
	}
	
	public ISeqActivityTree getActTreePrototype() {
		return actTreePrototype;
	}

	/*public Map getCommentsFromLMS() {
		return comments;
	}*/

	public LaunchData getLaunchData(String identifier) {
		return launchDataMap.get(identifier);
	}
	
	public List getLaunchData() {
		return launchDataList;
	}

	public void setActTreePrototype(ISeqActivityTree actTreePrototype) {
		this.actTreePrototype = actTreePrototype;
	}

	/*public void setCommentsFromLMS(Map comments) {
		this.comments = comments;
	}*/

	public void setLaunchData(List launchDataList) {
		launchDataMap = new HashMap<String, LaunchData>();
		
		for(int i=0;i<launchDataList.size();++i){
			LaunchData l = (LaunchData)launchDataList.get(i);
		
			launchDataMap.put(l.getItemIdentifier(),l);
		}	
		
		this.launchDataList = launchDataList;
	}

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}
}
