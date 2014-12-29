/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.siteassociation.impl;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class AssociationImpl implements Serializable {

	private static final long serialVersionUID = -3351535938426125345L;

	protected String fromContext, toContext;

	protected Integer version;

	public AssociationImpl() {
	}

	public AssociationImpl(String fromContext, String toContext) {
		this.fromContext = fromContext;
		this.toContext = toContext;
	}

	protected Integer getVersion() {
		return version;
	}

	protected void setVersion(Integer version) {
		this.version = version;
	}

	public String getFromContext() {
		return fromContext;
	}

	protected void setFromContext(String fromContext) {
		this.fromContext = fromContext;
	}

	public String getToContext() {
		return toContext;
	}

	protected void setToContext(String toContext) {
		this.toContext = toContext;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder sb = new ToStringBuilder(this);
		sb.append("version", version);
		sb.append("fromContext", fromContext);
		sb.append("toContext", toContext);
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AssociationImpl)) {
			return false;
		}
		AssociationImpl other = (AssociationImpl) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(
				fromContext, other.getFromContext()).append(toContext,
				other.getToContext()).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(111, 73).append(fromContext).append(
				toContext).toHashCode();
	}
}
