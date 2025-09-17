<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
***********************************************************************************
* Copyright (c) ${license.git.copyrightYears} ${holder}
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*             http://opensource.org/licenses/ecl2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**********************************************************************************/
--%>

	<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>
	
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.sakai_assessment_manager} #{assessmentSettingsMessages.dash} #{assessmentSettingsMessages.settings}" /></title>
      <script src="/samigo-app/jsf/widget/hideDivision/hideDivision.js"></script>
      <script src="/library/js/lang-datepicker/lang-datepicker.js"></script>
      <script src="/samigo-app/js/authoring.js"></script>
      <script src="/samigo-app/js/authoringSecureDeliverySettings.js"></script>
      <script src="/library/js/spinner.js"></script>
      <script>includeWebjarLibrary('bootstrap-multiselect');</script>

      <f:verbatim rendered="#{publishedSettings.gradebookGroupEnabled}">
        <script>
          // Initialize input sync
          window.addEventListener("load", () => {
            window.syncGbSelectorInput("gb-selector", "assessmentSettingsAction:gb_selector");
            window.syncGbSelectorInput("category-selector", "assessmentSettingsAction:category_selector");
          });
        </script>
      </f:verbatim>

      <script>
        $(document).ready(function() {
          // set up the accordion for settings
          var accordionPanel = 2;
          var itemName = "samigo_publishedsettings_" + <h:outputText value="#{publishedSettings.assessmentId}"/>;
          if (window.sessionStorage && window.sessionStorage.getItem(itemName)) {
              accordionPanel = parseInt(window.sessionStorage.getItem(itemName));
          }
          $("#jqueryui-accordion").accordion({
              heightStyle: "content",
              activate: function(event, ui) {
                  if (window.sessionStorage) {
                      window.sessionStorage.setItem(itemName, $("#jqueryui-accordion").accordion("option", "active"));
                  }
              },
              active: accordionPanel,
              collapsible: true
          });
          // This is a sub-accordion inside of the About the Assessment Panel
          $("#jqueryui-accordion-metadata").accordion({ heightStyle: "content",collapsible: true,active: false });
          // This is a sub-accordion inside of the Availability and Submission Panel
          $("#jqueryui-accordion-security").accordion({ heightStyle: "content",collapsible: true,active: false });

	checkNav = function() {
              enabled = true;
              if ($("#assessmentSettingsAction\\:itemNavigation\\:1").is(":checked")) {
                  enabled = false;
              }

              if (enabled) {
                        $('#assessmentSettingsAction\\:itemNavigationHelpBlock').hide();
                        $('#assessmentSettingsAction\\:markForReview1').closest(".row").show();
                        $('#assessmentSettingsAction\\:assessmentFormat\\:0').closest(".row").show();
              } else {
                        $('#assessmentSettingsAction\\:itemNavigationHelpBlock').show();
                        $('#assessmentSettingsAction\\:markForReview1').prop("checked", false).closest(".row").hide();
                        $('#assessmentSettingsAction\\:assessmentFormat\\:0').closest(".row").hide();
              }
          };

          $('#assessmentSettingsAction\\:itemNavigation\\:0').change(checkNav);
          $('#assessmentSettingsAction\\:itemNavigation\\:1').change(checkNav);
          checkNav();

          // Copy to clipboard
          $("#copyToClipboard").click(function() {
              copyToClipboardNoScroll(this, "<h:outputText value='#{publishedSettings.publishedUrl}' />");
          });

          // SAM-2323 jquery-UI datepicker
          localDatePicker({
              input: '#assessmentSettingsAction\\:startDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: false,
              val: '<h:outputText value="#{publishedSettings.startDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'startDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:endDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.dueDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'endDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:retractDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.retractDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'retractDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:feedbackDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.feedbackDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'feedbackDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:feedbackEndDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.feedbackEndDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'feedbackEndDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-start_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeStart}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'newEntry-start_date-iso8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-due_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeDue}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'newEntry-due_date-iso8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-retract_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeRetract}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss" timeZone="#{author.userTimeZone}"/></h:outputText>',
              ashidden: { iso8601: 'newEntry-retract_date-iso8601' }
          });
          
          const releaseToVal = $('#assessmentSettingsAction\\:releaseTo').val();
          if (releaseToVal == 'Anonymous Users') {
              lockdownAnonyGrading(releaseToVal);
              lockdownGradebook(releaseToVal);
          }
          showHideReleaseGroups();
          checkTimedRadio();
          checkLastHandling();
          initTimedRadio();
          initAnononymousUsers();
          setAccessibilityAttributes();
          setExceptionDefault();
          setSubmissionLimit();
          disableAllFeedbackCheck();

          // Secure delivery
          initSecureDeliverySettings(true);

          <!--Initialize bootstrap multiselect-->
          $("#assessmentSettingsAction\\:groupsForSite").attr("multiple", "multiple");

          var divElem = document.createElement('div');
          var filterPlaceholder = <h:outputText value="'#{assessmentSettingsMessages.multiselect_filterPlaceholder}'" />;
          divElem.innerHTML = filterPlaceholder;
          filterPlaceholder = divElem.textContent;
          var selectAllText = <h:outputText value="'#{assessmentSettingsMessages.select_all_groups}'" />;
          divElem.innerHTML = selectAllText;
          selectAllText = divElem.textContent;
          var nonSelectedText = <h:outputText value="'#{assessmentSettingsMessages.multiselect_nonSelectedText}'" />;
          divElem.innerHTML = nonSelectedText;
          nonSelectedText = divElem.textContent;
          var allSelectedText = <h:outputText value="'#{assessmentSettingsMessages.multiselect_allSelectedText}'" />;
          divElem.innerHTML = allSelectedText;
          allSelectedText = divElem.textContent;
          var nSelectedText = <h:outputText value="'#{assessmentSettingsMessages.multiselect_nSelectedText}'" />;
          divElem.innerHTML = nSelectedText;
          nSelectedText = divElem.textContent;
          $("#assessmentSettingsAction\\:groupsForSite").multiselect({
              enableFiltering: true,
              enableCaseInsensitiveFiltering: true,
              includeSelectAllOption: true,
              filterPlaceholder: filterPlaceholder,
              selectAllText: selectAllText,
              nonSelectedText: nonSelectedText,
              allSelectedText: allSelectedText,
              nSelectedText: nSelectedText,
              templates: {
                button: '<button type="button" class="multiselect dropdown-toggle btn-primary" data-bs-toggle="dropdown"><span class="multiselect-selected-text"></span><i class="si si-caret-down-fill ps-2"></i></button>',
                filter: '<div class="multiselect-filter d-flex align-items-center"><i class="fa fa-sm fa-search text-muted"></i><input type="search" class="multiselect-search form-control" /></div>',
              },
          });

          const colorTextInput = document.getElementById("assessmentSettingsAction:pickColor");
          const colorInput = document.getElementById("color-input");

          // The color picker can be disabled.
          if (colorTextInput && colorInput) {

            // Initialize the picker if exists a previous value
            if (colorTextInput.value) {
              colorInput.value = colorTextInput.value;
            }

            function updateTextInputColor(event) {
              colorTextInput.value = colorInput.value;
            }
            colorInput.addEventListener("change", updateTextInputColor, false);

            function updateColorInputColor(event) {
              if (!CSS.supports('color', colorTextInput.value)) {
                return;
              }
              colorInput.value = colorTextInput.value;
            }
            colorTextInput.addEventListener("change", updateColorInputColor, false);

          }

          enableDisableToGradebook();

      });
      </script>

      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">

