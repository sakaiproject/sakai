package org.sakaiproject.util;

/**
 * <p>
 * MyFaces has a issue/bug whereby if a managed bean implements Map then it calls {@link #put(Object, Object)} rather
 * than an explicit setter. This means that setting the baseName for the resource bundle doesn't work on
 * the plain ResourceLoader as ResourceLoader doesn't support {@link #put(Object, Object)}.
 * </p>
 * <p>
 * We also implement {@link #get(Object)} because with MyFaces is initialising managed properties it calls the setting
 * first and without this we get warning about using a null bundle.
 * </p>
 *
 * @author Matthew Buckett
 */
public class ResourceLoaderWrapper extends ResourceLoader {

	@Override
	public Object get(Object key) {
		if ("baseName".equals(key)) {
			return baseName;
		} else {
			return super.get(key);
		}
	}

	@Override
	public Object put(Object key, Object value) {
		if ("baseName".equals(key) && value instanceof String) {
			setBaseName((String)value);
			return null;
		} else {
			throw new IllegalArgumentException("We don't support put() with :"+ key);
		}
	}

}
