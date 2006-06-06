/**
 * 
 */
package org.sakaiproject.search.api.rdf;

/**
 * @author ieb
 *
 */
public class RDFException extends Exception
{

	/**
	 * 
	 */
	public RDFException()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public RDFException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RDFException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public RDFException(Throwable arg0)
	{
		super(arg0);
	}

}
