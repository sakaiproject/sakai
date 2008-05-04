package org.sakaiproject.content.multiplex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;

/*
 * In Spring 2.0 we would use SimpleBeanTargetSource, but it doesn't exist
 * in Spring 1.2.x which Sakai 2.4.x still uses.
 */
public class ContentHostingTargetSource extends AbstractBeanFactoryBasedTargetSource
{
	private static final Log log = LogFactory.getLog(ContentHostingTargetSource.class);

	public void init()
	{
		log.info("init()");
	}

	public void destroy()
	{
		log.info("destroy()");
	}
	/* (non-Javadoc)
	 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource#getTargetClass()
	 */

	public Object getTarget() throws Exception
	{
		return getBeanFactory().getBean(getTargetBeanName());
	}

}