<!-- content... -->
<h:form id="assessmentSettingsAction" onsubmit="return editorCheck();">
  <f:verbatim><input type="hidden" id="ckeditor-autosave-context" name="ckeditor-autosave-context" value="samigo_publishedSettings" /></f:verbatim>
  <h:panelGroup rendered="#{publishedSettings.assessmentId!=null}"><f:verbatim><input type="hidden" id="ckeditor-autosave-entity-id" name="ckeditor-autosave-entity-id" value="</f:verbatim><h:outputText value="#{publishedSettings.assessmentId}"/><f:verbatim>"/></f:verbatim></h:panelGroup>
  <h:inputHidden id="assessmentId" value="#{publishedSettings.assessmentId}"/>
  <h:inputHidden id="blockDivs" value="#{publishedSettings.blockDivs}"/>
  <h:inputHidden id="itemNavigationUpdated" value="false" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>
  <h1>
     <h:outputText value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{publishedSettings.title}"/>
  </h1>

  <div class="pull-right">
      <a href="javascript:void(0)" id="expandLink" onclick="expandAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
          <h:outputText value="#{assessmentSettingsMessages.expandAll}"/>
      </a>
      <a href="javascript:void(0)" id="collapseLink" style="display:none" onclick="collapseAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
          <h:outputText value="#{assessmentSettingsMessages.collapseAll}"/>
      </a>
  </div>
  <br/>
  
  <h:dataTable value="#{assessmentSettings.errorMessages}" var="message" styleClass="sak-banner-error" rendered="#{assessmentSettings.renderErrorMessage}">
  	<h:column>
  	  <h:outputText value="#{message.detail}"/>
  	</h:column>
  </h:dataTable>
  <h:dataTable value="#{assessmentSettings.infoMessages}" var="message" styleClass="sak-banner-warn" rendered="#{assessmentSettings.renderInfoMessage}">
  	<h:column>
  	  <h:outputText value="#{message.detail}"/>
  	</h:column>
  </h:dataTable>

<div class="tier1" id="jqueryui-accordion">

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_about}" >

