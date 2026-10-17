<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <f:subview id="timed">
    <!-- TIMED  -->
    <div class="form-group row">
      <h:outputLabel styleClass="col-lg-2" value="#{assessmentSettingsMessages.assessment_timed}"/>
      <div class="col-lg-9">
        <t:selectOneRadio id="selTimed" value="#{itemauthor.currentItem.timedQuestion}" layout="spread" onclick="toggleSection('itemForm:timed:timeSection', this.value)">
          <f:selectItem itemValue="false" itemLabel="#{assessmentSettingsMessages.assessment_not_timed}"/>
          <f:selectItem itemValue="true" itemLabel="#{assessmentSettingsMessages.assessment_is_timed}"/>
        </t:selectOneRadio>
        <ul class="ulTimed">
          <li>
            <t:radio renderLogicalId="true" for="selTimed" index="0"/>
          </li>
          <li>
            <t:radio renderLogicalId="true" for="selTimed" index="1"/>
            <h:panelGroup id="timeSection" styleClass="#{!itemauthor.currentItem.timedQuestion ? 'hidden' : ''}">
              <span>
                <jsp:text><![CDATA[.&#160;]]></jsp:text>
              </span>
              <h:outputLabel id="isTimedTimeLimitLabel" value="#{assessmentSettingsMessages.assessment_is_timed_limit} "/>
              <jsp:text><![CDATA[
                        
                        <input id="itemForm:timed:timedHours" type="number" min="0" max="11" onchange="this.reportValidity()? document.getElementById('itemForm:timed:hiddenTimedHours').value=this.value : false" value="]]></jsp:text>
              <h:outputText value="#{itemauthor.currentItem.timedHours}"/>
              <jsp:text>" title="</jsp:text>
              <h:outputText value="#{assessmentSettingsMessages.timed_hours}"/>
              <jsp:text><![CDATA[" />
                        ]]></jsp:text>
              <h:inputHidden id="hiddenTimedHours" value="#{itemauthor.currentItem.timedHours}"/>
              <h:outputLabel id="timedHoursLabel" for="timedHours" value="#{assessmentSettingsMessages.timed_hours} "/>
              <jsp:text><![CDATA[
                        
                        <input id="itemForm:timed:timedMinutes" type="number" min="0" max="59" onchange="this.reportValidity()? document.getElementById('itemForm:timed:hiddenTimedMinutes').value=this.value : false" value="]]></jsp:text>
              <h:outputText value="#{itemauthor.currentItem.timedMinutes}"/>
              <jsp:text>" title="</jsp:text>
              <h:outputText value="#{assessmentSettingsMessages.timed_minutes}"/>
              <jsp:text><![CDATA[" />
                        ]]></jsp:text>
              <h:inputHidden id="hiddenTimedMinutes" value="#{itemauthor.currentItem.timedMinutes}"/>
              <h:outputLabel id="timedMinutesLabel" for="timedMinutes" value="#{assessmentSettingsMessages.timed_minutes} "/>
            </h:panelGroup>
          </li>
        </ul>
        <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.question_timed_info}"/>
      </div>
    </div>
  </f:subview>
</jsp:root>
