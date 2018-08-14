package org.sakaiproject.sitestats.api.event.detailed.samigo;

/**
 * Data for an assessment question
 * @author plukasew
 */
public class ItemData implements SamigoData
{
	public final SectionData section;
	public final int questionNumber;

	/**
	 * Constructor
	 * @param section the section (part) the question is in
	 * @param questionNumber the number of the question
	 */
	public ItemData(SectionData section, int questionNumber)
	{
		this.section = section;
		this.questionNumber = questionNumber;
	}
}
