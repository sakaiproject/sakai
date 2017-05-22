package edu.indiana.lib.osid.base.repository.http;

public class MetasearchException extends org.osid.repository.RepositoryException
{
	public static final String SESSION_TIMED_OUT 	= "Metasearch session has " +
			"timed out. Please restart your search session.";
	public static final String METASEARCH_ERROR   = "Metasearch error has occured. Please contact your site's support team.";
	public static final String ASSET_NOT_FETCHED 	= "An Asset is available, but has not yet been fetched.";

	protected MetasearchException(String message)
	{
		super(message);
	}
}
