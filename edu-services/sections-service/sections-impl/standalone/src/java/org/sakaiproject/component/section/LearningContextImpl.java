/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.section;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sakaiproject.section.api.coursemanagement.LearningContext;

/**
 * A base class of LearningContexts for detachable persistent storage.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class LearningContextImpl extends AbstractPersistentObject implements
		LearningContext, Serializable {

	private static final long serialVersionUID = 1L;
	
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof LearningContextImpl) {
			LearningContextImpl other = (LearningContextImpl)o;
			return new EqualsBuilder()
				.append(uuid, other.uuid)
				.isEquals();
		}
		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(uuid)
			.toHashCode();
	}


}
