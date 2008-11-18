package org.sakaiproject.util;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Thin wrapper around the JDK's interface to javac.
 * 
 * @author dmccallum@unicon.net
 *
 */
public class Compiler {
	
	private Class jdkCompiler;
	
	public Compiler() {
		refresh();
	}
	
	public boolean isUseable() {
		return jdkCompiler != null;
	}
	
	public boolean refresh() {
		try {
			jdkCompiler = getClass().getClassLoader().loadClass("com.sun.tools.javac.Main");
		} catch ( ClassNotFoundException e ) {
		}
		return isUseable();
	}
	
	public void compile(String[] args, PrintWriter log) {
		if ( !(isUseable()) ) {
			throw new IllegalStateException("Could not load JDK compiler");
		}
		// Have to use reflection b/c Eclipse does not want to put tools.jar on the
		// code-time classpath without introducing a new classpath variable.
		try {
			Method jdkCompile = jdkCompiler.getMethod("compile", String[].class, PrintWriter.class);
			jdkCompile.invoke(null, args, log);
		} catch ( NoSuchMethodException e ) {
			throw new UnsupportedOperationException("Unable to locate JDK compile() method", e);
		} catch ( IllegalAccessException e) {
			throw new UnsupportedOperationException("Unable to invoke JDK compile() method", e);
		} catch ( InvocationTargetException e ) {
			throw new UnsupportedOperationException("Unable to invoke JDK compile() method", e);
		}
	}
	
}
