package org.sakaiproject.scorm.entity.api;

import org.sakaiproject.entitybroker.IdEntityReference;

public class ScormEntityReference extends IdEntityReference {
	public String key;
	
	public ScormEntityReference(String reference) {
		super(reference);
		key = getKey(reference);
	}

	public ScormEntityReference(String prefix, String id) {
		super(prefix, id);
	}
	
	public ScormEntityReference(String prefix, String id, String key) {
		super(prefix, id);
		this.key = key;
	}
	
	public static String getKey(String reference) {
		int spos = getKeySeparatorPos(reference);
		
		return spos == -1 ? "" : reference.substring(spos + 1);
		
		//int lspos = (reference.indexOf('/', spos + 1));
		//return lspos == -1? reference.substring(spos + 1):reference.substring(spos + 1, lspos);
	}
	
	public static int getKeySeparatorPos(String reference) {
		int firstSeparatorPos = getSeparatorPos(reference);
		
		if (reference == null || reference.length() < firstSeparatorPos + 1
				|| reference.charAt(0) != '/') {
			return -1;
		}
		
		return reference.indexOf('/', firstSeparatorPos + 1);
	}
	
	public String toString() {
		return new StringBuffer().append(prefix).append("/").append(id)
			.append("/").append(key).toString();
	}
	
}
