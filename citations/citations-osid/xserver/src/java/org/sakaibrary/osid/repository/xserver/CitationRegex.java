package org.sakaibrary.osid.repository.xserver;

public class CitationRegex implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String regex;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
}
