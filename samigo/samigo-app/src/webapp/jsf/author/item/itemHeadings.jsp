<%--
Headings for item edit pages, needs to have msg=AuthorMessages.properties.
--%>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
<!-- Core files -->
<script type="text/JavaScript">
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

//Display the EMI question example
function displayEMIHelp(){
	window.open('../../../../../../samigo-app/emi/help.txt', '_blank', 'location=no,menubar=no,status=no,toolbar=no');
}
//-->
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

<f:verbatim><ul class="navIntraTool actionToolbar" role="menu">
<li role="menuitem" class="firstToolBarItem"><span></f:verbatim>

    <h:commandLink title="#{generalMessages.t_assessment}" action="author" immediate="true">
      <h:outputText value="#{generalMessages.assessment}" />
    </h:commandLink>

<f:verbatim></span></li></f:verbatim>

<h:panelGroup rendered="#{authorization.adminTemplate and template.showAssessmentTypes}">
<f:verbatim><li role="menuitem" ><span></f:verbatim>

    <h:commandLink title="#{generalMessages.t_template}" action="template" immediate="true">
      <h:outputText value="#{generalMessages.template}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
    </h:commandLink>

<f:verbatim></span></li></f:verbatim>
</h:panelGroup>

<f:verbatim><li role="menuitem" ><span></f:verbatim>

    <h:commandLink title="#{generalMessages.t_questionPool}" action="poolList" immediate="true">
      <h:outputText value="#{generalMessages.questionPool}" />
    </h:commandLink>

<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
    <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.eventLog}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
	<h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true" rendered="#{authorization.adminQuestionPool}">
		<h:outputText value="#{generalMessages.section_activity}" />
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
	</h:commandLink>
<f:verbatim></span></li> 
</ul>
<br/></f:verbatim>

<!-- breadcrumb-->
<div>
    <h:commandLink title="#{authorMessages.t_assessment}" rendered="#{itemauthor.target == 'assessment'}" action="author" immediate="true">
      <h:outputText value="#{authorMessages.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText rendered="#{itemauthor.target == 'assessment'}" value=" #{authorMessages.greater} " />
    <h:commandLink title="#{authorMessages.t_question}" action="editAssessment" immediate="true" rendered="#{itemauthor.target == 'assessment'}">
      <h:outputText value="#{authorMessages.qs}#{authorMessages.column} #{assessmentBean.title}" escape="false"/>
    </h:commandLink>
    <h:outputText value=" #{authorMessages.greater} " rendered="#{itemauthor.target == 'assessment'}" />
    <h:outputText value="#{authorMessages.q} #{itemauthor.itemNo}" rendered="#{itemauthor.target == 'assessment'}"/>

</div>
<div>
<h:outputText rendered="#{itemauthor.target == 'questionpool'}" value="#{authorMessages.global_nav_pools}> "/>

<samigo:dataLine rendered="#{itemauthor.target == 'questionpool'}" value="#{questionpool.currentPool.parentPoolsArray}" var="parent"
   separator=" > " first="0" rows="100" >
  <h:column>
    <h:commandLink action="#{questionpool.editPool}"  immediate="true">
      <h:outputText value="#{parent.displayName}" />
      <f:param name="qpid" value="#{parent.questionPoolId}"/>
    </h:commandLink>
  </h:column>
</samigo:dataLine>
<h:outputText rendered="#{questionpool.currentPool.showParentPools && itemauthor.target == 'questionpool'}" value=" #{authorMessages.greater} " />
<h:commandLink rendered="#{itemauthor.target == 'questionpool'}" action="#{questionpool.editPool}"  immediate="true">
  <h:outputText value="#{questionpool.currentPool.displayName}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
</div>

<h3>
   <h:outputText value="#{authorMessages.modify_q}#{authorMessages.column} "/>
   <h:outputText value="#{assessmentBean.title}" rendered="#{itemauthor.target == 'assessment'}" escape="false"/>
</h3>
<!-- CHANGE TYPE -->
<div class="tier1">
<div class=" shorttext"><h:outputLabel value="#{authorMessages.change_q_type}" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow || (itemauthor.target == 'questionpool' && itemauthor.itemType == '')}"/>
<%-- todo:
listener set selectFromQuestionPool, eliminating the rendered attribute
--%>

<%-- from question pool context, do not show question pool as option --%>
<h:selectOneMenu rendered="#{(itemauthor.target == 'assessment' && questionpool.importToAuthoring == 'true') || (itemauthor.target == 'questionpool' && itemauthor.itemType == '')}" onchange="changeTypeLink(this);"
  value="#{itemauthor.currentItem.itemType}" required="true" id="changeQType1">
  <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />

  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
</h:selectOneMenu>

<%-- not from qpool , show the last option: copy from question pool --%>
<h:selectOneMenu onchange="changeTypeLink(this);" rendered="#{author.isEditPendingAssessmentFlow && itemauthor.target == 'assessment' && questionpool.importToAuthoring == 'false'}"
  value="#{itemauthor.currentItem.itemType}" required="true" id="changeQType2">
  <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />

  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
</h:selectOneMenu>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
</h:commandLink>

&nbsp;
<h:outputLink title="#{authorMessages.example_emi_question}" value="#" rendered="#{itemauthor.currentItem.itemType == 14}" 
		onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
		onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
	<h:outputText  value=" (#{authorMessages.example_emi_question})"/>
</h:outputLink>

<h:message rendered="#{questionpool.importToAuthoring == 'true' && itemauthor.target == 'assessment'}" for="changeQType1" infoClass="messageSamigo" warnClass="validation" errorClass="messageSamigo" fatalClass="messageSamigo"/>
<h:message rendered="#{questionpool.importToAuthoring == 'false' && itemauthor.target == 'assessment'}" for="changeQType2" infoClass="messageSamigo" warnClass="messageSamigo" errorClass="messageSamigo" fatalClass="messageSamigo"/>
</div>
<!-- SUBHEADING -->
<p class="navModeAction">
  <span class="leftNav">
   <b>
     <h:outputText value="#{authorMessages.q}"/>
     <h:outputText rendered="#{itemauthor.target == 'assessment'}" value="#{itemauthor.itemNo}"/>
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
   </b>
 </span>
 <span class="rightNav">

 <%--
  temporily comment put Preview link for a specific question in Author. It will not be the feature in Sam 1.5.
  <h:commandLink id="preview" immediate="true" action="preview">
          <h:outputText value="#{commonMessages.action_preview}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
  </h:commandLink>


  <h:outputText rendered="#{itemauthor.currentItem.itemId != null}" value=" #{authorMessages.separator} " />
--%>
  <h:commandLink title="#{authorMessages.t_removeQ}" rendered="#{author.isEditPendingAssessmentFlow && itemauthor.currentItem.itemId != null}" styleClass="navList" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}">
                <h:outputText value="#{commonMessages.remove_action}" />
                <f:param name="itemid" value="#{itemauthor.currentItem.itemId}"/>
              </h:commandLink>


<%-- removed MyQuestion link per new mockup
  <h:outputText rendered="#{itemauthor.target == 'assessment' && itemauthor.currentItem.itemId != null}" value=" | " />
  <h:commandLink immediate="true" rendered="#{itemauthor.target == 'assessment'}" action="editAssessment">
    <h:outputText value="#{authorMessages.my_qs}" />
  </h:commandLink>
--%>
 </span>
    <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
</p>
</div>
</h:form>
