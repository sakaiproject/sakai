package org.sakaiproject.tool.assessment.facade;

import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;

public interface SectionFacadeQueriesAPI
{

  public Long addSection(Long assessmentId);

  public void remove(Long sectionId);

  public SectionFacade get(Long sectionId);

  public SectionData load(Long sectionId);

  public void addSectionMetaData(Long sectionId, String label, String value);

  public void deleteSectionMetaData(Long sectionId, String label);

}