package org.sakaiproject.umem.api;

import java.util.TimeZone;

/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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


/**
 * <P>
 * This is an interface to provides all necessary methods, which are depend on
 * the Sakai Services. This will allow the separation of User Membership Tool and the
 * Sakai Tools
 * </P>
 */
public interface SakaiFacade {
	/**
	 * get a TimeService object from one of the Sakai services
	 * 
	 * @return a TimeService object.
	 */
	public TimeZone getLocalTimeZone();

}
