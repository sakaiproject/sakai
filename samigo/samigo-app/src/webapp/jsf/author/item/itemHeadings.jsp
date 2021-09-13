<%-- Headings for item edit pages, needs to have msg=AuthorMessages.properties.  --%>
<!-- Core files -->
<script>
function changeTypeLink(field){

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if ( document.links[i].id.indexOf("hiddenlink") >=0){
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

jQuery(document).ready(function() {
	var itemType = "${itemauthor.currentItem.itemType}";
	if ( itemType == 2 || itemType == 12 ) {
		$('#itemFormHeading\\:changeQType2').find('option[value=1]').attr('selected', true);
	}
	var itemAuthorTarget = "${itemauthor.target}";
	if(itemAuthorTarget == 'questionpool') {
		$('#itemFormHeading\\:changeQType2').find('option:last').remove();
	}
});

//Display the EMI question example
function displayEMIHelp(){
    window.open('../../../../../../samigo-app/emi/help.txt', '_blank', 'location=no,menubar=no,status=no,toolbar=no');
}
</script>
<h:form id="itemFormHeading">
<%-- The following hidden fields echo some of the data in the item form
     when this form posts a change in item type, the data is persisted.
     We don't keep the answers--probably misleading for the new type.
--%>
<%-- score --%>
<h:inputHidden value="#{itemauthor.currentItem.itemScore}" />
<%-- non-matching type questions --%>
<h:inputHidden value="#{itemauthor.currentItem.itemText}" />
<%-- matching questions --%>
<h:inputHidden value="#{itemauthor.currentItem.instruction}" />
<%-- feedback --%>
<h:inputHidden value="#{itemauthor.currentItem.corrFeedback}" />
<h:inputHidden value="#{itemauthor.currentItem.incorrFeedback}" />
<h:inputHidden value="#{itemauthor.currentItem.generalFeedback}" />
<%-- --%>

<ul class="navIntraTool actionToolbar" role="menu">
    <h:panelGroup rendered="#{authorization.createAssessment or authorization.editAnyAssessment or authorization.editOwnAssessment or authorization.gradeAnyAssessment or authorization.gradeOwnAssessment}">
        <li role="menuitem">
            <span>
                <h:commandLink title="#{generalMessages.t_assessment}" action="author" immediate="true">
                    <h:outputText value="#{generalMessages.assessment}" />
                </h:commandLink>
            </span>
        </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{authorization.createAssessment}">
        <li role="menuitem">
            <span>
                <h:commandLink title="#{generalMessages.add}" action="#{author.getOutcome}" immediate="true">
                    <f:param name="action" value="create_assessment_title" />
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
                    <h:outputText value="#{generalMessages.add}" />
                </h:commandLink>
            </span>
        </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{authorization.adminTemplate and template.showAssessmentTypes}">
        <li role="menuitem">
            <span>
                <h:commandLink title="#{generalMessages.t_template}" action="template" immediate="true">
                    <h:outputText value="#{generalMessages.template}" />
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
                </h:commandLink>
            </span>
        </li>
    </h:panelGroup>
    <li role="menuitem">
        <span>
            <h:commandLink title="#{generalMessages.t_questionPool}" action="poolList" immediate="true">
                <h:outputText value="#{generalMessages.questionPool}" />
            </h:commandLink>
        </span>
    </li>
    <li role="menuitem">
        <span>
            <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true" rendered="#{authorization.adminQuestionPool}">
                <h:outputText value="#{generalMessages.eventLog}" />
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
            </h:commandLink>
        </span>
    </li>
    <li role="menuitem">
        <span>
            <h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true" rendered="#{authorization.adminQuestionPool}">
                <h:outputText value="#{generalMessages.section_activity}" />
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
            </h:commandLink>
        </span>
    </li>
</ul>

<br/>

<!-- breadcrumb-->
<ol class="breadcrumb">
  <h:panelGroup rendered="#{itemauthor.target == 'assessment'}" >
    <li>
      <h:commandLink title="#{authorMessages.t_assessment}" action="author" immediate="true">
        <h:outputText value="#{authorMessages.global_nav_assessmt}" />
      </h:commandLink>
    </li>
    <li>
      <h:commandLink title="#{authorMessages.t_question}" action="editAssessment" immediate="true" >
        <h:outputText value="#{authorMessages.qs}#{authorMessages.column} #{assessmentBean.title}" escape="false"/>
      </h:commandLink>
    </li>
    <li>
      <h:outputText value="#{authorMessages.q} #{itemauthor.itemNo}" />
    </li>
  </h:panelGroup>
  <h:panelGroup rendered="#{itemauthor.target == 'questionpool'}">
    <li>
      <h:outputText value="#{authorMessages.global_nav_pools}" />
    </li>
    <samigo:dataLine value="#{questionpool.currentPool.parentPoolsArray}" var="parent" separator="" first="0" rows="100" >
      <h:column>
        <li>
          <h:commandLink action="#{questionpool.editPool}" immediate="true">
            <h:outputText value="#{parent.displayName}" escape="false"/>
            <f:param name="qpid" value="#{parent.questionPoolId}"/>
          </h:commandLink>
        </li>
      </h:column>
    </samigo:dataLine>
    <li>
      <h:commandLink action="#{questionpool.editPool}" immediate="true">
        <h:outputText value="#{questionpool.currentPool.displayName}"/>
        <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
      </h:commandLink>
    </li>
  </h:panelGroup>
</ol>

<div class="page-header">
  <h1>
    <h:outputText value="#{authorMessages.modify_q}#{authorMessages.column} "/>
    <small> 
      <h:outputText value="#{assessmentBean.title}" rendered="#{itemauthor.target == 'assessment'}" escape="false"/>
    </small>
  </h1>
</div>

<!-- SUBHEADING -->
<h3>
     <h:outputText value="#{authorMessages.q}"/>
     <h:outputText rendered="#{itemauthor.target == 'assessment'}" value="#{itemauthor.itemNo}"/>
  <small class="rightNav">
     <h:outputText value=" #{authorMessages.dash} "/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType == 1}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 2}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 12}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 3}" value="#{authorMessages.multiple_choice_surv}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 4}" value="#{authorMessages.true_false}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 5}" value="#{authorMessages.short_answer_essay}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 8}" value="#{authorMessages.fill_in_the_blank}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 11}" value="#{authorMessages.fill_in_numeric}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 9}" value="#{authorMessages.matching}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 7}" value="#{authorMessages.audio_recording}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 6}" value="#{authorMessages.file_upload}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 10}" value="#{authorMessages.import_from_q}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 14}" value="#{authorMessages.extended_matching_items}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 13}" value="#{authorMessages.matrix_choices_surv}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 15}" value="#{authorMessages.calculated_question}"/><!-- // CALCULATED_QUESTION -->
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 16}" value="#{authorMessages.image_map_question}"/><!-- // IMAGE MAP_QUESTION -->
  <h:commandLink title="#{authorMessages.t_removeQ}" rendered="#{author.isEditPendingAssessmentFlow && itemauthor.currentItem.itemId != null}" styleClass="navList" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}">
                <h:outputText value="#{commonMessages.remove_action}" />
                <f:param name="itemid" value="#{itemauthor.currentItem.itemId}"/>
              </h:commandLink>
  </small>
</h3>

<h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<!-- CHANGE TYPE -->
<div class="form-group row">
    <h:outputLabel for="changeQType2" styleClass="col-md-2" value="#{authorMessages.change_q_type} &#160;" escape="false" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow || (itemauthor.target == 'questionpool' && itemauthor.itemType == '')}"/>
  <div class="col-md-10">

<h:selectOneMenu onchange="changeTypeLink(this);" value="#{itemauthor.currentItem.itemType}" required="true" id="changeQType2">
  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
</h:selectOneMenu>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="" styleClass="hidden">
</h:commandLink>

&nbsp;
<h:outputLink title="#{authorMessages.example_emi_question}" value="#" rendered="#{itemauthor.currentItem.itemType == 14}" 
        onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
        onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" (#{authorMessages.example_emi_question})"/>
</h:outputLink>

<h:message for="changeQType2" infoClass="sak-banner-info" warnClass="sak-banner-warn" errorClass="sak-banner-error" fatalClass="sak-banner-error"/>
</div>
</div>
</h:form>
