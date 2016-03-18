package org.sakaiproject.scorm.entity;

/**
 *
 * @author bjones86
 */
public class MockScormEntityProviderImpl implements ScormEntityProvider
{
    @Override
    public String getEntityPrefix()
    {
        return ScormEntityProvider.ENTITY_PREFIX;
    }
}
