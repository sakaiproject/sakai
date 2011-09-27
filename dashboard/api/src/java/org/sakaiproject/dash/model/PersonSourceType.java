/**
 * 
 */
package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public class PersonSourceType {
	
	protected long id;
	protected Person person;
	protected SourceType sourceType;
	
	/**
	 * 
	 */
	public PersonSourceType() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param person
	 * @param sourceType
	 */
	public PersonSourceType(Person person, SourceType sourceType) {
		super();
		this.person = person;
		this.sourceType = sourceType;
	}

	/**
	 * @param id
	 * @param person
	 * @param sourceType
	 */
	public PersonSourceType(long id, Person person, SourceType sourceType) {
		super();
		this.id = id;
		this.person = person;
		this.sourceType = sourceType;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @return the sourceType
	 */
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonSourceType [id=");
		builder.append(id);
		builder.append(", person=");
		builder.append(person);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

}
