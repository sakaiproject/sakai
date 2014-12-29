package org.sakaiproject.sitestats.test.perf.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class NoOpInvokationHandler implements InvocationHandler{

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Class<?> returnType = method.getReturnType();
		if (returnType.isAssignableFrom(Number.class) || returnType.isAssignableFrom(int.class)) {
			return 0;
		} else if (returnType.isAssignableFrom(Boolean.class) || returnType.isAssignableFrom(boolean.class)) {
			return false;
		}
		
		return null;
	}

}
