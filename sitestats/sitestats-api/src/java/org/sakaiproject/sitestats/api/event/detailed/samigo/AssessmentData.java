package org.sakaiproject.sitestats.api.event.detailed.samigo;

/**
 * Data for an assessment
 * @author plukasew
 */
public class AssessmentData implements SamigoData
{
	public final String title;
	public final boolean published;

	/**
	 * Constructor
	 * @param title the assessment title
	 * @param published whether the assessment is published
	 */
	public AssessmentData(String title, boolean published)
	{
		this.title = title;
		this.published = published;
	}
}
