package edu.indiana.lib.osid.base.repository.http;

public class Logger
{
	private static Logger													_logger = new Logger();
	private static org.osid.logging.WritableLog		_log 		= null;

	/**
	 * Private constructor
	 */
	private Logger() { }

	/**
	 * Fetch a logger instance
	 */
	public static Logger getInstance()
	{
		return _logger;
	}

	/**
	 * Return the active log
	 */
	public static org.osid.logging.WritableLog getLog()
	{
		return _log;
	}

	/**
	 * Initialize a log for use
	 * @param log WritableLog instance for future use
	 */
	public void initialize(org.osid.logging.WritableLog log)
	{
		_log = log;
	}

	/**
	 * Log a message
	 */
	public void log(String entry)
	throws org.osid.repository.RepositoryException
	{
		if (_log != null)
		{
			try
			{
				_log.appendLog(entry);
			}
			catch (org.osid.logging.LoggingException ignore) { }
		}
	}
}
