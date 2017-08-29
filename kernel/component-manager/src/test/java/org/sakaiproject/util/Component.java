/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * <p>
 * Represents a single Sakai component directory. Given an ID, a base dir,
 * and a {@link Compiler}, can generate its own directory layout and can
 * generate a near-guaranteed unique Java class and associated bean definition.
 * </p>
 * <p>
 * It can also generate additional JARs in the component if supplied.
 * </p>
 * <p>
 * Cannot be instantiated if a component of the same ID has already been
 * created below the specified root directory. Cannot invoke {@link #generate()}
 * multiple times unless the underlying file system has been edited out-of-band.
 * </p>
 *  
 * @author dmccallum@unicon.net
 *
 */
public class Component {

	private static final String CLASS_SRC_TEMPLATE = 
		"package %1$s;\n" + 
		"public class %2$s { }";
	private static final String BEAN_DEF_TEMPLATE_HEADER =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\n" +
		"<beans>\n";
	private static final String BEAN_DEF_TEMPLATE_BEAN =
			"\t<bean id=\"%1$s\" class=\"%1$s\" />\n";
	private static final String BEAN_DEF_TEMPLATE_FOOTER =
			"</beans>";
	private String id;
	private Jar[] jars;
	private File componentsRootDir;
	private File componentDir;
	private File webInfDir;
	private File classesDir;
	private File libDir;
	private File srcDir;
	private File dstDir;
	private String pkgName;
	private String className;
	private File srcPkgDir;
	private File classSrcFile;
	private File beanDefsFile;
	private Compiler compiler;

	public class Jar {

		private String name;
		private String className;
		private String classSrc;
		private File srcPkgDir;
		private File classSrcFile;

		public Jar(String name) {
			this.name = name;
		}

		public String getBeanId() {
			return getBeanClass();
		}

		protected String getBeanClass() {
			if ( pkgName == null || className == null ) {
				return null;
			}
			return pkgName + "." + className;
		}

		protected void generateJavaSource() {
			// Assume that folders are already created.
			className = "ServiceImpl"+name + id;
			String pkgAsPath = pkgName.replace(".", File.separator);
			srcPkgDir = mkdir(srcDir, pkgAsPath);

			classSrc = String.format(CLASS_SRC_TEMPLATE, pkgName, className);
			classSrcFile = writeFile(srcPkgDir, className + ".java", classSrc);
		}

		protected String generateBeanDefs() {
			return String.format(BEAN_DEF_TEMPLATE_BEAN, getBeanClass());
		}

		protected void compile() {
			(Component.this).compile(classSrcFile);
		}

		protected void move() {
			try {
				JarBuilder builder = new JarBuilder(new File(libDir, name +".jar").getAbsolutePath());
				builder.start();
				String pkgAsPath = pkgName.replace(".", File.separator);
				builder.add(dstDir.getAbsolutePath(), pkgAsPath+ File.separator+ className+ ".class", true);
				builder.stop();
			} catch (IOException ioe) {
				throw new IllegalStateException("Failed to create JAR", ioe);
			}

		}
	}
	
	public Component(String id, String componentsRootDir, Compiler compiler, String... jars) {
		if ( id == null ) {
			throw new IllegalArgumentException("Must specify an ID");
		}
		this.id = id;
		this.componentsRootDir = new File(componentsRootDir);
		if ( !(this.componentsRootDir.exists()) ) {
			throw new IllegalArgumentException("Specified components root dir (" + componentsRootDir + 
					") does not exist");
		}
		File proposedComponentDir = 
			new File(componentsRootDir, calcComponentName());
		if ( proposedComponentDir.exists() ) {
			throw new IllegalArgumentException("Proposed component dir (" + proposedComponentDir + 
					") already exists");
		}
		if (compiler == null) {
			throw new IllegalArgumentException("Must specify a Compiler");
		}
		this.compiler = compiler;
		this.jars = new Jar[jars.length];
		for (int i = 0; i < jars.length; i++) {
			this.jars[i] =  new Jar(jars[i]);
		}
	}

	public String getBeanId() {
		return getBeanClass();
	}

	public Jar[] getJars() {
		return jars;
	}
	
	public String getDir() {
		return componentDir.getAbsolutePath();
	}

	protected String getBeanClass() {
		if ( pkgName == null || className == null ) {
			return null;
		}
		return pkgName + "." + className;
	}

	public void generate() {
		if ( componentDir != null && componentDir.exists() ) {
			throw new IllegalStateException("Component already exists at " + componentDir);
		}
		layout();
		compile(classSrcFile);
		move(dstDir, classesDir);
		for (Jar jar : jars) {
			jar.compile();
			jar.move();
		}
	}

	protected void layout() {
		makeComponentDir();
		layoutWebapp();
		generateJavaSource();
		generateBeanDefs();
	}

	protected void makeComponentDir() {
		this.componentDir = mkdir(componentsRootDir, calcComponentName());
	}
	
	protected String calcComponentName() {
		return "sakai-" + id + "-pack";
	}

	protected void layoutWebapp() {
		String path = componentDir.getPath() + File.separatorChar + "WEB-INF";
		webInfDir = mkdir(componentDir, "WEB-INF");
		classesDir = mkdir(webInfDir, "classes");
		libDir = mkdir(webInfDir, "lib");
	}
	
	protected File mkdir(File parent, String name) {
		File dir = new File(parent, name);
		if ( !(dir.isDirectory() || dir.mkdirs()) ) {
			throw new IllegalStateException("Unable to create dir at " + dir.getPath());
		}
		return dir;
	}
	
	protected void generateJavaSource() {
		srcDir = mkdir(componentDir, "src");
		dstDir = mkdir(componentDir, "dst");
		pkgName = getClass().getPackage().getName();
		String pkgAsPath = 
			pkgName.replace(".", File.separator);
		srcPkgDir = mkdir(srcDir, pkgAsPath);
		className = "ServiceImpl" + id;
		String classSrc = String.format(CLASS_SRC_TEMPLATE, pkgName, className);
		classSrcFile = writeFile(srcPkgDir, className + ".java", classSrc);
		for(Jar jar : jars) {
			jar.generateJavaSource();
		}
	}
	
	protected void generateBeanDefs() {
		String beanDefs = BEAN_DEF_TEMPLATE_HEADER;
		beanDefs += String.format(BEAN_DEF_TEMPLATE_BEAN, getBeanClass());
		for(Jar jar : jars) {
			beanDefs += jar.generateBeanDefs();
		}
		beanDefs += BEAN_DEF_TEMPLATE_FOOTER;
		beanDefsFile = writeFile(webInfDir, "components.xml", beanDefs);
	}
	
	protected File writeFile(File parentDir, String filePath, String fileContent) {
		File file = new File(parentDir, filePath);
		if (!file.exists()) {
			try {
				if (!(file.createNewFile())) {
					throw new IllegalStateException("Unable to create file at " + file);
				}
			} catch ( IOException e ) {
				throw new IllegalStateException("Unable to create file at " + file, e);
			}
		}
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(file));
			output.write( fileContent );
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to write to " + file, e); 
		} finally {
			try {
				output.close();
			} catch ( IOException e ) {
				throw new IllegalArgumentException("Failed to close file " + file, e);
			}
		}
		return file;
	}

	protected void compile(File sourceFile) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		compiler.compile(sourceFile, dstDir, out);
		out.close();
	}

	private void move(File source, File destination) {
		if (!(source.exists())) {
			throw new IllegalArgumentException("File doesn't exist: "+ source);
		}
		if (!(destination.isDirectory())) {
			throw new IllegalArgumentException("Destination is not a directory: "+ destination);
		}
		for (File file : source.listFiles()) {
			File dest = new File(destination, file.getName());
			if (!(file.renameTo(dest))) {
				throw new IllegalStateException("Failed to move: "+ file+ " to: "+ dest);
			}
		}
	}

}
