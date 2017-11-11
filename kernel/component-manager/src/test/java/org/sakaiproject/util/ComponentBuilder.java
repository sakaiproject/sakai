/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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

/**
 * A utility for generating (and cleaning up) {@link Component}s. Please
 * note that the tear-down is highly aggressive: it deletes the entire
 * root components directory that it generates.
 * 
 * 
 * @author dmccallum
 *
 */
public class ComponentBuilder {

	private File componentsRootDir;
	private Compiler compiler;
	
	public ComponentBuilder() {
		compiler = new Compiler();
	}
	
	
	/**
	 * Verifies that this builder has a reasonable chance at performing
	 * its work, i.e. can at least attempt to compile generated source code.
	 * 
	 * @return
	 */
	public boolean isUseable() {
		return compiler.isUseable();
	}

	public Component buildComponent() {
		return buildComponent(nextComponentId());
	}

	public Component buildComponent(String id, String... jars) {
		if ( !(isUseable()) ) {
			throw new IllegalStateException("ComponentBuilder not currently useable, probably because the JDK compiler is unavailable.");
		}
		initComponentsRootDir();
		Component component = new Component(id,
				componentsRootDir.getAbsolutePath(), compiler, jars);
		component.generate();
		return component;
	}


	protected String nextComponentId() {
		return Long.toString((long)(Math.random() * 2821109907456L), 36);
	}

	protected void initComponentsRootDir() {
		if ( componentsRootDir != null && componentsRootDir.exists() ) {
			return;
		}
		doInitComponentsRootDir();
	}

	protected void doInitComponentsRootDir() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		if ( !(tmpDir.exists()) || !(tmpDir.isDirectory()) || !(tmpDir.canWrite()) ) {
			throw new IllegalStateException("Unable to create components root dir in [" + tmpDir + "]");
		}
		File newRoot = null;
		for ( int i = 0; i < 1000; i++ ) {
			newRoot = new File(tmpDir.getAbsolutePath() + File.separator + "components" + (i == 0 ? "" : "-" + i));
			if ( newRoot.mkdir() ) {
				componentsRootDir = newRoot;
				break;
			}
		}
		if ( componentsRootDir == null ) {
			throw new IllegalStateException("Unable to create new components root dir below " + tmpDir);
		}
	}

	public File getComponentsRootDir() {
		return componentsRootDir;
	}
	
	public void tearDown() {
		if ( componentsRootDir == null || !(componentsRootDir.exists()) ) {
			return;
		}
		deleteDir(componentsRootDir);
	}

	protected void deleteDir(File dir) {
		if (!(dir.exists())) {
			return;
		}
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			for ( File file : files ) {
				deleteDir(file);
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}

}
