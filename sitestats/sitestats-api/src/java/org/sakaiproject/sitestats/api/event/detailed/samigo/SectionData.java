package org.sakaiproject.sitestats.api.event.detailed.samigo;

/**
 * Data for a section (part) of an assessment
 * @author plukasew
 */
public class SectionData implements SamigoData
{
	public final AssessmentData assessment;
	public final String title;

	/**
	 * Constructor
	 * @param assessment the assessment the section belongs to
	 * @param title the title of the section
	 */
	public SectionData(AssessmentData assessment, String title)
	{
		this.assessment = assessment;
		this.title = title;
	}
}
