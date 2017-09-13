/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.sitestats.test.perf.mock;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.StubMethod;

import static net.bytebuddy.matcher.ElementMatchers.isAbstract;

/**
 * Simple utility method to stub out a class. This means we don't bind against all the huge interfaces
 * and the tests are much clearer as it shows the methods they use/need.
 */
public class StubUtils {

	public static <T> T stubClass(Class<T> aClass) {
		try {
			return new ByteBuddy()
					.subclass(aClass)
					.method(isAbstract()).intercept(StubMethod.INSTANCE)
					.make()
					.load(MockSiteService.class.getClassLoader())
					.getLoaded()
					.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
