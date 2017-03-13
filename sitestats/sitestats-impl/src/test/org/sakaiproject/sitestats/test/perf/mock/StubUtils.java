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
