package edu.indiana.lib.osid.base.repository.http;

public class MetasearchException extends org.osid.repository.RepositoryException
{
		private static org.apache.commons.logging.Log	_log = edu.indiana.lib.twinpeaks.util.LogUtils.getLog(MetasearchException.class);
		
	public static final String SESSION_TIMED_OUT 	= "Metasearch session has timed out.";
	public static final String METASEARCH_ERROR 	= "Metasearch error has occured.";
	public static final String ASSET_NOT_FETCHED 	= "An Asset is available, but has not yet been fetched.";

	protected MetasearchException(String message)
	{
		super(message);
	}
}
