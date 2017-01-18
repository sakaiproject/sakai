/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.LaunchData;

public class ContentPackageManifest implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private ISeqActivityTree actTreePrototype;

	//private Map comments;
	private List launchDataList;

	private HashMap<String, LaunchData> launchDataMap;

	public ContentPackageManifest() {

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContentPackageManifest other = (ContentPackageManifest) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/*public Map getCommentsFromLMS() {
		return comments;
	}*/

	public ISeqActivityTree getActTreePrototype() {
		return actTreePrototype;
	}

	public Serializable getId() {
		return id;
	}

	public List getLaunchData() {
		return launchDataList;
	}

	/*public void setCommentsFromLMS(Map comments) {
		this.comments = comments;
	}*/

	public LaunchData getLaunchData(String identifier) {
		return launchDataMap.get(identifier);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setActTreePrototype(ISeqActivityTree actTreePrototype) {
		this.actTreePrototype = actTreePrototype;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLaunchData(List launchDataList) {
		launchDataMap = new HashMap<String, LaunchData>();

		for (int i = 0; i < launchDataList.size(); ++i) {
			LaunchData l = (LaunchData) launchDataList.get(i);

			launchDataMap.put(l.getItemIdentifier(), l);
		}

		this.launchDataList = launchDataList;
	}
}
