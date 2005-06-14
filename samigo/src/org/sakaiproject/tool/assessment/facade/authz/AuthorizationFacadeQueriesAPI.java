package org.sakaiproject.tool.assessment.facade.authz;

public interface AuthorizationFacadeQueriesAPI
{

  public QualifierIteratorFacade getQualifierParents(String qualifierId);

  public QualifierIteratorFacade getQualifierChildren(String qualifierId);

  public void showQualifiers(QualifierIteratorFacade iter);

  public void addAuthz(AuthorizationFacade a);

  public void addQualifier(QualifierFacade q);

}