<!-- *** ASSESSMENT INTRODUCTION *** -->
  <div id="assessment-intro">

    <div class="form-group row">
        <h:outputLabel styleClass="col-md-10 form-label" for="assessment_title" value="#{assessmentSettingsMessages.assessment_title}"/>
        <div class="col-md-10">
            <h:inputText styleClass="form-control" id="assessment_title" size="80" maxlength="255" value="#{publishedSettings.title}" />
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel value="#{assessmentSettingsMessages.published_assessment_url}: " 
                        styleClass="form-label col-md-10 form-label"/>
        <div class=" col-md-10">
            <h:outputText value="#{publishedSettings.publishedUrl}" />
            <button type="button" id="copyToClipboard" title="<h:outputText value='#{assessmentSettingsMessages.copyToClipboard}' />">
                <span class="fa fa-clipboard" aria-hidden="true"></span>
                <span class="sr-only"><h:outputText value="#{assessmentSettingsMessages.copyToClipboard}: #{assessmentSettingsMessages.published_assessment_url}" /></span>
            </button>
        </div>
    </div>

    <!-- DESCRIPTION AND ATTACHMENTS TITILE  -->
    <div class="samigo-subheading">
        <h:outputLabel value="#{assessmentSettingsMessages.assessment_description_and_attachments}"/>
    </div>

    <div class="form-group row">
        <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.assessment_description}" rendered="#{publishedSettings.valueMap.description_isInstructorEditable==true}"/>

        <div class="col-md-10">
            <h:panelGrid>
                <samigo:wysiwyg rows="100" columns="400" value="#{publishedSettings.description}" hasToggle="yes" mode="author">
                <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

    <!-- ASSESSMENT ATTACHMENTS -->
    <div class="form-group row">
    	<%@ include file="/jsf/author/publishedSettings_attachment.jsp" %>
    </div>

    <!-- Honor Pledge -->
    <div class="form-group row">
        <h:outputLabel styleClass="col-md-10 form-label" for="honor_pledge" value="#{assessmentSettingsMessages.honor_pledge}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
        <div class="col-md-10">
            <h:selectBooleanCheckbox id="honor_pledge" value="#{publishedSettings.honorPledge}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
            <h:outputLabel for="honor_pledge" value="#{assessmentSettingsMessages.honor_pledge_add}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
        </div>
    </div>

  </div>


    <!-- *** META *** -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.metadataAssess_isInstructorEditable==true}">
      <div class="samigo-subheading">
        <h:outputLabel value="#{assessmentSettingsMessages.heading_metadata}" /> 
      </div>
        <!-- ASSESSMENT METADATA -->
        <h:outputLabel styleClass="samigo-sub-subheading" value="#{assessmentSettingsMessages.assessment_metadata}" />
        
        <div class="form-group row">
            <h:outputLabel for="keywords" value="#{assessmentSettingsMessages.metadata_keywords}"  styleClass="col-md-10 form-label"/>
            <div class="col-md-10">
                <h:inputText id="keywords" size="80" value="#{publishedSettings.keywords}" styleClass="form-control"/>
            </div>
        </div>

        <div class="form-group row">
            <h:outputLabel for="objectives" value="#{assessmentSettingsMessages.metadata_objectives}"  styleClass="col-md-10 form-label"/>
            <div class="col-md-10">
                <h:inputText id="objectives" value="#{publishedSettings.objectives}" styleClass="form-control"/>
            </div>
        </div>

         <div class="form-group row">
            <h:outputLabel for="rubrics" value="#{assessmentSettingsMessages.metadata_rubrics}"  styleClass="col-md-10 form-label"/>
            <div class="col-md-10">
                <h:inputText id="rubrics" value="#{publishedSettings.rubrics}" styleClass="form-control"/>
            </div>
        </div>
        
	<!-- QUESTION METADATA -->
        <h:outputLabel styleClass="samigo-sub-subheading" value="#{assessmentSettingsMessages.record_metadata}" />
        <div>
         <h:selectBooleanCheckbox id="metadataQuestions" rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
            value="#{publishedSettings.valueMap.hasMetaDataForQuestions}"/>
         <h:outputLabel for="metadataQuestions" value="#{assessmentSettingsMessages.metadata_questions}" rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
        </div>
        <h:outputLabel id="metadataQuestionsHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.metadata_questions_info}" />
    </h:panelGroup>

    <!-- TRACKING -->
    <div class="form-group row">
      <h:outputLabel styleClass="col-md-2 form-label" value="#{assessmentSettingsMessages.track_questions_title}"/>
      <div class="col-md-10">
        <h:selectBooleanCheckbox id="trackQuestions" value="#{publishedSettings.trackQuestions}"/>
          <h:outputLabel for="trackQuestions" value="#{assessmentSettingsMessages.track_questions_msg}" />
      </div>
    </div>
</samigo:hideDivision><!-- End the About this Assessment category -->

<!-- SECURITY AND PROCTORING -->
<samigo:hideDivision title="#{assessmentSettingsMessages.heading_security_proctoring}">
    <div class="samigo-subheading">
        <h:outputLabel rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true or assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}" value="#{assessmentSettingsMessages.heading_high_security}"/>
        <h:outputLabel rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable!=true and assessmentSettings.valueMap.passwordRequired_isInstructorEditable!=true}" value="#{assessmentSettingsMessages.high_security_none}"/>
    </div>
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.high_security_allow_only_specified_ip}" styleClass="col-md-10 form-label"/>
        <%-- no WYSIWYG for IP addresses --%>
        <div class="col-md-10">
            <h:inputTextarea value="#{publishedSettings.ipAddresses}" cols="40" rows="5"/>
            <h:outputLabel styleClass="d-block info-text small" value="#{assessmentSettingsMessages.ip_note}"/>
            <h:outputLabel styleClass="d-block info-text small" value="#{assessmentSettingsMessages.ip_example} #{assessmentSettingsMessages.ip_ex}"/>
        </div>
    </h:panelGroup>

    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel for="password" value="#{assessmentSettingsMessages.high_security_password}" styleClass="col-md-10 form-label"/>
        <div class="col-md-10">
            <h:inputText id="password" size="20" value="#{publishedSettings.password}" styleClass="form-control"/>
        </div>
    </h:panelGroup>

    <h:panelGroup layout="block" rendered="#{publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true && publishedSettings.secureDeliveryAvailable}">
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="secureDeliveryModule" value="#{assessmentSettingsMessages.require_secure_delivery}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="secureDeliveryModule" disabled="true" value="#{publishedSettings.secureDeliveryModule}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.secureDeliveryModuleSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebConfigMode" value="#{assessmentSettingsMessages.seb_config_mode}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebConfigMode" disabled="true" value="#{publishedSettings.sebConfigMode}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.sebConfigModeSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebAllowUserQuitSeb" value="#{assessmentSettingsMessages.seb_allow_user_quit_seb}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebAllowUserQuitSeb" disabled="true" value="#{publishedSettings.sebAllowUserQuitSeb}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel for="secureDeliveryModuleExitPassword" value="#{assessmentSettingsMessages.secure_delivery_exit_pwd}"
            styleClass="col-md-2 form-label" />
        <div class="col-md-10">
            <h:inputText id="secureDeliveryModuleExitPassword" disabled="true" size="14" value="#{publishedSettings.secureDeliveryModuleExitPassword}"
                maxlength="14" styleClass="form-control" />
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebShowTaskbar" value="#{assessmentSettingsMessages.seb_show_taskbar}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebShowTaskbar" disabled="true" value="#{publishedSettings.sebShowTaskbar}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebShowTime" value="#{assessmentSettingsMessages.seb_show_time}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebShowTime" disabled="true" value="#{publishedSettings.sebShowTime}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebShowKeyboardLayout" value="#{assessmentSettingsMessages.seb_show_keyboard_layout}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebShowKeyboardLayout" disabled="true" value="#{publishedSettings.sebShowKeyboardLayout}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebShowWifiControl" value="#{assessmentSettingsMessages.seb_show_wifi_control}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebShowWifiControl" disabled="true" value="#{publishedSettings.sebShowWifiControl}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebAllowAudioControl" value="#{assessmentSettingsMessages.seb_allow_audio_control}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebAllowAudioControl" disabled="true" value="#{publishedSettings.sebAllowAudioControl}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebAllowSpellChecking" value="#{assessmentSettingsMessages.seb_allow_spell_checking}" styleClass="col-md-2 form-label" />
        <div class="col-md-10">
          <h:selectOneRadio id="sebAllowSpellChecking" disabled="true" value="#{publishedSettings.sebAllowSpellChecking}" layout="pageDirection" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.booleanSelections}" />
          </h:selectOneRadio>
        </div>
      </h:panelGroup>
      <h:panelGroup rendered="#{publishedSettings.sebConfigMode == 'UPLOAD'}" styleClass="form-group row" layout="block">
        <label for="sebConfigUpload" class="col-md-2 form-label">
          <h:outputText value="#{assessmentSettingsMessages.seb_config_upload}" />
        </label>
        <div class="col-md-10 seb-upload">
          <h:outputLink id="sebConfigUploadLink" rendered="#{publishedSettings.sebConfigFileName != null}" value="/access/content#{publishedSettings.sebConfigUploadId}" target="new_window">
            <h:outputText value="#{publishedSettings.sebConfigFileName}" />
          </h:outputLink>
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebConfigKey" value="#{assessmentSettingsMessages.seb_config_key}" styleClass="col-md-2 form-label"/>
        <div class="col-md-10">
          <h:inputText id="sebConfigKey" styleClass="form-control" value="#{publishedSettings.sebConfigKey}" />
        </div>
      </h:panelGroup>
      <h:panelGroup styleClass="form-group row" layout="block">
        <h:outputLabel for="sebExamKeys" value="#{assessmentSettingsMessages.seb_exam_keys}" styleClass="col-md-2 form-label"/>
        <div class="col-md-10">
          <h:inputTextarea id="sebExamKeys" styleClass="form-control" value="#{publishedSettings.sebExamKeys}" cols="40" rows="5" />
          <label class="help-block info-text small">
            <h:outputText value="#{assessmentSettingsMessages.seb_exam_keys_upload_published_info}" />
          </label>
        </div>
      </h:panelGroup>
    </h:panelGroup>
