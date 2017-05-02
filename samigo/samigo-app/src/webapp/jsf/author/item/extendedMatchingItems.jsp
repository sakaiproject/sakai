<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"
	language="java"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id: extendedMatchingItems.jsp 59352 2009-03-31 16:59:41Z gopal@zestware.com $
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

<f:view>
	<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%=request.getAttribute("html.head")%>
<title><h:outputText
		value="#{authorMessages.item_display_author}" />
</title>
<!-- AUTHORING -->
<link type="text/css"
	rel="Stylesheet" />
<script type="text/javascript">
	var emiAuthoring = true;
</script>

<samigo:script path="/js/authoring.js" />
<samigo:script path="/js/utils-emi.js" />
<samigo:script path="/js/authoring-emi.js" />
</head>
<body onload="<%=request.getAttribute("html.body.onload")%>">
    
	<div id="portletContent" class="portletBody" style="display: none;">
		<!-- content... -->
		<!-- FORM -->
		<!-- HEADING -->
		<%@ include file="/jsf/author/item/itemHeadings.jsp"%>
		<table width="80%">
			<tbody id='emiErrorMessageTable'>
			</tbody>
		</table>

		<h:form id="itemForm" onsubmit="return editorCheck();">
			<p class="act">
				<h:commandButton rendered="#{itemauthor.target=='assessment'}"
					value="#{authorMessages.button_save}"
					action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
				</h:commandButton>

				<h:commandButton rendered="#{itemauthor.target=='questionpool'}"
					value="#{authorMessages.button_save}"
					action="#{itemauthor.currentItem.getPoolOutcome}"
					styleClass="active">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
				</h:commandButton>


				<h:commandButton rendered="#{itemauthor.target=='assessment'}"
					value="#{commonMessages.cancel_action}" action="editAssessment"
					immediate="true">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
				</h:commandButton>

				<h:commandButton rendered="#{itemauthor.target=='questionpool'}"
					value="#{commonMessages.cancel_action}" action="editPool"
					immediate="true">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
				</h:commandButton>
			</p>

			<!-- QUESTION PROPERTIES -->
			<!-- this is for creating emi questions -->

			<!-- 1 POINTS and DISCOUNT -->
			<div>
				<h3>
					<h:outputText value="#{authorMessages.answer_point_value}" />
					<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
							onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=point#point','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
							onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=point#point','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
						<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
					</h:outputLink>
					<h:inputText id="answerptr" label="#{authorMessages.pt}" disabled="true"
						value="#{itemauthor.currentItem.itemScore}" required="true"
						size="6" >
						<f:validateDoubleRange minimum="0" />
					</h:inputText>
					<h:message for="answerptr" styleClass="validate" />
				</h3>
			</div>
			<div class="longtext">
				<h:outputLabel value="#{authorMessages.answer_point_value_display}" />    </div>
			<div class="tier3">
				<h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
				<f:selectItem itemValue="true"
				  itemLabel="#{authorMessages.yes}" />
				<f:selectItem itemValue="false"
				  itemLabel="#{authorMessages.no}" />
				</h:selectOneRadio>
			</div>

			<!-- 2 QUESTION THEME TEXT -->
			<h3>
				<h:outputText value="#{authorMessages.question_theme_text}" />
				<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
						onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=theme#theme','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
						onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=theme#theme','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
					<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
				</h:outputLink>
			</h3>
			<h:inputText id="themetext"
				value="#{itemauthor.currentItem.itemText}" required="true" size="60"></h:inputText>
			<h:message for="themetext" styleClass="validate" />
			<f:verbatim>
				<br />
				<br />
			</f:verbatim>

			<!-- 3 ANSWER OPTIONS - SIMPLE OR RICH TEXT-->
			<div class="act greyBox">
				<h3>
					<h:outputText value="#{authorMessages.options_text}" />
					<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
							onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=options#options','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
							onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=options#options','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
						<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
					</h:outputLink>
				</h3>
				<f:verbatim>
					<br />
				</f:verbatim>
				<div
					style="padding: 2px; border-style: outset; border-radius: 1em; border-color: black; border-width: 1px;">
					<h:outputText value="#{authorMessages.select_appropriate_format}" />
					<h:selectOneRadio id="emiAnswerOptionsSimpleOrRich"
						value="#{itemauthor.currentItem.answerOptionsSimpleOrRich}"
						layout="pageDirection" required="yes">
						<f:selectItem
							itemLabel="#{authorMessages.simple_text_option_label}"
							itemValue="0" />
						<f:selectItem
							itemLabel="#{authorMessages.simple_text_paste_label}"
							itemValue="2" />
						<f:selectItem itemLabel="#{authorMessages.rich_text_option_label}"
							itemValue="1" />
					</h:selectOneRadio>
				</div>
				<f:verbatim>
					<br />
				</f:verbatim>

				<!-- 3.1(a) ANSWER OPTIONS - SIMPLE TEXT OPTIONS -->
				<!-- dynamically generate rows of answer options -->
				<div id="emiAnswerOptionsSimpleOptions" class=" tier2">
					<div>
						<h:outputLabel value="#{authorMessages.answer_options}" />
					</div>
					<h:dataTable id="emiAnswerOptions"
						value="#{itemauthor.currentItem.emiAnswerOptions}" var="answer"
						headerClass="navView">
						<h:column>
							<h:panelGrid id="Row" columns="3" cellspacing="0" cellpadding="0">

								<h:panelGroup>
									<h:outputText id="Label" value="#{answer.label}" />
									<f:verbatim>
										<div style="width: 10px"></div>
									</f:verbatim>
								</h:panelGroup>

								<h:panelGrid>
									<h:inputText id="Text" value="#{answer.text}" size="40">
									</h:inputText>
								</h:panelGrid>

                                <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
									<h:outputLink id="RemoveLink"
										title="#{authorMessages.t_removeO}">
										<f:verbatim>
											<img src="/library/image/silk/cross.png" border="0" />
										</f:verbatim>
									</h:outputLink>
								</h:panelGroup>
							</h:panelGrid>
						</h:column>
					</h:dataTable>

                    <h:outputLink id="addEmiAnswerOptionsLink" value="#" styleClass="iconAdd" rendered="#{author.isEditPendingAssessmentFlow}">
						<h:outputText value="#{authorMessages.add_more_options}" />
					</h:outputLink>
					<h:selectOneMenu id="addEmiAnswerOptionsSelect" value="1" rendered="#{author.isEditPendingAssessmentFlow}">
						<f:selectItem itemLabel="1" itemValue="1" />
						<f:selectItem itemLabel="2" itemValue="2" />
						<f:selectItem itemLabel="3" itemValue="3" />
						<!-- This gets created by js... -->
					</h:selectOneMenu>
				</div>

				<!-- 3.1(b) ANSWER OPTIONS - SIMPLE TEXT PASTE -->
				<!-- dynamically generate rows of answer options by pasting -->
				<div id="emiAnswerOptionsSimplePaste" class=" tier2">
					<div>
						<h:outputLabel id="pasteLabel"
							value="#{authorMessages.answer_options_paste}" />
					</div>
					<table>
						<tr>
							<td>
								<h:inputTextarea id="emiAnswerOptionsPaste" rows="6" cols="50"
									value="#{itemauthor.currentItem.emiAnswerOptionsPaste}">
								</h:inputTextarea>
							</td>
							<td>
								<table>
									<tbody id='emiAnswerOptionsPasteLabelsTable'>
									</tbody>
								</table>
							</td>
						</tr>
					</table>
				</div>

				<!-- 3.1(c) ANSWER RICH - TABLE/GRAPHIC -->
				<div id="emiAnswerOptionsRich" class="tier2">
					<h:outputLabel value="#{authorMessages.answer_options_rich}" />
					<!-- WYSIWYG -->
					<h:panelGrid>
						<samigo:wysiwyg rows="140"
							value="#{itemauthor.currentItem.emiAnswerOptionsRich}"
							hasToggle="yes" mode="author">
							<f:validateLength minimum="1" maximum="64000" />
						</samigo:wysiwyg>
					</h:panelGrid>

					<!-- ATTACHMENTS BELOW - OPTIONS -->
					<div>
						<h:panelGroup rendered="#{itemauthor.hasAttachment}">
							<h:dataTable value="#{itemauthor.attachmentList}" var="attach">
								<h:column>
									<%@ include file="/jsf/shared/mimeicon.jsp"%>
								</h:column>
								<h:column>
									<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
									<h:outputLink value="#{attach.location}" target="new_window">
										<h:outputText escape="false" value="#{attach.filename}" />
									</h:outputLink>
								</h:column>
								<h:column>
									<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
									<h:outputText escape="false"
										value="(#{attach.fileSize} #{generalMessages.kb})"
										rendered="#{!attach.isLink}" />
								</h:column>
							</h:dataTable>
						</h:panelGroup>
						<h:panelGroup rendered="#{!itemauthor.hasAttachment}">
							<h:commandLink action="#{itemauthor.addAttachmentsRedirect}"
								value="" immediate="false" styleClass="iconAttach">
								<h:outputText value="#{authorMessages.add_attachments}" />
							</h:commandLink>
						</h:panelGroup>
						<h:panelGroup rendered="#{itemauthor.hasAttachment}">
							<h:commandLink action="#{itemauthor.addAttachmentsRedirect}"
								value="" immediate="false" styleClass="iconAttach">
								<h:outputText value="#{authorMessages.add_remove_attachments}" />
							</h:commandLink>
						</h:panelGroup>
					</div>
					<!-- ATTACHMENTS ABOVE - OPTIONS-->
					<f:verbatim>
						<br />
						<br />
					</f:verbatim>
					<div class="tier2">
						<h:outputText value="#{authorMessages.answer_options_count}" />
						<h:selectOneMenu id="answerOptionsRichCount"
							value="#{itemauthor.currentItem.answerOptionsRichCount}">
							<f:selectItem itemLabel="#{authorMessages.select_menu}"
								itemValue="0" />
							<f:selectItem itemLabel="A-B (2)" itemValue="2" />
							<f:selectItem itemLabel="A-C (3)" itemValue="3" />
							<f:selectItem itemLabel="A-D (4)" itemValue="4" />
							<f:selectItem itemLabel="A-E (5)" itemValue="5" />
							<f:selectItem itemLabel="A-F (6)" itemValue="6" />
							<f:selectItem itemLabel="A-G (7)" itemValue="7" />
							<f:selectItem itemLabel="A-H (8)" itemValue="8" />
							<f:selectItem itemLabel="A-I (9)" itemValue="9" />
							<f:selectItem itemLabel="A-J (10)" itemValue="10" />
							<f:selectItem itemLabel="A-K (11)" itemValue="11" />
							<f:selectItem itemLabel="A-L (12)" itemValue="12" />
							<f:selectItem itemLabel="A-M (13)" itemValue="13" />
							<f:selectItem itemLabel="A-N (14)" itemValue="14" />
							<f:selectItem itemLabel="A-O (15)" itemValue="15" />
							<f:selectItem itemLabel="A-P (16)" itemValue="16" />
							<f:selectItem itemLabel="A-Q (17)" itemValue="17" />
							<f:selectItem itemLabel="A-R (18)" itemValue="18" />
							<f:selectItem itemLabel="A-S (19)" itemValue="19" />
							<f:selectItem itemLabel="A-T (20)" itemValue="20" />
							<f:selectItem itemLabel="A-Y (21)" itemValue="21" />
							<f:selectItem itemLabel="A-V (22)" itemValue="22" />
							<f:selectItem itemLabel="A-W (23)" itemValue="23" />
							<f:selectItem itemLabel="A-X (24)" itemValue="24" />
							<f:selectItem itemLabel="A-Y (25)" itemValue="25" />
							<f:selectItem itemLabel="A-Z (26)" itemValue="26" />
						</h:selectOneMenu>
					</div>
				</div>
			</div>

			<!-- 4 LEAD IN STATEMENT -->
			<h3>
				<h:outputText value="#{authorMessages.lead_in_statement}" />
				<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
						onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=leadin#leadin','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
						onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=leadin#leadin','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
					<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
				</h:outputLink>
			</h3>
			<!-- WYSIWYG -->
			<h:panelGrid>
				<samigo:wysiwyg identity="lead_in_statement" rows="140"
					value="#{itemauthor.currentItem.leadInStatement}" hasToggle="yes" mode="author">
					<f:validateLength minimum="1" maximum="64000" />
				</samigo:wysiwyg>
			</h:panelGrid>
			<br />

			<!-- 5 QUESTION-ANSWER COMBINATIONS -->
			<!-- dynamicaly generate rows of question-answer combos -->
			<div class="act greyBox">
				<h3><h:outputText
						value="#{authorMessages.question_answer_combinations} " />
					<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
							onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=items#items','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
							onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=items#items','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
						<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
					</h:outputLink>
				</h3>
				<f:verbatim>
					<br />
				</f:verbatim>
				<div class="tier2">
				<h:dataTable id="emiQuestionAnswerCombinations"
					value="#{itemauthor.currentItem.emiQuestionAnswerCombinationsUI}"
					var="answer" headerClass="navView" cellspacing="0" cellpadding="0">
					<h:column>
						<h:panelGrid id="Row" columns="4" columnClasses="alignTop">
							<h:panelGroup>
								<f:verbatim>
									<span id="showItemLabel"></span>
								</f:verbatim>

								<h:inputHidden id="Label" value="#{answer.label}" />
								<f:verbatim>
									<br />
								</f:verbatim>
							</h:panelGroup>

							<!-- WYSIWYG -->
							<h:panelGrid>
								<samigo:wysiwyg rows="140" value="#{answer.text}"
									hasToggle="yes" mode="author">
									<f:validateLength maximum="64000" />
								</samigo:wysiwyg>

								<h:inputHidden id="hasAttachment"
									value="#{answer.hasAttachment}" />

								<!-- ATTACHMENTS BELOW - ITEMS-->
								<div class="longtext">
									<h:panelGroup rendered="#{answer.hasAttachment}">
										<h:dataTable value="#{answer.attachmentList}" var="attach">
											<h:column>
												<%@ include file="/jsf/shared/mimeicon.jsp"%>
											</h:column>
											<h:column>
												<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
												<h:outputLink value="#{attach.location}" target="new_window">
													<h:outputText escape="false" value="#{attach.filename}" />
												</h:outputLink>
											</h:column>
											<h:column>
												<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
												<h:outputText escape="false"
													value="(#{attach.fileSize} #{generalMessages.kb})"
													rendered="#{!attach.isLink}" />
											</h:column>
										</h:dataTable>
									</h:panelGroup>

									<h:panelGroup rendered="#{!answer.hasAttachment}">
										<h:commandLink action="#{answer.addAttachmentsRedirect}"
											value="" immediate="false" styleClass="iconAttach">
											<h:outputText value="#{authorMessages.add_attachments}" />
										</h:commandLink>
									</h:panelGroup>

									<h:panelGroup rendered="#{answer.hasAttachment}">
										<h:commandLink action="#{answer.addAttachmentsRedirect}"
											value="" immediate="false" styleClass="iconAttach">
											<h:outputText
												value="#{authorMessages.add_remove_attachments}" />
										</h:commandLink>
									</h:panelGroup>
								</div>
								<!-- ATTACHMENTS ABOVE - ITEMS -->
							</h:panelGrid>

							<h:panelGroup>
								<h:outputLabel value="#{authorMessages.correct_option_labels}" />
								<f:verbatim>
									<br />
								</f:verbatim>
								<h:inputText id="correctOptionLabels"
									value="#{answer.correctOptionLabels}" size="6"
									style="text-transform:uppercase;" />
								<f:verbatim>
									<br />
									<br />
								</f:verbatim>

								<h:outputText value="#{authorMessages.required_options_count}" />
								<f:verbatim>
									<br />
								</f:verbatim>
								<h:selectOneMenu id="requiredOptionsCount"
									onchange="this.form.onsubmit();"
									value="#{answer.requiredOptionsCount}">
									<f:selectItem itemLabel="#{authorMessages.all}" itemValue="0" />
									<f:selectItem itemLabel="1" itemValue="1" />
									<f:selectItem itemLabel="2" itemValue="2" />
									<f:selectItem itemLabel="3" itemValue="3" />
									<f:selectItem itemLabel="4" itemValue="4" />
									<f:selectItem itemLabel="5" itemValue="5" />
									<f:selectItem itemLabel="6" itemValue="6" />
									<f:selectItem itemLabel="7" itemValue="7" />
									<f:selectItem itemLabel="8" itemValue="8" />
									<f:selectItem itemLabel="9" itemValue="9" />
									<f:selectItem itemLabel="10" itemValue="10" />
									<f:selectItem itemLabel="11" itemValue="11" />
									<f:selectItem itemLabel="12" itemValue="12" />
									<f:selectItem itemLabel="13" itemValue="13" />
									<f:selectItem itemLabel="14" itemValue="14" />
									<f:selectItem itemLabel="15" itemValue="15" />
									<f:selectItem itemLabel="16" itemValue="16" />
									<f:selectItem itemLabel="17" itemValue="17" />
									<f:selectItem itemLabel="18" itemValue="18" />
									<f:selectItem itemLabel="19" itemValue="19" />
									<f:selectItem itemLabel="20" itemValue="20" />
									<f:selectItem itemLabel="21" itemValue="21" />
									<f:selectItem itemLabel="22" itemValue="22" />
									<f:selectItem itemLabel="23" itemValue="23" />
									<f:selectItem itemLabel="24" itemValue="24" />
									<f:selectItem itemLabel="25" itemValue="25" />
									<f:selectItem itemLabel="26" itemValue="26" />
								</h:selectOneMenu>
								<f:verbatim>
									<br />
									<br />
								</f:verbatim>
								
								<h:outputLabel value="#{authorMessages.emi_pt}" />
								<h:outputLink title="#{authorMessages.emi_whats_this}" value="#" 
										onclick="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=point#point','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" 
										onkeypress="javascript:window.open('/samigo-app/jsf/author/item/emiWhatsThis.faces?item=point#point','EMIWhatsThis','width=800,height=660,scrollbars=yes, resizable=yes');" >
									<h:outputText  value=" (#{authorMessages.emi_whats_this})"/>
								</h:outputLink>
								<f:verbatim>
									<br />
								</f:verbatim>
								<h:inputText id="itemScore" value="#{answer.score}" styleClass="ConvertPoint" size="4" maxlength="4" >
									<f:validateDoubleRange minimum="0.00"/>
								</h:inputText>
								<h:inputHidden id="itemScoreUserSet" value="#{answer.scoreUserSet}" />
							</h:panelGroup>

                            <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
								<h:outputLink id="RemoveLink"
									title="#{authorMessages.t_removeI}" rendered="true">
									<f:verbatim>
										<img src="/library/image/silk/cross.png" border="0" />
									</f:verbatim>
								</h:outputLink>
							</h:panelGroup>
						</h:panelGrid>
					</h:column>
				</h:dataTable>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:outputLink id="addEmiQuestionAnswerCombinationsLink" value="#" 
					styleClass="iconAdd" rendered="#{author.isEditPendingAssessmentFlow}">
					<h:outputText value="#{authorMessages.add_more_items}" />
				</h:outputLink>
				<h:selectOneMenu id="addEmiQuestionAnswerCombinationsSelect" value="1" rendered="#{author.isEditPendingAssessmentFlow}">
					<f:selectItem itemLabel="1" itemValue="1" />
					<f:selectItem itemLabel="2" itemValue="2" />
					<f:selectItem itemLabel="3" itemValue="3" />
					<!-- 
						...
						...
						This gets created by js... 
					-->
				</h:selectOneMenu>
				</div>
			</div>
			<br />

			<!-- 6 PART -->
			<h:panelGrid columns="3" columnClasses="shorttext"
				rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
				<f:verbatim>&nbsp;</f:verbatim>
				<h:outputLabel value="#{authorMessages.assign_to_p} " />
				<h:selectOneMenu id="assignToPart"
					value="#{itemauthor.currentItem.selectedSection}">
					<f:selectItems value="#{itemauthor.sectionSelectList}" />
				</h:selectOneMenu>
			</h:panelGrid>

			<!-- 7 POOL -->
			<h:panelGrid columns="3" columnClasses="shorttext"
				rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
				<f:verbatim>&nbsp;</f:verbatim>
				<h:outputLabel value="#{authorMessages.assign_to_question_p} " />
				<h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}"
					id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
					<f:selectItem itemValue=""
						itemLabel="#{authorMessages.select_a_pool_name}" />
					<f:selectItems value="#{itemauthor.poolSelectList}" />
				</h:selectOneMenu>
			</h:panelGrid>

			<!-- METADATA -->
			<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}"
				styleClass="longtext">
				<f:verbatim></f:verbatim>
				<h:outputLabel value="Metadata" />
				<br />

				<h:panelGrid columns="2" columnClasses="shorttext">
					<h:outputText value="#{authorMessages.objective}" />
					<h:inputText size="30" id="obj"
						value="#{itemauthor.currentItem.objective}" />
					<h:outputText value="#{authorMessages.keyword}" />
					<h:inputText size="30" id="keyword"
						value="#{itemauthor.currentItem.keyword}" />
					<h:outputText value="#{authorMessages.rubric_colon}" />
					<h:inputText size="30" id="rubric"
						value="#{itemauthor.currentItem.rubric}" />
				</h:panelGrid>
			</h:panelGroup>

			<%@ include file="/jsf/author/item/tags.jsp" %>

			<p class="act">
				<h:commandButton rendered="#{itemauthor.target=='assessment'}"
					value="#{authorMessages.button_save}"
					action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
				</h:commandButton>

				<h:commandButton rendered="#{itemauthor.target=='questionpool'}"
					value="#{authorMessages.button_save}"
					action="#{itemauthor.currentItem.getPoolOutcome}"
					styleClass="active">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
				</h:commandButton>


				<h:commandButton rendered="#{itemauthor.target=='assessment'}"
					value="#{commonMessages.cancel_action}" action="editAssessment"
					immediate="true">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
				</h:commandButton>

				<h:commandButton rendered="#{itemauthor.target=='questionpool'}"
					value="#{commonMessages.cancel_action}" action="editPool"
					immediate="true">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
				</h:commandButton>

				<h:inputHidden id="emiVisibleItems"
					value="#{itemauthor.currentItem.emiVisibleItems}" />

			</p>
		</h:form>

		<!-- TEXT for JavaScript Processing-->
		<h:inputHidden id="all" value="#{authorMessages.all}" />
		<h:inputHidden id="default_lead_in_statement_description"
			value="#{authorMessages.default_lead_in_statement_description}  #{authorMessages.default_lead_in_statement}" />
		<h:inputHidden id="default_lead_in_statement"
			value="#{authorMessages.default_lead_in_statement}" />

		<!-- ERROR MESSAGES for JavaScript Processing-->
		<h:inputHidden id="answer_point_value_error"
			value="#{authorMessages.answer_point_value_error}" />
		<h:inputHidden id="theme_text_error"
			value="#{authorMessages.theme_text_error}" />
		<h:inputHidden id="simple_text_options_blank_error"
			value="#{authorMessages.simple_text_options_blank_error}" />
		<h:inputHidden id="number_of_rich_text_options_error"
			value="#{authorMessages.number_of_rich_text_options_error}" />
		<h:inputHidden id="blank_or_non_integer_item_sequence_error"
			value="#{authorMessages.blank_or_non_integer_item_sequence_error}" />
		<h:inputHidden id="correct_option_labels_error"
			value="#{authorMessages.correct_option_labels_error}" />
		<h:inputHidden id="item_text_not_entered_error"
			value="#{authorMessages.item_text_not_entered_error}" />
		<h:inputHidden id="correct_option_labels_invalid_error"
			value="#{authorMessages.correct_option_labels_invalid_error}" />
		<h:inputHidden id="at_least_two_options_required_error"
			value="#{authorMessages.at_least_two_options_required_error}" />
		<h:inputHidden id="at_least_two_pasted_options_required_error"
			value="#{authorMessages.at_least_two_pasted_options_required_error}" />

		<!-- end content -->
	</div>
</body>
	</html>
</f:view>

