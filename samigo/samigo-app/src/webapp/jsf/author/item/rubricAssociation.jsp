<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <div class="form-group row">
    <div class="col-sm-12">
      <jsp:text><![CDATA[
        <sakai-rubric-association
            site-id=']]></jsp:text>
      <h:outputText value="#{itemauthor.currentItem.siteId}"/>
      <jsp:text>'
            dont-associate-label='</jsp:text>
      <h:outputText value="#{assessmentSettingsMessages.dont_associate_label} "/>
      <jsp:text>'
            associate-label='</jsp:text>
      <h:outputText value="#{assessmentSettingsMessages.associate_label} "/>
      <jsp:text>'
            associate-label-dyn='</jsp:text>
      <h:outputText value="#{assessmentSettingsMessages.associate_label_dyn} "/>
      <jsp:text>'
            show-dynamic=true
            tool-id="sakai.samigo"
            </jsp:text>
      <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'AssessmentFacade' and itemauthor.itemId != ''}">
        <jsp:text>
                entity-id='</jsp:text>
        <h:outputText value="#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>
        <jsp:text>'
            </jsp:text>
      </h:panelGroup>
      <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'PublishedAssessmentFacade' and itemauthor.itemId != ''}">
        <jsp:text>
                entity-id='</jsp:text>
        <h:outputText value="pub.#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>
        <jsp:text>'
            </jsp:text>
      </h:panelGroup>
      <h:panelGroup rendered="#{itemauthor.rubricStateDetails != ''}">
        <jsp:text>
                state-details='</jsp:text>
        <h:outputText value="#{itemauthor.rubricStateDetails}"/>
        <jsp:text>'
            </jsp:text>
      </h:panelGroup>
      <jsp:text>
            fine-tune-points='</jsp:text>
      <h:outputText value="#{assessmentSettingsMessages.option_pointsoverride}"/>
      <jsp:text>'
            hide-student-preview='</jsp:text>
      <h:outputText value="#{assessmentSettingsMessages.option_studentpreview}"/>
      <jsp:text><![CDATA['>
        </sakai-rubric-association>
    ]]></jsp:text>
    </div>
  </div>
</jsp:root>
