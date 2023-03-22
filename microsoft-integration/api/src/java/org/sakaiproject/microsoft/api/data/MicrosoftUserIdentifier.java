package org.sakaiproject.microsoft.api.data;

public enum MicrosoftUserIdentifier {
	USER_ID("userId"),
	EMAIL("email");
	
	private static final String PREFIX_MAPPING = "MAP:";
	public static final String KEY = PREFIX_MAPPING + "MICROSOFT_USER_ID_MAP";
	
	private String code;
	
	private MicrosoftUserIdentifier(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static MicrosoftUserIdentifier fromString(String text) {
		for (MicrosoftUserIdentifier v : MicrosoftUserIdentifier.values()) {
			if (v.code.equalsIgnoreCase(text)) {
				return v;
			}
		}
		return EMAIL;
	}
}
