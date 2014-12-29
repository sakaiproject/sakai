package org.sakaiproject.api.app.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 3, 2010
 * Time: 10:01:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurableJobPropertyValidationException extends Exception
{
    public ConfigurableJobPropertyValidationException()
    {
        super();
    }

    public ConfigurableJobPropertyValidationException(Throwable t)
    {
        super(t);
    }

    public ConfigurableJobPropertyValidationException(String message)
    {
        super(message);
    }

    public ConfigurableJobPropertyValidationException(String message, Throwable t)
    {
        super(message, t);
    }
}
