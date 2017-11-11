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
package org.sakaiproject.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Thin wrapper around the JDK's interface to javac.
 * 
 * @author dmccallum@unicon.net
 *
 */
public class Compiler {
	
	private Object jdkCompiler;
	boolean isJavaxCompiler = false;
	
	public Compiler() {
		refresh();
	}
	
	public boolean isUseable() {
		return jdkCompiler != null;
	}
	
	public boolean refresh() {
		try {
			Class toolProvider = getClass().getClassLoader().loadClass("javax.tools.ToolProvider");
			jdkCompiler = toolProvider.getMethod("getSystemJavaCompiler").invoke(null);
			isJavaxCompiler = true;
		} catch ( Exception e ) {
			// fall back to trying for the com.sun compiler
			throw new RuntimeException(e);
		}

		if ( jdkCompiler == null ) {
			try {
				jdkCompiler = loadClass("com.sun.tools.javac.Main");
			} catch ( ClassNotFoundException e ) {
			}
		}
		return isUseable();
	}
	
	public void compile(File srcFile, File outDir, PrintWriter log) {
		if ( !(isUseable()) ) {
			throw new IllegalStateException("Could not load JDK compiler");
		}
		if ( isJavaxCompiler ) {
			compileWithJavaxCompiler(srcFile, outDir, log);
		} else {
			compileWithSunCompiler(srcFile, outDir, log);
		}
	}

	private void compileWithSunCompiler(File srcFile, File outDir, PrintWriter log) {
		try {
			Method jdkCompile = ((Class)jdkCompiler).getMethod("compile", String[].class, PrintWriter.class);
			String[] options = { "-g",
					"-source",
					"1.5",
					"-target",
					"1.5",
					"-d",
					outDir.getAbsolutePath(),
					srcFile.getAbsolutePath()};
			jdkCompile.invoke(null, options, log);
		} catch ( NoSuchMethodException e ) {
			throw new UnsupportedOperationException("Unable to locate JDK compile() method", e);
		} catch ( IllegalAccessException e) {
			throw new UnsupportedOperationException("Unable to invoke JDK compile() method", e);
		} catch ( InvocationTargetException e ) {
			throw new UnsupportedOperationException("Unable to invoke JDK compile() method", e);
		}
	}

	private void compileWithJavaxCompiler(File srcFile, File outDir, PrintWriter log) {
		try {
			Class diagnosticListenerClass = loadClass("javax.tools.DiagnosticListener");
			Object fileManager = jdkCompiler.getClass().getDeclaredMethod("getStandardFileManager",
					diagnosticListenerClass, Locale.class, Charset.class).invoke(jdkCompiler, null, null, null);

			Class fileManagerClass = loadClass("javax.tools.JavaFileManager");
			List<String> options = Arrays.asList(new String[] { "-d", outDir.getAbsolutePath() });
			Object compilationUnits =
					fileManager.getClass().getDeclaredMethod("getJavaFileObjectsFromFiles", Iterable.class)
							.invoke(fileManager, Arrays.asList(srcFile));
			Method getTaskMethod =
					jdkCompiler.getClass().getDeclaredMethod("getTask",
							Writer.class, fileManagerClass, diagnosticListenerClass,
							Iterable.class, Iterable.class, Iterable.class);

			@SuppressWarnings("unchecked")
			Callable<Boolean> compileTask = (Callable<Boolean>)
					getTaskMethod.invoke(jdkCompiler, null, fileManager, null, options, null, compilationUnits);
			if ( !(compileTask.call()) ) {
				throw new RuntimeException("Compilation failed");
			}
		} catch ( Exception e ) {
			throw new UnsupportedOperationException("Unable to execute JavaCompiler", e);
		}
	}

	private Class loadClass(String name) throws ClassNotFoundException {
		return getClass().getClassLoader().loadClass(name);
	}

}
