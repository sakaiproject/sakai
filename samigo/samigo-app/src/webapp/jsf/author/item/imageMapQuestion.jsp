<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id: matching.jsp 124679 2013-05-20 17:09:41Z ktsao@stanford.edu $
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
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
	  
	  <samigo:script path="/../library/webjars/jquery/1.12.4/jquery.min.js"/>
	  <samigo:script path="/js/jquery.dynamiclist.author.js"/>
	  <samigo:script path="/js/selection.author.js"/>
	  
	  <samigo:stylesheet path="/css/imageQuestion.author.css"/>
<%--
<script type="text/JavaScript">
<!--
<%@ include file="/js/authoring.js" %>
//-->
</script>
--%>

<script type="text/JavaScript">

	var dynamicList = new DynamicList('itemForm\\:serialized', 'template', {selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'imageContainer');
			
	jQuery(window).load(function(){
		if($('#itemForm\\:serialized').val() != '')
			dynamicList.fillElements();
		else
			dynamicList.addElement('<h:outputText value="#{authorMessages.im_description}" escape="false" />', true,true);
	});
	
	function resetList()
	{
		dynamicList.resetElements();
		dynamicList.serializeElements();
	}
	
	function validate(){
		if (validateDescriptions()){
			return validateZones();
		}
		return false;
	}
	
	function validateDescriptions(){
		
		var ok = true;
		
		$('input[id^=value_]').each(function(){
			if (!$.trim(this.value).length){
				ok = false;
				return false;
			}
		});
		
		if (!ok){
			alert("<h:outputText value="#{authorMessages.all_im_descriptions_needed}" />");
		}
		
		return ok;
	}
	
	function validateZones()
	{
		var val = dynamicList.validateElements();
		if(val == 0)
		{
			alert("<h:outputText value="#{authorMessages.all_im_zones_needed}" />");
			return false;
		}
		dynamicList.serializeElements();
		return true;
	}
</script>
      </head>
<%-- unfortunately have to use a scriptlet here --%>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<%--
      <body onload="javascript:initEditors('<%=request.getContextPath()%>');;<%= request.getAttribute("html.body.onload") %>" >
--%>

<div class="portletBody container-fluid">
<!-- content... -->
<!-- FORM -->

<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>

<h:form id="itemForm" enctype="multipart/form-data">

	
<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active" onclick="return validateZones()">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active" onclick="return validateZones()">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>
</p>

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
  <div style="display:none">
  <h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
  </div>


    <!-- 1 POINTS -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-2">
            <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" 
                required="true" disabled="#{author.isEditPoolFlow}" styleClass="ConvertPoint form-control">
                <f:validateDoubleRange minimum="0.00"/>
            </h:inputText>
            <h:message for="answerptr" styleClass="validate"/>
        </div>
    </div>
    
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-5 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>    

    <!-- 2 TEXT -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.q_text_image_map}" styleClass="col-md-12 form-control-label"/> 
        <!-- WYSIWYG -->
        <div class="col-md-12">
            <h:panelGrid>
                <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes">
                    <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

    <!-- 2a ATTACHMENTS -->
    <%@ include file="/jsf/author/item/attachment.jsp" %>
  
    <hr class="itemSeparator" />
    <br />
  
    <!-- IMAGE SRC -->
    <div class="form-group row"> 
        <h:outputLabel value="#{authorMessages.image_map_src}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <corejsf:upload
                target="jsf/upload_tmp/assessment#{assessmentBean.assessmentId}/question#{itemauthor.currentItem.itemId}/#{person.eid}"
                valueChangeListener="#{itemauthor.addImageToQuestion}" />
            <h:commandButton id="upl" action="imageMapItem" value="#{deliveryMessages.upload}" onclick='resetList()'/>
        </div>
    </div>
  
    <!-- 3 ANSWER -->
    <div class="longtext"> 
        <h:outputLabel value="#{authorMessages.create_IM_pairing} " />
    </div>
    <h:inputHidden id="serialized" value="#{itemauthor.currentItem.serializedImageMap}" /> 

    <div>
        <input type='button' onclick="dynamicList.addElement('<h:outputText value="#{authorMessages.im_description}" escape="false" />', true,true)" value="+" style="margin-left: 45px" />
        <div id='template' style='display:none'>	
            <span name='position_'></span>
            <span>
                <div id='btnSelect_'></div>
            </span>
            <span>
                <input type='text' name='value_' style='width : 200px'/>
            </span>
            <span>
                <input type='button' name='btnRemove_' onclick="dynamicList.removeElement(this)" value="-" />
            </span>
        </div>
        <h:outputLabel value="#{authorMessages.im_add_item} " />
    </div> <br />
  
 
    <div onmousedown="return false" id="imageContainer" class='authorImageContainer'>
        <img id='img' src='<h:outputText value="#{itemauthor.currentItem.imageMapSrc}" />' />
    </div>

    <!-- Match FEEDBACK -->
    <br/>
    <!-- REQUIRE ALL OK -->
    <div class="form-group row">   
        <h:outputLabel value="#{authorMessages.require_all_ok}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.requireAllOk}" >
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>

    <hr class="itemSeparator" />

    <!-- 6 PART -->
    <h:panelGroup rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}" layout="block" styleClass="form-group row">
        <h:outputLabel value="#{authorMessages.assign_to_p}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                <f:selectItems  value="#{itemauthor.sectionSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- 7 POOL -->
    <h:panelGroup rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}" layout="block" styleClass="form-group row">
        <h:outputLabel value="#{authorMessages.assign_to_question_p}" styleClass="col-md-2 form-control-label"/>
        <%-- stub debug --%>
        <div class="col-md-10">
            <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
                <f:selectItems value="#{itemauthor.poolSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- 8 FEEDBACK -->
    <h:panelGroup  layout="block" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
        <h:outputLabel value="#{authorMessages.correct_incorrect_an}" />
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_answer_opti}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-10">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" >
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.incorrect_answer_op}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-10">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" >
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
    </h:panelGroup>


    <!-- METADATA -->
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext" layout="block">
        <h:outputLabel value="Metadata"/>
        <div class="form-group row">
            <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-6">
                <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}"  styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-6">
                <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}"  styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-6">
                <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
            </div>
        </div>
    </h:panelGroup>

    <%@ include file="/jsf/author/item/tags.jsp" %>


<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active" onclick="return validate()">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active" onclick="return validate()">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>

<script type="text/javascript">
applyMenuListener("controllingSequence");
</script>
    </body>
  </html>
</f:view>

