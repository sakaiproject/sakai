package org.sakaiproject.sitestats.impl.parser;

import org.sakaiproject.sitestats.api.parser.EventParserTip;

public class EventParserTipImpl implements EventParserTip {
	private String forWhat;
	private String separator;
	private String index;
	
	public EventParserTipImpl() {
		
	}
	
	public EventParserTipImpl(String forWhat, String separator, String index) {
		super();
		this.forWhat = forWhat;
		this.separator = separator;
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#getFor()
	 */
	public String getFor() {
		return forWhat;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#setFor(java.lang.String)
	 */
	public void setFor(String forWhat) {
		this.forWhat = forWhat;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#getSeparator()
	 */
	public String getSeparator() {
		return separator;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#setSeparator(java.lang.String)
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#getIndex()
	 */
	public String getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.EventParserTip#setIndex(java.lang.String)
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("For: "+getFor());
		buff.append(" | Separator: "+getSeparator());
		buff.append(" | Index: "+getIndex());
		
		return buff.toString();
	}
	
}
