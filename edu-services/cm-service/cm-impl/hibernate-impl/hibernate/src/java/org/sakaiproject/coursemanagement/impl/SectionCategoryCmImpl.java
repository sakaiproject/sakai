/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl;

import org.sakaiproject.coursemanagement.api.SectionCategory;

public class SectionCategoryCmImpl implements SectionCategory {
	protected String categoryCode;
	protected String categoryDescription;
	
	public SectionCategoryCmImpl() {}
	
	public SectionCategoryCmImpl(String categoryCode, String categoryDescription) {
		this.categoryCode = categoryCode;
		this.categoryDescription = categoryDescription;
	}

	public String getCategoryCode() {
		return categoryCode;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getCategoryDescription() {
		return categoryDescription;
	}
	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((categoryCode == null) ? 0 : categoryCode.hashCode());
        result = prime * result
                + ((categoryDescription == null) ? 0 : categoryDescription.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SectionCategoryCmImpl other = (SectionCategoryCmImpl) obj;
        if (categoryCode == null) {
            if (other.categoryCode != null)
                return false;
        } else if (!categoryCode.equals(other.categoryCode))
            return false;
        if (categoryDescription == null) {
            if (other.categoryDescription != null)
                return false;
        } else if (!categoryDescription.equals(other.categoryDescription))
            return false;
        return true;
    }

	// replaced builders with eclipse equals/hashcode
//	public int hashCode() {
//		return new HashCodeBuilder(17, 37).append(categoryCode).append(categoryDescription).toHashCode();
//	}
//
//	public boolean equals(Object obj) {
//		SectionCategoryCmImpl other = null;
//		try {
//			other = (SectionCategoryCmImpl)obj;
//		} catch (ClassCastException cce) {
//			return false;
//		}
//		
//		return new EqualsBuilder().append(categoryCode, other.getCategoryCode()).
//			append(categoryDescription, other.getCategoryDescription()).isEquals();
//	}
	
}
