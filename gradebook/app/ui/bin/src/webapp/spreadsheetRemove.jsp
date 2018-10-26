<!--
/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Sakai Foundation, The MIT Corporation
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
-->

<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
		<t:aliasBean alias="#{bean}" value="#{spreadsheetRemoveBean}">
			<%@ include file="/inc/appMenu.jspf"%>
		</t:aliasBean>
         <sakai:flowState bean="#{spreadsheetRemoveBean}" />         
        <h2><h:outputText value="#{msgs.remove_spreadsheet_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.remove_spreadsheet_instruction}" escape="false"/></div>

		<p>
			<h:outputFormat value="#{msgs.remove_spreadsheet_confirmation_question}">
				<f:param value="#{spreadsheetRemoveBean.spreadsheet.name}"/>
			</h:outputFormat>
		</p>

		<%@ include file="/inc/globalMessages.jspf"%>

		<div class="indnt1">
			<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
				columnClasses="prefixedCheckbox">
				<h:selectBooleanCheckbox id="removeConfirmed" value="#{spreadsheetRemoveBean.removeConfirmed}" />
				<h:outputLabel for="removeConfirmed" value="#{msgs.remove_spreadsheet_confirmation_label}" />
			</h:panelGrid>
		</div>

		<p class="act">
			<h:commandButton
				styleClass="active"
				value="#{msgs.remove_spreadsheet_submit}"
				action="#{spreadsheetRemoveBean.removeSpreadsheet}"/>
			<h:commandButton
				value="#{msgs.remove_spreadsheet_cancel}"
				action="spreadsheetListing"
				immediate="true"/>
		</p>
	  </h:form>
	</div>
</f:view>

