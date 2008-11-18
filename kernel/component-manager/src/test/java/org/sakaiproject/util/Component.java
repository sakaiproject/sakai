package org.sakaiproject.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Represents a single Sakai component directory. Given an ID, a base dir,
 * and a {@link Compiler}, can generate its own directory layout and can
 * generate a near-guaranteed unique Java class and associated bean definition.
 * Cannot be instantiated if a component of the same ID has already been
 * created below the specified root directory. Cannot invoke {@link #generate()}
 * multiple times unless the underlying file system has been edited out-of-band.
 *  
 * @author dmccallum@unicon.net
 *
 */
public class Component {

	private static final String CLASS_SRC_TEMPLATE = 
		"package %1$s;\n" + 
		"public class %2$s { }";
	private static final String BEAN_DEF_TEMPLATE = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\n" +
		"<beans>\n" +
			"\t<bean id=\"%1$s\" class=\"%1$s\" />\n" +
		"</beans>";
	private String id;
	private File componentsRootDir;
	private File componentDir;
	private File webInfDir;
	private File classesDir;
	private File libDir;
	private File srcDir;
	private String pkgName;
	private String className;
	private File srcPkgDir;
	private File classSrcFile;
	private File beanDefsFile;
	private Compiler compiler;
	
	public Component(String id, String componentsRootDir, Compiler compiler) {
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
	}

	public String getBeanId() {
		return getBeanClass();
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
		compile();
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
		if (!(dir.mkdirs())) {
			throw new IllegalStateException("Unable to create dir at " + dir.getPath());
		}
		return dir;
	}
	
	protected void generateJavaSource() {
		srcDir = mkdir(componentDir, "src");
		pkgName = getClass().getPackage().getName();
		String pkgAsPath = 
			pkgName.replace(".", File.separator);
		srcPkgDir = mkdir(srcDir, pkgAsPath);
		className = "ServiceImpl" + id;
		String classSrc = String.format(CLASS_SRC_TEMPLATE, pkgName, className);
		classSrcFile = writeFile(srcPkgDir, className + ".java", classSrc);
	}
	
	protected void generateBeanDefs() {
		String beanDefs = String.format(BEAN_DEF_TEMPLATE, getBeanClass());
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

	protected void compile() {
		String[] options = { "-g", 
				"-source", 
				"1.5", 
				"-target", 
				"1.5",
				"-d",
				classesDir.getAbsolutePath(),
				"-sourcepath", 
				srcDir.getAbsolutePath(),
				classSrcFile.getAbsolutePath()};
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		compiler.compile(options,out);
		out.close();
	}

}
