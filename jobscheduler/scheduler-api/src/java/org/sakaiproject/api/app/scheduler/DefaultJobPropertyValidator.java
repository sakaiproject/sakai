package org.sakaiproject.api.app.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 3, 2010
 * Time: 10:03:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultJobPropertyValidator
    implements ConfigurableJobPropertyValidator
{
    public final void assertValid(String propertyLabel, String value)
        throws ConfigurableJobPropertyValidationException
    {
        //no-op - assume all values are valid
    }
}
