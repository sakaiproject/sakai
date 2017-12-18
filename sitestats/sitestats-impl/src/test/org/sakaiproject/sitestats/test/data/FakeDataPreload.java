/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.test.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@Slf4j
public class FakeDataPreload extends AbstractJUnit4SpringContextTests {

	public void init() {
		log.info("FakeDataPreload.init()");
	}
}