</samigo:hideDivision><!-- END the Security and Proctoring category -->

<!-- AVAILABILITY AND SUBMISSIONS -->
<samigo:hideDivision title="#{assessmentSettingsMessages.heading_availability}"> 
  <!-- *** RELEASED TO *** -->
  <div class="form-group row">
      <h:outputLabel for="releaseTo" styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.released_to} " />
      <div class="col-md-10">
          <h:selectOneMenu id="releaseTo" disabled="true" value="#{publishedSettings.firstTargetSelected}" onclick="setBlockDivs();">
              <f:selectItems value="#{publishedSettings.publishingTargets}" />
          </h:selectOneMenu>
          <h:outputLabel id="releaseToHelp" rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true || assessmentSettings.valueMap.toGradebook_isInstructorEditable==true}"
                         styleClass="help-block info-text small" value="#{assessmentSettingsMessages.released_to_help}" />
       </div>
  </div>

  <h:panelGroup rendered="#{assessmentSettings.gradebookGroupEnabled == true}">
    <div class="sak-banner-info">
      <h:outputLabel value="#{assessmentSettingsMessages.multi_gradebook_use_info}"></h:outputLabel>
    </div>
  </h:panelGroup>

  <div id="groupDiv" class="groupTable form-group row col-md-offset-2 col-md-10">
    <h:selectManyListbox id="groupsForSite" disabled="true" value="#{publishedSettings.groupsAuthorized}">
      <f:selectItems value="#{publishedSettings.groupsForSite}" />
    </h:selectManyListbox>
  </div>

  <!-- NUMBER OF SUBMISSIONS -->
  <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.submissions_allowed}" />
      <div class="col-md-10 form-inline">
              <!-- Use the custom Tomahawk layout spread to style this radio http://myfaces.apache.org/tomahawk-project/tomahawk12/tagdoc/t_selectOneRadio.html -->
              <t:selectOneRadio id="unlimitedSubmissions" value="#{publishedSettings.unlimitedSubmissions}" layout="spread">
                <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
                <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
              </t:selectOneRadio>
              <ul class="submissions-allowed">
                <li class="my-1"><t:radio renderLogicalId="true" for="unlimitedSubmissions" index="0" /></li>
                <li class="my-1">
                  <t:radio renderLogicalId="true" for="unlimitedSubmissions" index="1" />
                  <span class="submissions-allowed">
                    <h:inputText size="5" id="submissions_Allowed" value="#{publishedSettings.submissionsAllowed}" />
                    <h:outputText value="&#160;" escape="false" />
                    <h:outputLabel for="submissions_Allowed" value="#{assessmentSettingsMessages.limited_submission}" />
                  </span>
                </li>
              </ul>
      </div>
  </h:panelGroup>
      
  <!-- *** DELIVERY DATES *** -->
  <div class="samigo-subheading">
        <h:outputLabel value="#{assessmentSettingsMessages.availability_title}"/>
  </div>
      <div class="form-group row">
          <h:outputLabel styleClass="col-md-10 form-label" for="startDate" value="#{assessmentSettingsMessages.assessment_available}"/>
          <div class="col-md-10">
              <h:inputText value="#{publishedSettings.startDateString}" size="25" id="startDate" />
          </div>
      </div>
      <div class="form-group row">
          <h:outputLabel styleClass="col-md-10 form-label" for="endDate" value="#{assessmentSettingsMessages.assessment_due}" />
          <div class="col-md-10">
              <h:inputText value="#{publishedSettings.dueDateString}" size="25" id="endDate"/>
              <h:outputText value="&#160;" escape="false" />
          </div>
      </div>

    <!-- LATE HANDLING -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <div class="row">
        <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.late_accept}" />
        <div class="col-md-10">
        <t:selectOneRadio id="lateHandling" value="#{publishedSettings.lateHandling}" onclick="checkLastHandling();" layout="spread">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.no_late}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.yes_late}"/>
        </t:selectOneRadio>
        <ul class="late-handling mb-1">
          <li class="my-1"><t:radio renderLogicalId="true" for="lateHandling" index="0" /></li>
          <li class="my-1">
            <t:radio renderLogicalId="true" for="lateHandling" index="1" />
            <h:outputLabel id="lateHandlingDeadlineLabel" value="#{assessmentSettingsMessages.yes_late_deadline}" />
            <h:outputText value="&#160;" escape="false" />
            <h:inputText value="#{publishedSettings.retractDateString}" size="25" id="retractDate"/>
          </li>
        </ul>
        <h:outputLabel id="lateHandlingHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.late_accept_help}" />
        <h:commandButton type="submit" value="#{assessmentSettingsMessages.button_stop_accepting_now}" action="confirmAssessmentRetract" />
      </div>
    </div>
  </h:panelGroup>

    <!-- *** TIMED *** -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.timedAssessment_isInstructorEditable==true}">
     <div class="row">
        <h:outputLabel styleClass="col-md-10 form-label mt-3" value="#{assessmentSettingsMessages.assessment_timed}" />
          <div class="col-md-10">
          <t:selectOneRadio id="selTimeAssess" value="#{publishedSettings.valueMap.hasTimeAssessment}" onclick="checkTimedRadio();setBlockDivs();" layout="spread" >
            <f:selectItem itemValue="false" itemLabel="#{assessmentSettingsMessages.assessment_not_timed}"/>
            <f:selectItem itemValue="true" itemLabel="#{assessmentSettingsMessages.assessment_is_timed}"/>
          </t:selectOneRadio>
          <ul class="selTimeAssess mb-1">
            <li>
              <t:radio renderLogicalId="true" for="selTimeAssess" index="0" />
            </li>
            <li>
              <t:radio renderLogicalId="true" for="selTimeAssess" index="1" />
              <h:outputLabel id="isTimedTimeLimitLabel" value="#{assessmentSettingsMessages.assessment_is_timed_limit} " />
              <h:outputText value="&#160;" escape="false" />
              <h:selectOneMenu id="timedHours" value="#{publishedSettings.timedHours}" >
                <f:selectItems value="#{publishedSettings.hours}" />
              </h:selectOneMenu>
              <h:outputText value="&#160;" escape="false" />
              <h:outputLabel id="timedHoursLabel"  value="#{assessmentSettingsMessages.timed_hours} " />
              <h:outputText value="&#160;" escape="false" />
              <h:selectOneMenu id="timedMinutes" value="#{publishedSettings.timedMinutes}" >
                <f:selectItems value="#{publishedSettings.mins}" />
              </h:selectOneMenu>
              <h:outputText value="&#160;" escape="false" />
              <h:outputLabel id="timedMinutesLabel" value="#{assessmentSettingsMessages.timed_minutes} " />
            </li>
          </ul>
          <h:outputLabel id="selTimeAssessHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.assessment_timed_info}" />
          <h:panelGroup rendered="#{publishedSettings.assessment.hasMultipleTimers() > 1}" layout="block" id="multipleTimersInfo" styleClass="sak-banner-warn">
            <h:outputText value="#{assessmentSettingsMessages.multiple_timers_detected}" />
          </h:panelGroup>
          </div>
     </div>
    </h:panelGroup>

  <!-- AUTOMATIC SUBMISSION -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-10 form-label mt-3" value="#{assessmentSettingsMessages.auto_submit}" />
    <div class="col-md-10">
      <h:selectBooleanCheckbox id="automaticSubmission" value="#{publishedSettings.autoSubmit}" />
      <h:outputLabel for="automaticSubmission" value="#{assessmentSettingsMessages.auto_submit_help}" />
      <h:outputLabel id="automaticSubmissionHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.auto_submit_info}" />
    </div>
  </h:panelGroup>

  <!-- SUBMISSION EMAILS -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.instructorNotification_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-10 form-label mt-3" value="#{assessmentSettingsMessages.instructorNotificationLabel}" />
    <div class="col-md-10">
      <t:selectOneRadio id="notificationEmailChoices" value="#{publishedSettings.instructorNotification}" layout="spread">
        <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.oneEmail}" />
        <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.digestEmail}" />
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.noEmail}" />
      </t:selectOneRadio>
      <ul class="email-notification">
        <li class="my-1"><t:radio renderLogicalId="true" for="notificationEmailChoices" index="0" /></li>
        <li class="my-1"><t:radio renderLogicalId="true" for="notificationEmailChoices" index="1" /></li>
        <li class="my-1"><t:radio renderLogicalId="true" for="notificationEmailChoices" index="2" /></li>
      </ul>
    </div>
  </h:panelGroup>

