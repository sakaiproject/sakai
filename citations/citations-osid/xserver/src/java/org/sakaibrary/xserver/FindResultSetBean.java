package org.sakaibrary.xserver;

public class FindResultSetBean {
	private static final org.apache.commons.logging.Log LOG =
		org.apache.commons.logging.LogFactory.getLog(
				"org.sakaibrary.xserver.FindResultSetBean" );
	
	private String baseName;
	private String sourceId;
	private String setNumber;
	private StringBuffer fullName;
	private String status;
	private StringBuffer findErrorText;
	private String numDocs;
	
	public FindResultSetBean( String baseName ) {
		this.baseName = baseName;
	}
	
	public String getBaseName() {
		return baseName;
	}
	
	public void setSourceId( String id ) {
		sourceId = id;
	}
	
	public String getSourceId() {
		return sourceId;
	}
	
	public void setSetNumber( String setNum ) {
		setNumber = setNum;
	}
	
	public String getSetNumber() {
		return setNumber;
	}
	
	public void setFullName( String name ) {
		if( fullName == null ) {
			fullName = new StringBuffer();
		}
		
		fullName.append( name );
	}
	
	public String getFullName() {
		return ( fullName == null ) ? null : fullName.toString();
	}
	
	public void setStatus( String stat ) {
		status = stat;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setFindErrorText( String text ) {
		if( findErrorText == null ) {
			findErrorText = new StringBuffer();
		}
		
		findErrorText.append( text );
	}
	
	public String getFindErrorText() {
		return ( findErrorText == null ) ? null : findErrorText.toString();
	}
	
	public void setNumDocs( String num ) {
		numDocs = num;
	}
	
	public String getNumDocs() {
		return numDocs;
	}
	
	public void printInfo() {
		LOG.debug( "\nFIND RESULT SET INFO" );
		LOG.debug( "source id:  " + getSourceId() );
		LOG.debug( "full name:  " + getFullName() );
		LOG.debug( "set number: " + getSetNumber() );
		LOG.debug( "status:     " + getStatus() );
		LOG.debug( "error text: " + getFindErrorText() );
		LOG.debug( "docs found: " + getNumDocs() );
	}
}
