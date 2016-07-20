package org.sakaiproject.contentreview.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

public class ContentReviewActivityConfigDao extends HibernateCommonDao<ContentReviewActivityConfigEntry>
{
	// Hibernate property names
	private static final String ACTIVITY_ID = "activityId";
	private static final String TOOL_ID = "toolId";
	private static final String PROVIDER_ID = "providerId";
	private static final String NAME = "name";
	private static final String VALUE = "value"; // here for documentation and future use
	
	/**
	 * Finds an activity config entry by key (unique combination of name, toolId, activityId, and providerId)
	 * @param name the name of the configuration property
	 * @param activityId the unique identifier for the activity (ex: an assignment id)
	 * @param toolId the unique identifier for the Sakai tool the activity belongs to (ex: sakai.assignment.grades)
	 * @param providerId the unique identifier for the content review provider this configuration is for
	 * @return the entry for this key, if found
	 */
	public Optional<ContentReviewActivityConfigEntry> findByEntryKey(String name, String activityId, String toolId, int providerId)
	{
		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewActivityConfigEntry.class)
				.add(Restrictions.eq(NAME, name))
				.add(Restrictions.eq(ACTIVITY_ID, activityId))
				.add(Restrictions.eq(TOOL_ID, toolId))
				.add(Restrictions.eq(PROVIDER_ID, providerId));
		
		return Optional.ofNullable((ContentReviewActivityConfigEntry) c.uniqueResult());
	}
	
	/**
	 * Finds all configuration properties for the given activity
	 * @param activityId the unique identifier for the activity (ex: an assignment id)
	 * @param toolId the unique identifier for the Sakai tool the activity belongs to (ex: sakai.assignment.grades)
	 * @param providerId the unique identifier for the content review provider this configuration is for
	 * @return a map of name/value pairs for all configuration properties, or an empty map if none found
	 */
	public Map<String, String> findConfigurationForActivity(String activityId, String toolId, int providerId)
	{
		Criteria c = sessionFactory.getCurrentSession()
				.createCriteria(ContentReviewActivityConfigEntry.class)
				.add(Restrictions.eq(ACTIVITY_ID, activityId))
				.add(Restrictions.eq(TOOL_ID, toolId))
				.add(Restrictions.eq(PROVIDER_ID, providerId));
		
		List<ContentReviewActivityConfigEntry> results = c.list();
		return results.stream().collect(Collectors.toMap(entry -> entry.getName(), entry -> entry.getValue()));
	}
	
	/**
	 * Creates or updates an activity config entry for the given key (unique combination of name, toolId, activityId, and providerId)
	 * @param name the name of the configuration property
	 * @param value the value of the configuration property
	 * @param activityId the unique identifier for the activity (ex: an assignment id)
	 * @param toolId the unique identifier for the Sakai tool the activity belongs to (ex: sakai.assignment.grades)
	 * @param providerId the unique identifier for the content review provider this configuration is for
	 * @param overrideIfSet if an entry for the given key already exists, override the existing value
	 * @return true if a new entry was created or an existing entry was modified
	 */
	public boolean createOrUpdateEntry(String name, String value, String activityId, String toolId, int providerId, boolean overrideIfSet)
	{
		Optional<ContentReviewActivityConfigEntry> opt = findByEntryKey(name, activityId, toolId, providerId);
		if (!opt.isPresent())
		{
			try
			{
				create(new ContentReviewActivityConfigEntry(name, value, activityId, toolId, providerId));
				return true;
			}
			catch (ConstraintViolationException e)
			{
				// there is a uniqueness constraint on entry keys in the database
				// a row with the same key was written after we checked, retrieve new data and continue
				opt = findByEntryKey(name, activityId, toolId, providerId);
			}
		}
		
		if (overrideIfSet)
		{
			ContentReviewActivityConfigEntry entry = opt.orElseThrow( () ->	new RuntimeException("Unique constraint violated during insert attempt, yet unable to retrieve row."));
			entry.setValue(value);
			save(entry);
			return true;
		}
		
		return false;
	}
}
