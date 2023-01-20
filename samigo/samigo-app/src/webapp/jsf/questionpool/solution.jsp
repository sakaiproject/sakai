<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"
	language="java"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
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
	<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head><%=request.getAttribute("html.head")%>
<title><h:outputText value="#{questionPoolMessages.sol_q}" /></title>

<script>
document.addEventListener('DOMContentLoaded', function () {
  // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
  document.getElementById('solution:questionPoolsLink').parentElement.classList.add('current');
}, false);
</script>

</head>
<body>

	<h:form id="solution">
		<!-- HEADINGS -->
		<%@ include file="/jsf/evaluation/evaluationHeadings.jsp"%>

		<div class="tier2">
			<div class="page-header">
				<h4 class="part-header">
					<h:outputText value="#{questionPoolMessages.solution_calculated_question}" />
				</h4>
			</div>
			<t:dataList value="#{questionpool.itemsBean}" var="question"
				itemStyleClass="page-header question-box"
				styleClass="question-wrapper" layout="unorderedList">
				<h:panelGroup layout="block"
					styleClass="row #{delivery.actionString}">
					<h:panelGroup layout="block" styleClass="col-sm-6">
						<h:panelGroup layout="block" styleClass="row">
							<h:panelGroup layout="block" styleClass="col-sm-12 input-group">
								<p class="input-group-addon">
									<h:outputText
										value="#{deliveryMessages.q} 1 #{deliveryMessages.of} " />
									<h:outputText value="1" />
								</p>
								<h:inputText readonly="true"
									styleClass="form-control adjustedScore#{studentScores.assessmentGradingId}.#{question.itemData.itemId}"
									id="adjustedScore" value="#{question.pointsForEdit}">
								</h:inputText>
							</h:panelGroup>
							<h:panelGroup layout="block" styleClass="col-sm-12 input-group">
								<p class="input-group-addon">
									<h:outputText
										value=" #{deliveryMessages.splash} #{question.roundedMaxPointsToDisplay} " />
									<h:outputText value="#{deliveryMessages.pt}" />
									<h:message for="adjustedScore" styleClass="sak-banner-error" />
									<h:outputText styleClass="extraCreditLabel"
										rendered="#{question.itemData.isExtraCredit == true}"
										value=" #{deliveryMessages.extra_credit_preview}" />
								</p>
							</h:panelGroup>
						</h:panelGroup>
					</h:panelGroup>
				</h:panelGroup>
				<h:panelGroup rendered="#{question.itemData.typeId == 15}">
					<div class="samigo-question-callout">
						<f:subview id="checkSolutionCalculatedQuestion">
							<%@ include
								file="/jsf/questionpool/checkSolutionCalculatedQuestion.jsp"%>
						</f:subview>
					</div>
				</h:panelGroup>
				<p class="act">
					<h:commandButton id="anotherSolution" styleClass="active"
						value="#{questionPoolMessages.another_solution}"
						action="#{questionpool.checkSolution}">
						<f:param name="itemid" value="#{question.itemData.itemId}" />
						<f:param name="outCome" value="editPool" />
					</h:commandButton>
				</p>
			</t:dataList>

			<h:commandButton style="act" value="#{commonMessages.cancel_action}"
				action="#{questionpool.cancelPool}"
				onclick="SPNR.disableControlsAndSpin(this, null);">
				<f:actionListener
					type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener" />
				<f:attribute name="returnToParentPool" value="false" />
			</h:commandButton>
		</div>
	</h:form>
</body>
	</html>
</f:view>
