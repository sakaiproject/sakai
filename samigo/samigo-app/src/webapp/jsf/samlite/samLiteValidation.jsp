<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006, 2007 The Sakai Foundation.
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
			<title><h:outputText value="#{samLiteMessages.samlite_title}" /></title>
			<style>
				.list-column {
					vertical-align:top;
				}
			</style>
		</head>
		<body onload="<%= request.getAttribute("html.body.onload") %>">
			<div class="portletBody">
			<!-- content... -->
		 	<h:form id="samLiteEntryValidation">
		 			 			 	
		 	<!-- HEADINGS -->
	  		<%@ include file="/jsf/author/allHeadings.jsp" %>
	  		<h3>
	   	 		<h:outputText value="#{samLiteMessages.samlite_validation_title}"/>
	  	    </h3>
		    <p/>
  		 	<h4><h:outputText value="#{samLiteMessages.samlite_step_2_of_2}"/></h4>
		    <p/>
		    <div class="instructions"><h:outputText value="#{samLiteMessages.samlite_validation_instructions}"/></div>
  		 	<p/>
			<h:dataTable id="questions" styleClass="listHier"
		 		columnClasses="list-column,list-column,list-column,list-column"
		 		width="100%" 
		 		value="#{samLiteBean.questions}" 
		 		var="question">
		 		<h:column>
		 			<f:facet name="header">
		          		<h:outputText value="#{samLiteMessages.samlite_number}" />
		        	</f:facet>
		        	<h:outputText value="#{question.questionNumber}"/>
		 		</h:column>
		 		<h:column>
		 			<f:facet name="header">
		          		<h:outputText value="#{samLiteMessages.samlite_question}" />
		        	</f:facet>
		        	<h:outputText value="#{question.question}" escape="false"/>
		 		</h:column>
		 		<h:column>
		 			<f:facet name="header">
		          		<h:outputText value="#{samLiteMessages.samlite_type}" />
		        	</f:facet>
		 			<h:outputText value="#{question.questionTypeAsString}"/>
		 		</h:column>
		 		<h:column>
		 			<f:facet name="header">
		          		<h:outputText value="#{samLiteMessages.samlite_points}" />
		        	</f:facet>
		 			<h:outputText value="#{question.questionPoints}"/>
		 		</h:column>
		 		
		 		<h:column>
		 			<f:facet name="header">
		          		<h:outputText value="#{samLiteMessages.samlite_answers}" />
		        	</f:facet>
		 			<h:panelGrid>
          				<h:panelGroup rendered="#{question.questionType == 10}">
           					<h:dataTable value="#{question.answers}" var="answer">
						        <h:column>
								  	<h:graphicImage id="image1" rendered="#{answer.correct}"
						             alt="#{samLiteMessages.correct}" url="/images/checked.gif" >
						          	</h:graphicImage>
						          	<h:graphicImage id="image2" rendered="#{!answer.correct}"
						             alt="#{samLiteMessages.not_correct}" url="/images/unchecked.gif" >
						          	</h:graphicImage>						   
						          	<h:outputText value="#{answer.id}. #{answer.text}" escape="false"/>
						        </h:column>
						    </h:dataTable>
          				</h:panelGroup>
          				<h:panelGroup rendered="#{question.questionType == 15}">
           					<h:dataTable value="#{question.answers}" var="answer">
						        <h:column>
								  	<h:graphicImage id="image1" rendered="#{answer.correct}"
						             alt="#{samLiteMessages.correct}" url="/images/checked.gif" >
						          	</h:graphicImage>
						          	<h:graphicImage id="image2" rendered="#{!answer.correct}"
						             alt="#{samLiteMessages.not_correct}" url="/images/unchecked.gif" >
						          	</h:graphicImage>						   
						          	<h:outputText value="#{answer.id}. #{answer.text}"/>
						        </h:column>
						    </h:dataTable>
          				</h:panelGroup>
          				<h:panelGroup rendered="#{question.questionType == 20}">
          					<h:outputText value="#{question.correctAnswer}"/>
          				</h:panelGroup>
          				<h:panelGroup rendered="#{question.questionType == 30}">
          					<h:dataTable value="#{question.answers}" var="answer">
						        <h:column>
								  	<h:graphicImage id="image1" rendered="#{answer.correct}"
						             alt="#{samLiteMessages.correct}" url="/images/checked.gif" >
						          	</h:graphicImage>
						          	<h:graphicImage id="image2" rendered="#{!answer.correct}"
						             alt="#{samLiteMessages.not_correct}" url="/images/unchecked.gif" >
						          	</h:graphicImage>						   
						          	<h:outputText value="#{answer.text}"/>
						        </h:column>
						    </h:dataTable>
          				</h:panelGroup>
		 				<h:panelGroup rendered="#{question.questionType == 40}">
          					<h:outputText value="#{question.correctAnswer}"/>
          				</h:panelGroup>
		 			</h:panelGrid>
				</h:column>
		 	</h:dataTable> 
		 	<f:verbatim><p/></f:verbatim>
		 	<div class="act">
				<%-- immediate=true bypasses the valueChangeListener --%>
		     	<h:commandButton value="#{samLiteMessages.samlite_back}" type="submit" action="samLiteEntry" immediate="true"/>

			 	<%-- activates the valueChangeListener --%>
		     	<h:commandButton value="#{samLiteMessages.samlite_assessment}" type="submit" styleClass="active" action="author">
		     		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.samlite.AssessmentListener" />
		     	</h:commandButton>
		     	<h:commandButton value="#{samLiteMessages.samlite_questionpool}" type="submit" styleClass="active" action="poolList">
		     		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.samlite.QuestionPoolListener" />
		     	</h:commandButton>
		 	</div>
		 	</h:form>
		 	</div>
		</body>
	 </html>
 </f:view> 