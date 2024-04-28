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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.adl.sequencer.ISeqActivityTree;
import org.adl.validator.contentpackage.LaunchData;

@EqualsAndHashCode
public class ContentPackageManifest implements Serializable
{
	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include @Getter @Setter private Long id;
	@Getter @Setter private ISeqActivityTree actTreePrototype;
	@Getter private List launchDataList;
	private HashMap<String, LaunchData> launchDataMap;

	public List getLaunchData()
	{
		return launchDataList;
	}

	public LaunchData getLaunchData(String identifier)
	{
		return launchDataMap.get(identifier);
	}

	public void setLaunchData(List launchDataList)
	{
		launchDataMap = new HashMap<>();
		for (int i = 0; i < launchDataList.size(); ++i)
		{
			LaunchData l = (LaunchData) launchDataList.get(i);
			launchDataMap.put(l.getItemIdentifier(), l);
		}

		this.launchDataList = launchDataList;
	}
}
