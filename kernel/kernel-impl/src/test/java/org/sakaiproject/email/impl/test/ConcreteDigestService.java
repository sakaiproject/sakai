/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.email.impl.test;

import org.sakaiproject.email.impl.BaseDigestService;

/** Just checks we don't need any missing methods as the main implementation is abstract.*/
public class ConcreteDigestService extends BaseDigestService {

	@Override
	protected Storage newStorage() {
		// TODO Auto-generated method stub
		return null;
	}

}
