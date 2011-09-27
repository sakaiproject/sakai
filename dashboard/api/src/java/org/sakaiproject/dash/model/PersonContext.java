/**
 * 
 */

package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public class PersonContext {
	
	protected long id;
	protected Person person;
	protected Context context;
	
	/**
	 * 
	 */
	public PersonContext() {
		super();
	}

	/**
	 * @param id
	 * @param person
	 * @param context
	 */
	public PersonContext(Person person, Context context) {
		super();
		this.person = person;
		this.context = context;
	}
	/**
	 * @param id
	 * @param person
	 * @param context
	 */
	public PersonContext(long id, Person person, Context context) {
		super();
		this.id = id;
		this.person = person;
		this.context = context;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonContext [id=");
		builder.append(id);
		builder.append(", person=");
		builder.append(person);
		builder.append(", context=");
		builder.append(context);
		builder.append("]");
		return builder.toString();
	}
	
}
