package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;

public class SakaiPersonMeta implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private long id;
	private long userUuid;
	private String property;
	private String value;
	
	/* 
	 * Empty constructor
	 */
	public SakaiPersonMeta() {
	}

	
	/* 
	 * Constructor to create a Meta object in one go
	 */
	public SakaiPersonMeta(long userUuid, String property, String value) {
		super();
		this.userUuid = userUuid;
		this.property = property;
		this.value = value;
	}



	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(long userUuid) {
		this.userUuid = userUuid;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
	
	
}
