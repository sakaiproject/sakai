<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<f:subview id="timed">
    <%-- TIMED --%>
    <div class="form-group row">
        <h:outputLabel styleClass="col-lg-2" value="#{assessmentSettingsMessages.assessment_timed}" />
        <div class="col-lg-9">
            <t:selectOneRadio id="selTimed" value="#{itemauthor.currentItem.timedQuestion}" layout="spread" onclick="toggleSection('itemForm:timed:timeSection', this.value)" styleClass="changeWatch">
                <f:selectItem itemValue="false" itemLabel="#{assessmentSettingsMessages.assessment_not_timed}"/>
                <f:selectItem itemValue="true" itemLabel="#{assessmentSettingsMessages.assessment_is_timed}"/>
            </t:selectOneRadio>
            <ul class="ulTimed">
                <li>
                    <t:radio renderLogicalId="true" for="selTimed" index="0" />
                </li>
                <li>
                    <t:radio renderLogicalId="true" for="selTimed" index="1" />

                    <h:panelGroup id="timeSection" styleClass="#{!itemauthor.currentItem.timedQuestion ? 'hidden' : ''}">
                        <span>.&#160;</span>
                        <h:outputLabel id="isTimedTimeLimitLabel" value="#{assessmentSettingsMessages.assessment_is_timed_limit} " />
                        
                        <input type="number" min="0" max="11" onchange="this.reportValidity()? document.getElementById('itemForm:timed:hiddenTimedHours').value=this.value : false" value="<h:outputText value="#{itemauthor.currentItem.timedHours}" />" title="<h:outputText value="#{assessmentSettingsMessages.timed_hours}" />" class="changeWatch"/>
                        <h:inputHidden id="hiddenTimedHours" value="#{itemauthor.currentItem.timedHours}" />
                        <h:outputLabel id="timedHoursLabel"  value="#{assessmentSettingsMessages.timed_hours} " />
                        
                        <input type="number" min="0" max="59" onchange="this.reportValidity()? document.getElementById('itemForm:timed:hiddenTimedMinutes').value=this.value : false" value="<h:outputText value="#{itemauthor.currentItem.timedMinutes}" />" title="<h:outputText value="#{assessmentSettingsMessages.timed_minutes}" />" class="changeWatch"/>
                        <h:inputHidden id="hiddenTimedMinutes" value="#{itemauthor.currentItem.timedMinutes}" />
                        <h:outputLabel id="timedMinutesLabel" value="#{assessmentSettingsMessages.timed_minutes} " />
                    </h:panelGroup>
                </li>
            </ul>
            <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.question_timed_info}" />
        </div>
    </div>
</f:subview>
