package org.sakaiproject.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class TerracottaClassLoader extends URLClassLoader {
	/** Our logger */
	private static Log log = LogFactory.getLog(ComponentsLoader.class);
	
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
