package org.sakaiproject.microsoft.api.data;

import lombok.Getter;

@Getter
public enum MicrosoftLogInvokers {

	UNKNOWN("unknown"),
	HOOK("hook"),
	JOB("job"),
	MANUAL("manual");

	private String code;

	private MicrosoftLogInvokers(String code) {
		this.code = code;
	}

}
