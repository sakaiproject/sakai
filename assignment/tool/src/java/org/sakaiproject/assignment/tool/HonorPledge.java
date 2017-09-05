/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.tool;

import org.sakaiproject.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HonorPledge {
	private static ResourceLoader rb = new ResourceLoader("honorpledge");
	private static ResourceLoader rb2 = new ResourceLoader("assignment");
	protected static final Logger log = LoggerFactory.getLogger(HonorPledge.class);
	
	/**
	 * @return
	 */
	public static String honorPledge2() {
		
		String hp2 = null; 
		hp2 = rb.getString("gen.honple2");

		if ("[missing key (mre): honorpledge gen.honple2]".equals(hp2)) {
			log.debug("gen.honple2 null!");
			hp2 = rb2.getString("gen.honple2");
		}
		
		return hp2;
	}

}
