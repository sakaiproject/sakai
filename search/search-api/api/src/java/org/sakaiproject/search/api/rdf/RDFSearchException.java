/**
 * 
 */
package org.sakaiproject.search.api.rdf;


/**
 * @author ieb
 *
 */
public class RDFSearchException extends RDFException
{

	/**
	 * 
	 */
	public RDFSearchException()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public RDFSearchException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RDFSearchException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public RDFSearchException(Throwable arg0)
	{
		super(arg0);
	}

}
