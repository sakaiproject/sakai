<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id: fullShortAnswer.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2008 The Sakai Foundation.
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
		<head><%= request.getAttribute("html.head") %>
			<title><h:outputText value="#{authorMessages.multiple_choice_type}" escape="false"/></title>
		</head>
		<body onload="<%= request.getAttribute("html.body.onload") %>">
		<h:form>
		    <h:panelGrid columns="1" border="0">
				<h:outputText value="#{authorMessages.mc_whats_this_main_text}" escape="false"/>
				<h:outputText value="&nbsp;" escape="false"/>
				<h:panelGroup>
					<f:verbatim><b></f:verbatim>
					<h:outputText value="#{commonMessages.multiple_choice_sin}" escape="false"/>
					<f:verbatim></b></f:verbatim>
				</h:panelGroup>

				<h:outputText value="#{authorMessages.mcsc_whats_this_text}" escape="false" rendered="#{itemauthor.currentItem.partialCreditEnabled==true}"/>
				<h:outputText value=" #{authorMessages.mcsc_whats_this_text_no_partial_credit}" escape="false"  rendered="#{itemauthor.currentItem.partialCreditEnabled!=true}"/>
				<h:outputText value="&nbsp;" escape="false" rendered="#{itemauthor.currentItem.partialCreditEnabled!=true}"/>

				<h:panelGroup rendered="#{itemauthor.currentItem.partialCreditEnabled==true}" >
					<f:verbatim><ul></f:verbatim>
					<h:panelGroup>
					<f:verbatim><li></f:verbatim>
					<f:verbatim><b></f:verbatim>
					<h:outputText value="#{authorMessages.enable_nagative_marking}: " escape="false"/>
					<f:verbatim></b></f:verbatim>	
					<h:outputText value="#{authorMessages.enable_negative_makrinkg_text}" escape="false"/>
					<f:verbatim></li></f:verbatim>
					</h:panelGroup>
					<h:panelGroup>
					<h:outputText value="&nbsp;" escape="false"/>
					<f:verbatim><li></f:verbatim>
					<f:verbatim><b></f:verbatim>
					<h:outputText value="#{authorMessages.enable_partial_credit}: " escape="false"/>
					<f:verbatim></b></f:verbatim>	
					<h:outputText value="#{authorMessages.enable_partial_credit_text}" escape="false"/>
					<f:verbatim></li></f:verbatim>
					</h:panelGroup>
					<h:panelGroup>
					<h:outputText value="&nbsp;" escape="false"/>
					<f:verbatim><li></f:verbatim>
					<f:verbatim><b></f:verbatim>
					<h:outputText value="#{authorMessages.reset_grading_logic}: " escape="false"/>
					<f:verbatim></b></f:verbatim>	
					<h:outputText value="#{authorMessages.reset_to_default_grading_logic_text}" escape="false"/>
					<f:verbatim></li></f:verbatim>
					</h:panelGroup>
				<f:verbatim></ul></f:verbatim>
				</h:panelGroup>
				<h:panelGroup>
				<f:verbatim><b></f:verbatim>
				<h:outputText value="#{commonMessages.multipl_mc_ss}" escape="false"/>
				<f:verbatim></b></f:verbatim>
				</h:panelGroup>
				<h:outputText value="#{authorMessages.mcss_whats_this_text}" escape="false"/>
				
				<h:outputText value="&nbsp;" escape="false"/>

				<h:panelGroup>
				<f:verbatim><b></f:verbatim>
				<h:outputText value="#{commonMessages.multipl_mc_ms}" escape="false"/>
				<f:verbatim></b></f:verbatim>	
				</h:panelGroup>
				<h:outputText value="#{authorMessages.mcms_whats_this_text}" escape="false"/>
				<h:outputText value="#{authorMessages.mcms_whats_this_partial_text}" escape="false"/>
				<h:outputText value="#{authorMessages.mcms_whats_this_partial_note}" escape="false"/>
								<h:outputText value="#{authorMessages.mcms_whats_this_full_text}" escape="false"/>
								
				<h:outputText value="&nbsp;" escape="false"/>
				
				<h:commandButton id="close" onclick="window.close();" onkeypress="window.close();" value="#{authorMessages.button_close}"/>
		    </h:panelGrid>
		</h:form>
		</body>
	</html>
</f:view>
