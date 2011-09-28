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
	protected ItemType itemType;
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
	public PersonSourceType(ItemType itemType, Person person, SourceType sourceType) {
		super();
		this.itemType = itemType;
		this.person = person;
		this.sourceType = sourceType;
	}

	/**
	 * @param id
	 * @param person
	 * @param sourceType
	 */
	public PersonSourceType(long id, ItemType itemType, Person person, SourceType sourceType) {
		super();
		this.id = id;
		this.itemType = itemType;
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
	 * @return the itemType
	 */
	public ItemType getItemType() {
		return itemType;
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
	 * @param itemType the itemType to set
	 */
	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
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
		builder.append(", itemType=");
		builder.append(itemType.getName());
		builder.append(", person=");
		builder.append(person);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

}
