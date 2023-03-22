package org.sakaiproject.microsoft.api.data;

public enum SakaiUserIdentifier {
	USER_PROPERTY("property"),
	USER_EID("eid"),
	EMAIL("email");
	
	private static final String PREFIX_MAPPING = "MAP:";
	public static final String KEY = PREFIX_MAPPING + "SAKAI_USER_ID_MAP";
	
	public static final String USER_PROPERTY_KEY = "microsoft_mapped_id";
	
	private String code;
	
	private SakaiUserIdentifier(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static SakaiUserIdentifier fromString(String text) {
		for (SakaiUserIdentifier v : SakaiUserIdentifier.values()) {
			if (v.code.equalsIgnoreCase(text)) {
				return v;
			}
		}
		return EMAIL;
	}
}
