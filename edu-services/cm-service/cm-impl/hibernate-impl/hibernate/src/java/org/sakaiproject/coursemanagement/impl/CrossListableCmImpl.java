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

/**
 * A CrossListableCmImpl is a CM entity that can be cross-listed.  This does not belong
 * in the API, since the CrossListingCmImpl object is specific to the hibernate implementation.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class CrossListableCmImpl extends AbstractMembershipContainerCmImpl {

	private static final long serialVersionUID = 1L;
	public abstract CrossListingCmImpl getCrossListing();
	public abstract void setCrossListing(CrossListingCmImpl crossListingCmImpl);
}
