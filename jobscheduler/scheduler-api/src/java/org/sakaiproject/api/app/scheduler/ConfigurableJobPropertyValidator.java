package org.sakaiproject.api.app.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 3, 2010
 * Time: 10:00:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigurableJobPropertyValidator
{
    public void assertValid (String propertyLabel, String value)
        throws ConfigurableJobPropertyValidationException;
}
