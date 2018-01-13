/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TerracottaClassLoader extends URLClassLoader {
	private String classLoaderName;

	public TerracottaClassLoader(URL[] urls, ClassLoader parent, String classLoaderName) {
		super(urls, parent);
		
		this.classLoaderName = classLoaderName;
		
		boolean registeredWithTerracotta = false;
		
		try {
			if (parent == null) {
				log.error("Parent classloader is set to null.");
			} else {
				Class<?> namedClassLoader = parent.loadClass("com.tc.object.loaders.NamedClassLoader");
				if (namedClassLoader == null) {
					log.error("Could not load Terracotta NamedClassLoader");
				} else {
			        Class<?> helper = parent.loadClass("com.tc.object.bytecode.hook.impl.ClassProcessorHelper");
					if (helper == null) {
						log.error("Could not load Terracotta ClassProcessorHelper");
					} else {
				        Method m = helper.getMethod("registerGlobalLoader", new Class<?>[] { namedClassLoader });
						if (m == null) { 
							log.error("Could not find Terracotta Method - \"registerGlobalLoader\"");
						} else {
					        m.invoke(null, new Object[] { this });
							registeredWithTerracotta = true;
					        if (log.isInfoEnabled()) {
					        	log.info("Registered the [" + classLoaderName + 
					        			 "] class loader with Terracotta.");
					        }
						}
					}
				}
			}
		} catch (Exception e) {
			// It is important that normal startup not suffer if Terracotta is not running
			// so catch any error that occurs in this block of code dealing with Terracotta
			// class loader registering
			log.error("Unexpected error occurred trying to register class loader [" + 
					classLoaderName + "] with Terracotta: "+e, e);
		}
		if (!registeredWithTerracotta) {
        	log.warn("The [" + classLoaderName + 
       			 "] class loader is not registered with Terracotta.  Objects from this " +
       			 "class loader will not be shared.");
		}
	}
	
	// needed for Terracotta Clustering to work.  Harmless for non-Terracotta environments
    public String __tc_getClassLoaderName() {
        return classLoaderName;
    }
}
