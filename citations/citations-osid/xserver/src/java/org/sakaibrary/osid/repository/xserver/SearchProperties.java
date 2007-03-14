package org.sakaibrary.osid.repository.xserver;

import org.osid.shared.SharedException;

public class SearchProperties implements org.osid.shared.Properties {
	private Type type = new Type( "sakaibrary", "properties", "asynchMetasearch" );
	private java.util.Properties properties;
	private java.util.Vector keys;

	public SearchProperties( java.util.Properties properties ) {
		this.keys = new java.util.Vector();
		this.properties = properties;
		
		java.util.Enumeration keyNames = properties.keys();
		while( keyNames.hasMoreElements() ) {
			this.keys.add( (java.io.Serializable)keyNames.nextElement() );
		}
	}
	
	public org.osid.shared.ObjectIterator getKeys()
	throws SharedException {
		return new ObjectIterator( keys );
	}

	public java.io.Serializable getProperty( java.io.Serializable key )
	throws SharedException {
		return (java.io.Serializable)properties.get( key );
	}

	public org.osid.shared.Type getType() 
	throws SharedException {
		return type;
	}
}