</samigo:hideDivision><!-- END the Availabity and Submissions category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_extended_time}" >
  <!-- Extended Time -->
  <%@ include file="inc/publishedExtendedTime.jspf"%>
</samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading_feedback}" >

  <!-- *** GRADING *** -->
  <!-- RECORDED SCORE AND MULTIPLES -->
    <div class="samigo-subheading">
      <h:outputLabel value="#{assessmentSettingsMessages.grading_scoring_title}" />
    </div>
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.recordedScore_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.recorded_score} " />
      <div class="col-md-10 form-inline">
        <t:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType1" rendered="#{author.canRecordAverage}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.average_score}"/>
        </t:selectOneRadio>
        <t:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType2" rendered="#{!author.canRecordAverage}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </t:selectOneRadio>
      </div>
    </h:panelGroup>

    <!-- info message about the anonymous and gradebook options below, will be shown only if quiz released to "Anonymous Users" -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true || publishedSettings.valueMap.toGradebook_isInstructorEditable==true}"
                  layout="block" id="gradingOptionsDisabledInfo" styleClass="row sak-banner-info" style="display: none">
        <h:outputText value="#{assessmentSettingsMessages.grading_options_disabled_info}" />
    </h:panelGroup>

    <!--  ANONYMOUS OPTION -->
    <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.student_identity_label}"/>
      <div class="col-md-10">
        <h:selectBooleanCheckbox id="anonymousGrading" disabled="#{publishedSettings.firstTargetSelected == 'Anonymous Users' || publishedSettings.editPubAnonyGradingRestricted}" value="#{publishedSettings.anonymousGrading}"/>
        <h:outputLabel for="anonymousGrading" value="#{assessmentSettingsMessages.student_identity}" />
      </div>
    </h:panelGroup>
    
    <!-- GRADEBOOK OPTION -->
    <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.toGradebook_isInstructorEditable==true}">
      <h:outputLabel for="toDefaultGradebook" styleClass="col-md-10 form-label mt-3" value="#{assessmentSettingsMessages.gradebook_options}"/>
      <div class="col-md-10">
        <h:selectOneRadio id="toDefaultGradebook" value="#{publishedSettings.toDefaultGradebook}"  layout="pageDirection"
          onchange="enableDisableToGradebook();toggleCategories(this);">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.to_default_gradebook}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.to_selected_gradebook}" itemDisabled="#{!publishedSettings.gradebookEnabled}"/>
        </h:selectOneRadio>
      </div>
      <!-- Gradebook Category (sub-setting) -->
      <h:panelGroup layout="block" id="toGradebookCategory" styleClass="col-md-10 col-md-offset-2" rendered="#{publishedSettings.categoriesEnabled}" style="#{publishedSettings.toDefaultGradebook == 1 ? 'display:block;' : 'display:none;'}">
        <h:outputLabel for="selectCategory" value="#{assessmentSettingsMessages.gradebook_category_select}" />
        <h:panelGroup rendered="#{!publishedSettings.gradebookGroupEnabled}">
          <h:selectOneMenu styleClass="categorySelect" id="selectCategory" value="#{publishedSettings.categorySelected}">
            <f:selectItems value="#{publishedSettings.categoriesSelectList}" />
          </h:selectOneMenu>
        </h:panelGroup>
        <h:panelGroup rendered="#{publishedSettings.gradebookGroupEnabled}">
          <sakai-multi-gradebook
            id="category-selector"
            site-id='<h:outputText value="#{publishedSettings.currentSiteId}" />'
            selected-temp='<h:outputText value="#{publishedSettings.categorySelected}" />'
            is-category='true'>
          </sakai-multi-gradebook>
          <h:inputHidden id="category_selector" value="#{publishedSettings.categorySelected}" />
        </h:panelGroup>
      </h:panelGroup>

      <!-- Gradebook Name (sub-setting) -->
      <h:panelGroup layout="block" id="toGradebookSelected" style="#{publishedSettings.toDefaultGradebook == 3 ? 'display:block;' : 'display:none;'}" styleClass="col-md-10 col-md-offset-2">
        <h:panelGroup rendered="#{!publishedSettings.gradebookGroupEnabled}">
          <h:selectOneMenu id="toGradebookName" value="#{publishedSettings.gradebookName}" rendered="#{publishedSettings.firstTargetSelected != 'Anonymous Users'}">
            <f:selectItem itemValue="" itemLabel="#{assessmentSettingsMessages.gradebook_item_select}" />
            <f:selectItems value="#{publishedSettings.existingGradebook}" />
          </h:selectOneMenu>
        </h:panelGroup>
        <h:panelGroup rendered="#{publishedSettings.gradebookGroupEnabled}">
          <sakai-multi-gradebook
            id="gb-selector"
            site-id='<h:outputText value="#{publishedSettings.currentSiteId}" />'
            selected-temp='<h:outputText value="#{publishedSettings.gradebookName}" />'
            app-name="sakai.samigo" ></sakai-multi-gradebook>
          <h:inputHidden id="gb_selector" value="#{publishedSettings.gradebookName}" />
        </h:panelGroup>
      </h:panelGroup>
    </h:panelGroup>

    <!-- *** FEEDBACK *** -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or publishedSettings.valueMap.feedbackType_isInstructorEditable==true or publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >
    <div class="samigo-subheading mt-4">
      <h:outputLabel value="#{assessmentSettingsMessages.heading_feedback}"/>
    </div>

    <!-- FEEDBACK AUTHORING -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" for="feedbackAuthoring" value="#{assessmentSettingsMessages.feedback_level}"/>
      <div class="col-md-10">
        <t:selectOneRadio id="feedbackAuthoring" value="#{publishedSettings.feedbackAuthoring}" layout="spread">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.questionlevel_feedback}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.sectionlevel_feedback}"/>
           <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.both_feedback}"/>
        </t:selectOneRadio>
        <ul class="feedback-authoring mb-1">
          <li><t:radio renderLogicalId="true" for="feedbackAuthoring" index="0" /></li>
          <li><t:radio renderLogicalId="true" for="feedbackAuthoring" index="1" /></li>
          <li><t:radio renderLogicalId="true" for="feedbackAuthoring" index="2" /></li>
        </ul>
        <h:outputLabel id="feedbackAuthoringHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.feedback_level_help}"/>
      </div>
    </h:panelGroup>

    <!-- FEEDBACK DELIVERY -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.feedbackType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label mt-2" for="feedbackDelivery" value="#{assessmentSettingsMessages.feedback_type}"/>
      <div class="col-md-10">
        <t:selectOneRadio id="feedbackDelivery" value="#{publishedSettings.feedbackDelivery}" onclick="setBlockDivs();disableAllFeedbackCheck();" layout="spread">
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.on_submission_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date}"/>
        </t:selectOneRadio>
        <ul class="feedback-delivery">
          <li class="my-1"><t:radio renderLogicalId="true" for="feedbackDelivery" index="0" /></li>
          <li class="my-1"><t:radio renderLogicalId="true" for="feedbackDelivery" index="1" /></li>
          <li class="my-1"><t:radio renderLogicalId="true" for="feedbackDelivery" index="2" /></li>
          <li class="my-1"><t:radio renderLogicalId="true" for="feedbackDelivery" index="3" /></li>
        </ul>
        <div id="feedbackByDatePanel" class="feedbackByDatePanel" style="display:none;">
            <h:outputLabel for="feedbackDate" value="#{assessmentSettingsMessages.feedback_start_date}"/> <h:inputText value="#{publishedSettings.feedbackDateString}" size="25" id="feedbackDate" />
            <div class="d-lg-none"><div class="clearfix"></div></div>
            <h:outputLabel for="feedbackEndDate" value="#{assessmentSettingsMessages.feedback_end_date}"/> <h:inputText value="#{publishedSettings.feedbackEndDateString}" size="25" id="feedbackEndDate" />
            <div class="clearfix"></div><br/>
            <h:selectBooleanCheckbox value="#{publishedSettings.feedbackScoreThresholdEnabled}" id="feedbackScoreThresholdEnabled"/> <h:outputLabel for="feedbackScoreThresholdEnabled" value="#{assessmentSettingsMessages.feedback_score_threshold}"/> <h:inputText id="feedbackScoreThreshold" size="4" value="#{publishedSettings.feedbackScoreThreshold}"/>&#37;
        </div>
      </div>
    </h:panelGroup>
 
    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}">

      <div class="form-group row">
        <h:outputLabel styleClass="col-md-10 form-label" for="feedbackComponentOption" value="#{assessmentSettingsMessages.feedback_components}"/>
        <div class="col-md-10">
            <t:selectOneRadio id="feedbackComponentOption" value="#{publishedSettings.feedbackComponentOption}" onclick="setBlockDivs();disableOtherFeedbackComponentOption();" layout="pageDirection">
                <f:selectItem itemValue="1" itemLabel="#{templateMessages.feedback_components_totalscore_only}"/>
                <f:selectItem itemValue="2" itemLabel="#{templateMessages.feedback_components_select}"/>
            </t:selectOneRadio>
            <div class="respChoice indent1" style="display:none;">
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:outputLabel styleClass="samigo-sub-subheading" value="#{assessmentSettingsMessages.feedback_subheading_answers}" />
                </h:panelGroup>
                <h:panelGroup styleClass="" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showStudentResponse}" id="feedbackCheckbox11"/>
                    <h:outputLabel for="feedbackCheckbox1" value="#{assessmentSettingsMessages.student_response}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showCorrectResponse}" id="feedbackCheckbox13" onclick="changeStatusCorrectResponseCheckbox()"/>
                    <h:outputLabel for="feedbackCheckbox3" value="#{assessmentSettingsMessages.correct_response}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline indent1" id="hideCorrectResponse" layout="block" style="display: #{(publishedSettings.showCorrectResponse) ? 'block' : 'none'}">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showCorrection}" id="feedbackCheckbox19"/>
                    <h:outputLabel for="feedbackCheckbox19" value="#{assessmentSettingsMessages.show_correct_response}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showQuestionLevelFeedback}" id="feedbackCheckbox12"/>
                    <h:outputLabel for="feedbackCheckbox2" value="#{assessmentSettingsMessages.question_level_feedback}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showSelectionLevelFeedback}" id="feedbackCheckbox14"/> 
                    <h:outputLabel for="feedbackCheckbox4" value="#{assessmentSettingsMessages.selection_level_feedback}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:outputLabel styleClass="samigo-sub-subheading mt-3" value="#{assessmentSettingsMessages.feedback_subheading_comments}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showGraderComments}" id="feedbackCheckbox16"/>
                    <h:outputLabel for="feedbackCheckbox6" value="#{assessmentSettingsMessages.graders_comments}" />
                    <h:outputLabel id="feedbackCheckbox6HelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.graders_comments_info} "/>
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:outputLabel styleClass="samigo-sub-subheading mt-3" value="#{assessmentSettingsMessages.feedback_subheading_scores}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showStudentQuestionScore}" id="feedbackCheckbox17"/>
                    <h:outputLabel for="feedbackCheckbox7" value="#{assessmentSettingsMessages.student_question_score}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showStudentScore}" id="feedbackCheckbox15"/>
                    <h:outputLabel for="feedbackCheckbox5" value="#{assessmentSettingsMessages.student_assessment_score}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:outputLabel styleClass="samigo-sub-subheading mt-3" value="#{assessmentSettingsMessages.feedback_subheading_additonal_info}" />
                </h:panelGroup>
                <h:panelGroup styleClass="form-inline" layout="block">
                    <h:selectBooleanCheckbox value="#{publishedSettings.showStatistics}" id="feedbackCheckbox18"/>
                    <h:outputLabel for="feedbackCheckbox8" value="#{assessmentSettingsMessages.statistics_and_histogram}" />
                </h:panelGroup>
          </div>
        </div>
      </div>

     </h:panelGroup>
   </h:panelGroup>
 </samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_layout}" >

  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true or publishedSettings.valueMap.displayChunking_isInstructorEditable==true or publishedSettings.valueMap.displayNumbering_isInstructorEditable==true }" >

    <!-- NAVIGATION -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" for="itemNavigation" value="#{assessmentSettingsMessages.navigation}" />
      <div class="col-md-10">
        <t:selectOneRadio id="itemNavigation" value="#{publishedSettings.itemNavigation}" layout="spread" onclick="setBlockDivs();updateItemNavigation(true);">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.random_access}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.linear_access}"/>
        </t:selectOneRadio>
        <ul class="layout-navigation mb-1">
          <li><t:radio renderLogicalId="true" for="itemNavigation" index="0" /></li>
          <li><t:radio renderLogicalId="true" for="itemNavigation" index="1" /></li>
        </ul>
        <h:outputLabel id="itemNavigationHelpBlock" styleClass="help-block info-text small" value="#{assessmentSettingsMessages.linear_access_warning} "/>
      </div>
    </h:panelGroup>

    <!-- QUESTION LAYOUT -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayChunking_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label mt-2" for="assessmentFormat" value="#{assessmentSettingsMessages.question_layout}" />
      <div class="col-md-10">
        <t:selectOneRadio id="assessmentFormat" value="#{publishedSettings.assessmentFormat}" layout="spread">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.layout_by_assessment}"/>
        </t:selectOneRadio>
        <ul class="layout-format">
          <li><t:radio renderLogicalId="true" for="assessmentFormat" index="0" /></li>
          <li><t:radio renderLogicalId="true" for="assessmentFormat" index="1" /></li>
          <li><t:radio renderLogicalId="true" for="assessmentFormat" index="2" /></li>
        </ul>
      </div>
    </h:panelGroup>

  <!-- *** MARK FOR REVIEW *** -->
  <!-- *** (disabled for linear assessment) *** -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.markForReview_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-10 form-label" value="#{assessmentSettingsMessages.mark_for_review}" />
    <div class="col-md-10">
      <h:selectBooleanCheckbox id="markForReview1" value="#{publishedSettings.isMarkForReview}"/>
      <h:outputLabel for="markForReview1" value="#{assessmentSettingsMessages.mark_for_review_label}"/>
    </div>

  </h:panelGroup>
  <!-- NUMBERING -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayNumbering_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" for="itemNumbering" value="#{assessmentSettingsMessages.numbering}" />
      <div class="col-md-10">
         <t:selectOneRadio id="itemNumbering" value="#{publishedSettings.itemNumbering}" layout="spread">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.part_numbering}"/>
         </t:selectOneRadio>
         <ul class="layout-numbering">
           <li><t:radio renderLogicalId="true" for="itemNumbering" index="0" /></li>
           <li><t:radio renderLogicalId="true" for="itemNumbering" index="1" /></li>
         </ul>
      </div>
    </h:panelGroup>
  </h:panelGroup>

   <!-- Display Scores -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayScores_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-10 form-label" for="displayScores" value="#{assessmentSettingsMessages.displayScores}" /> 
      <div class="col-md-10">
         <t:selectOneRadio id="displayScores" value="#{publishedSettings.displayScoreDuringAssessments}" layout="spread">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.displayScores_show}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.displayScores_hide}"/>
         </t:selectOneRadio>
         <ul class="display-scores">
           <li><t:radio renderLogicalId="true" for="displayScores" index="0" /></li>
           <li><t:radio renderLogicalId="true" for="displayScores" index="1" /></li>
         </ul>
      </div>
    </h:panelGroup>
    
   <!-- *** COLORS AND GRAPHICS *** -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.bgColor_isInstructorEditable==true and publishedSettings.backgroundColorEnabled==true}" >
      <h:outputLabel for="color-input" styleClass="col-md-2" value="#{assessmentSettingsMessages.background_label}" />
         <div class="col-md-10">
          <h:selectOneRadio onclick="uncheckOther(this)" id="background_color" value="#{publishedSettings.bgColorSelect}">
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_color}"/>
         </h:selectOneRadio>
         <h:inputText size="10" value="#{publishedSettings.bgColor}" id="pickColor" /></br>
         <input id="color-input" type="color">
         <h:selectOneRadio onclick="uncheckOther(this)" id="background_image" value="#{publishedSettings.bgImageSelect}"  >
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_image}"/>
         </h:selectOneRadio>  
         <h:inputText size="80" value="#{publishedSettings.bgImage}"/>
      </div>
    </h:panelGroup>
    

