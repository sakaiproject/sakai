/**
 * 
 */
package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public class PersonContextSourceType {
	
	protected long id;
	protected Person person;
	protected Context context;
	protected SourceType sourceType;
	
	/**
	 * 
	 */
	public PersonContextSourceType() {
		super();
	}

	/**
	 * @param id
	 * @param person
	 * @param context
	 * @param sourceType
	 */
	public PersonContextSourceType(Person person, Context context, SourceType sourceType) {
		super();
		this.person = person;
		this.context = context;
		this.sourceType = sourceType;
	}

	/**
	 * @param id
	 * @param person
	 * @param context
	 * @param sourceType
	 */
	public PersonContextSourceType(long id, Person person, Context context, SourceType sourceType) {
		super();
		this.id = id;
		this.person = person;
		this.context = context;
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
	 * @return the context
	 */
	public Context getContext() {
		return context;
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
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
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
		builder.append("PersonContextSourceType [id=");
		builder.append(id);
		builder.append(", person=");
		builder.append(person);
		builder.append(", context=");
		builder.append(context);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

	
}
