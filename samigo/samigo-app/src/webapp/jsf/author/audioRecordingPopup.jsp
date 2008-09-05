<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id: audioRecordingPopup.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2008 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
		<head><%= request.getAttribute("html.head") %>
			<title><h:outputText value="#{assessmentSettingsMessages.audio_recording}" /></title>
		</head>
		<body onload="<%= request.getAttribute("html.body.onload") %>">
		    <h:outputText escape="false" value="
			<input type=\"hidden\" name=\"mediaLocation_#{param.questionId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{param.questionId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au\"/>" />
		    <h:panelGrid columns="1" border="0">
			    <%@ include file="/jsf/delivery/item/audioObject.jsp" %>
				<%@ include file="/jsf/delivery/item/audioApplet.jsp" %>
		    </h:panelGrid>
		</body>
	</html>
</f:view>