<!-- *** SUBMISSION MESSAGE *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true or publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
<div class="samigo-subheading">
   <h:outputLabel value="#{assessmentSettingsMessages.heading_submission_message}"/>
</div>
    <h:panelGroup layout="block" styleClass="form-group row" rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.submission_message}" styleClass="col-md-10 form-label" />
        <div class="col-md-10">
            <h:panelGrid>
                <samigo:wysiwyg rows="140" value="#{publishedSettings.submissionMessage}" hasToggle="yes" mode="author">
                    <f:validateLength maximum="4000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </h:panelGroup>
    <h:panelGroup  layout="block" styleClass="form-group row" rendered="#{publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}">
        <h:outputLabel for="finalPageUrl" value="#{assessmentSettingsMessages.submission_final_page_url}"  styleClass="col-md-10 form-label"/>
        <div class="col-md-10">
            <h:inputText size="80" id="finalPageUrl" value="#{publishedSettings.finalPageUrl}" styleClass="form-control"/>
            <h:commandButton value="#{assessmentSettingsMessages.validateURL}" type="button" onclick="javascript:validateUrl();"/>
        </div>
    </h:panelGroup>
</h:panelGroup>

</samigo:hideDivision><!-- END Layout and Appearance Category -->
</div> <!-- END of #jqueryui-accordion -->

 <p class="act">

  <!-- Save button -->
  <h:commandButton type="submit" value="#{commonMessages.action_save}" action="#{publishedSettings.getOutcome}"  styleClass="active" onclick="setBlockDivs();updateItemNavigation(false);SPNR.disableControlsAndSpin(this, null);" >
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SavePublishedSettingsListener" />
  </h:commandButton>
  
  <!-- Cancel button -->
  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="#{author.getFromPage}" rendered="#{author.fromPage != 'editAssessment'}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPublishedAssessmentAttachmentListener" />
  </h:commandButton>

  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editAssessment" rendered="#{author.fromPage == 'editAssessment'}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPublishedAssessmentAttachmentListener" />
	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
