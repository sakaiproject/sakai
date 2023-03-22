package org.sakaiproject.microsoft.api.data;

public enum SynchronizationStatus {
	OK(2),
	PARTIAL_OK(1), //site-team is OK, but some group-channel are KO
	KO(0),
	NONE(-1),
	ERROR(-2),
	ERROR_GUEST(-3);
	
	private Integer code;
	
	private SynchronizationStatus(Integer code) {
		this.code = code;
	}
	
	public Integer getCode() {
		return this.code;
	}
	
	public static SynchronizationStatus fromCode(Integer code) {
		for (SynchronizationStatus v : SynchronizationStatus.values()) {
			if (v.code == code) {
				return v;
			}
		}
		return null;
	}
}
