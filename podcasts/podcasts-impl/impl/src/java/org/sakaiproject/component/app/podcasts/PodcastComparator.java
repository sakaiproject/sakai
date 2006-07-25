package org.sakaiproject.component.app.podcasts;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.time.api.Time;

public class PodcastComparator implements Comparator {
	
	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);

	private String m_property = null;
	private boolean m_ascending = true;

	/**
	 * Construct.
	 * 
	 * @param property
	 *        The property name used for the sort.
	 * @param asc
	 *        true if the sort is to be ascending (false for descending).
	 */
	public PodcastComparator(String property, boolean ascending)
	{
		m_property = property;
		m_ascending = ascending;

	} // PodcastComparator	
	
	public int compare(Object o1, Object o2) {
		int rv = 0;

		try {
			Time t1 = ((ContentResource) o1).getProperties().getTimeProperty(m_property);
			Time t2 = ((ContentResource) o2).getProperties().getTimeProperty(m_property);
		
			rv = t1.compareTo(t2);
		
			if (!m_ascending) rv = -rv;
		}
		catch (EntityPropertyTypeException ignore)
		{
			LOG.warn("EntityPropertyTypeException while comparing podcast dates. " + ignore.getMessage());
			ignore.printStackTrace();

		} catch (EntityPropertyNotDefinedException ignore) {
			LOG.warn("EntityPropertyNotDefinedException while comparing podcast dates. " + ignore.getMessage());
			ignore.printStackTrace();
			
		}
		
		return rv;
	}
}
