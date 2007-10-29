package org.dojo.moxie;

/**
	@author Brad Neuberg, bkn3@columbia.edu
*/
public class MoxieException extends Exception{
	public MoxieException(){
		super();
	}
	
	public MoxieException(String s){
		super(s);
	}
	
	public MoxieException(String s, Throwable cause){
		super(s, cause);
	}
	
	public MoxieException(Throwable cause){
		super(cause);
	}
}